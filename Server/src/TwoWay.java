/*
 * This is the class for a two way connection with a client.
 * Each client gets an instance of this class.
 * There is and input and and output port.
 * Each TwoWay is ran on it's own thread on and infinite loop, if the password is correct.
 */

import java.awt.EventQueue;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

public class TwoWay extends Thread{
	
	private int portIn, portOut;
	private String ip, username;
	private Listener listen;
	private ArrayList<Integer> usedIds;
	private Hashtable<Integer, byte[]> names;
	private boolean verified;
	
	/*
	 * Constructor for a two way connection
	 * @param int i: The input port
	 * @param int o: The output port
	 * @param String addr: The ip of client
	 */
	public TwoWay(int i, int o, String addr) {
		names = new Hashtable<Integer, byte[]>();
		portIn = i;
		portOut = o;
		ip = addr;
		listen = new Listener(portIn);
		start();
	}//end constructor
	
	/*
	 * This method is called when the thread is started
	 * First, it checks the username and password
	 * Then, it moves to a listen loop awaiting commands
	 */
	public void run() {
		//get username and password
		int listLength = 0;
		ArrayList<byte[]> listIn = null;
		while(listLength != 2) {
			listIn = listen.getMessages();
			listLength = listIn.size();
			System.out.print("");
		}//end while
		
		//clear listen
		listen.clear();
		
		//record the info
		username = new String(listIn.get(0));
		byte[] hash = listIn.get(1);
		
		//check account to login
		verified = false;
	
		Hashtable accounts = RunServer.getAccounts();
		byte[] passOnFile = (byte[]) accounts.get(username);
		if(passOnFile == null){
			//new account
			verified = true;
			
			//add account and server
			RunServer.addAccount(username, hash);
			File newFolder = new File(RunServer.DIRECTORY + "/" + username);
			newFolder.mkdir();
			
			//save names file
			saveNames();
			
			//end the account creation process
		}else if(new String(passOnFile).equals(new String(hash))) {
			//password verified
			verified = true;
			//open the names
			File nameFile = new File(RunServer.UTILITIES + "/names-" + username);
			try {
				FileInputStream inStream = new FileInputStream(nameFile);
				ObjectInputStream objectIn = new ObjectInputStream(inStream);
				names = (Hashtable)objectIn.readObject();
				objectIn.close();
				inStream.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
			//end the login for old account
		}else {
			//if the password is not verified
			verified = false;
		}//end else

		//send sign in response
		if(verified)
			send("verified".getBytes());
		else
			send("rejected".getBytes());
		
		//send status to console
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				RunServer.addMessage(ip + " Verified: " + verified);
			}//end run
		});//end Runnable
		
		//main loop of listener
		while(verified) {
			//listen
			int length = 0;
			listIn = new ArrayList<byte[]>();
			while(length != 1) {
				listIn = listen.getMessages();
				length = listIn.size();
				try {
					sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}//end listen loop
			listen.clear();
			
			//process
			String command = new String(listIn.get(0));
			if(command.contains("sync")) {
				int portCount = Integer.parseInt(command.split("-")[1]);
				sync(portCount);
			}else if(command.equals("pull")) {
				pull();
			}//end if
		}//end while verified
	}//end run
	
	/*
	 * Save the names to file
	 */
	public void saveNames() {
		File nameFile = new File(RunServer.UTILITIES + "/names-" + username);
		try {
			FileOutputStream outStream = new FileOutputStream(nameFile);
			ObjectOutputStream objectOut = new ObjectOutputStream(outStream);
			objectOut.writeObject(names);
			objectOut.close();
			outStream.close();
		}catch(Exception e) {
			e.printStackTrace();
		}//end catch
	}//end saveNames
	
	/*
	 * Sends a byte message to connection ip on out
	 * @param byte[] msg: The message to be sent in bytes
	 * @return boolean: Whether or not send was successful
	 */
	private boolean send(byte[] msg) {
		try {
			Socket socket = new Socket(ip, portOut);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(msg.length);
			out.write(msg);
			out.close();
			socket.close();
			sleep(50);
			return true;
		}catch(Exception e) {
			return false;
		}//end catch
	}//end sendToServer
	
	private int getNewID() {
		final int LIMIT = 10000000;
		Random rand = new Random();
		int id = rand.nextInt(LIMIT) + 1;
		
		while(usedIds.contains(id)) {
			id = rand.nextInt(LIMIT);
		}//end while
		
		return id;
	}//end getNewID
	
	/*
	 * This method is used to sync the user's files to the system
	 */
	private void sync(int count) {
		//write to console
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				RunServer.addMessage(ip + " Syncing...");
			}//end run
		});//end Runnable
		
		//data storage
		usedIds  = new ArrayList<Integer>();
		ArrayList<Integer> sendPorts = new ArrayList<Integer>();
		ArrayList<FileHandler> handlers = new ArrayList<FileHandler>();
		names = new Hashtable<Integer, byte[]>();
		
		//add ids
		for(int i = 0; i < count; i++) {
			usedIds.add(getNewID());
		}//end adding
		
		//delete old files
		String userPath = RunServer.DIRECTORY + "/" + username;
		File userFile = new File(userPath);
		String[] files = userFile.list();
		for(String file: files) {
			File toDelete = new File(userPath + "/" + file);
			toDelete.delete();
		}//end deleting
		
		//get ports
		String ports = "";
		for(int i = 0; i < count; i++) {
			int port = RunServer.getPort();
			handlers.add(new FileHandler(port, username, usedIds.get(i), this));
			ports = ports + Integer.toString(port) + "-";
			sendPorts.add(port);
		}//end for
		
		//send the ports
		boolean continueSend = true;
		while(continueSend)
			continueSend = !send(ports.getBytes());
		
		//wait for completion
		boolean wait = true;
		while(wait) {
			wait = false;
			for(FileHandler h: handlers)
				if(!h.isComplete())
					wait = true;
		}//end while
		
	}//end sync
	
	/*
	 * This method sends the files back to the client
	 */
	private void pull() {
		//write to console
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				RunServer.addMessage(ip + " Pulling...");
			}//end run
		});//end Runnable
		
		//get files
		ArrayList<FileHandler> handlers = new ArrayList<FileHandler>();
		Set<Integer> idSet = names.keySet();
		int[] ids = new int[idSet.size()];
		int count = 0;
	
		for(int i: idSet) {
			ids[count] = i;
			count++;
		}
		
		//get ports and open handlers
		ArrayList<Integer> sendPorts = new ArrayList<Integer>();
		String ports = "";
		for(int i = 0; i < ids.length; i++) {
			int port = RunServer.getPort();
			handlers.add(new FileHandler(port, username, ids[i], ip, this));
			ports = ports + Integer.toString(port) + "-";
			sendPorts.add(port);
		}//end for
		
		//send port
		boolean continueSend = true;
		while(continueSend)
			continueSend = !send(ports.getBytes());
	}//end pull
	
	/*
	 * Adds name to name file
	 * @param int id: the id of name
	 * @param byte[] name: the name in bytes
	 */
	public void addName(int id, byte[] name) {
		names.put(new Integer(id), name);
	}//end add name
	
	/*
	 * Get name from id
	 * @param int id: The id to search for
	 * @return byte[] name: the name in bytes
	 */
	public byte[] getName(int id) {
		return names.get(id);
	}//end getName
	
	/*
	 * Disconnects and destroys this TwoWay
	 */
	public void disconnect() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				RunServer.addMessage(ip + " Disconnected");
			}//end run
		});//end Runnable
		RunServer.removePort(portIn);
		RunServer.removePort(portOut);
		verified = false;
	}//end disconnect
	
	/*
	 * Get the ip of client
	 * @return String ip: The ip address
	 */
	public String getIP() {
		return ip;
	}//end getIP
	
}//end TwoWay
