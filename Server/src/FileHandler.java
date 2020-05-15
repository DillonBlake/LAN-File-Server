/*
 * This class handles incoming and outgoing files.
 * For incoming files, each file is given an ID.
 * The ID is the key to a Hashtable with the encrypted name of the file.
 * The compressed and encrypted file data is stored in a file under the ID.
 * All of this is in the user's folder.
 * For outgoing files, the file name for the ID is sent and the compressed and encrypted data for the ID is sent.
 */

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class FileHandler extends Thread{
	
	private int port, mode, id;
	private String username, ip;
	private Listener listen;
	private TwoWay conn;
	private boolean complete;

	/*
	 * The constructor for catching files
	 * @param int p: The port to listen on
	 * @param String user: The username
	 * @param int num: The id of the file
	 * @param TwoWay tw: The TwoWay connection
	 */
	public FileHandler(int p, String user, int num, TwoWay tw) {
		port = p;
		mode = 0;
		username = user;
		conn = tw;
		id = num;
		complete = false;
		start();
	}//end constructor
	
	/*
	 * The constructor for sending files
	 * @param int p: The port to send on
	 * @param String user: The usernmae
	 * @param int num: The file id
	 * @param String addr: The ip to send to
	 * @param TwoWay tw: The TwoWay connection
	 */
	public FileHandler(int p, String user, int num, String addr, TwoWay tw) {
		port = p;
		mode = 1; 
		username = user;
		id = num;
		conn = tw;
		start();
	}//end constructor 
	
	/*
	 * The run method called by start()
	 */
	public void run() {
		if(mode == 0) {
			listen = new Listener(port);
			catcher();
		}else if(mode == 1) {
			sender();
		}//end if
	}//end run
	
	/*
	 * Get the file sent over port and store it in user's folder
	 */
	private void catcher() {
		//wait for file
		int length = 0;
		ArrayList<byte[]> listIn = new ArrayList<byte[]>();
		while(length != 2) {
			listIn = listen.getMessages();
			length = listIn.size();
			try {
				sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}//end listen loop
		
		//get the bytes
		byte[] fileName = listIn.get(0);
		byte[] data = listIn.get(1);
		
		//add name to hash
		conn.addName(id, fileName);
		
		//write file
		File file = new File(RunServer.DIRECTORY + "/" + username + "/" + id);
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			fOut.write(data);
			fOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		//finish
		RunServer.removePort(port);
		System.out.println("File received from " + ip + ": " + id);
		RunServer.addMessage("File received from " + ip + ": " + id);
		complete = true;
		
	}//end catcher
	
	/*
	 * This method sends the stored files back to the client
	 */
	public void sender() {
		byte[] name = conn.getName(id);
		
		//send name
		boolean continueSend = true;
		while(continueSend) 
			continueSend = !send(name);
		
		//send data
		continueSend = true;
		while(continueSend) {
			try {
				byte[] data;
				File f = new File(RunServer.DIRECTORY + "/" + username + "/" + id);
				FileInputStream streamIn = new FileInputStream(f);
				data = new byte[(int)f.length()];
				streamIn.read(data);
				streamIn.close();
				continueSend = !send(data);
			}catch(Exception e) {
				e.printStackTrace();
			}//end catch
		}//end while
		
		//finish
		RunServer.removePort(port);
		System.out.println("File sent to " + ip + " from: " + id);
		RunServer.addMessage("File sent to " + ip + " from: " + id);
		complete = true;
		
	}//end sender
	
	/*
	 * Sends a byte message to connection ip on out
	 * @param byte[] msg: The message to be sent in bytes
	 */
	public boolean send(byte[] msg) {
		try {
			Socket socket = new Socket(ip, port);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(msg.length);
			out.write(msg);
			out.close();
			socket.close();
			sleep(50);
			return true;
		}catch(Exception e) {
			//e.printStackTrace();
			return false;
		}//end try
	}//end sendToServer
	
	/*
	 * Check if process is complete
	 * @return boolean complete
	 */
	public boolean isComplete() {
		return complete;
	}//end isComplete
	
}//end FileHandler
