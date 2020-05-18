/**
 * This class contains the main method for the server program.
 * This class listens for requests to establish a connection.
 * It then sends ports back to the client requesting and opens a TwoWay.
 * The main purpose of this class is to manage accounts and connections.
 */

import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;


public class RunServer {
	
	public static final int GETPORT = 5000;
    public static final int SENDPORT= 5001;
	public static String DIRECTORY;
	public static String UTILITIES;
	public static String ACCOUNTS;
	public static String SALT;
	
	private static ArrayList<Integer> usedPorts = new ArrayList<Integer>();
	private static Hashtable<String, TwoWay> connections = new Hashtable<String, TwoWay>();
 	private static ServerConsole console = new ServerConsole();
	
	private static Hashtable accounts;
	private static byte[] salt;
	
	/**
	 * The main method that listens for and establishes connections
	 * @param args passed when main is called
	 */
	public static void main(String[] args) {
		//set directory
		changeDirectory();
		//check os
		String os = System.getProperty("os.name").toLowerCase();
		String slash;
		if(os.contains("windows"))
			slash = "\\";
		else
			slash = "/";
		//set other directories
		UTILITIES = DIRECTORY + slash +"utilities";
		ACCOUNTS = UTILITIES + slash + "accounts";
		SALT = UTILITIES + slash + "salt";
		
		usedPorts.add(GETPORT);
		usedPorts.add(SENDPORT);
		
		//load password file
		accounts = null;
		try {
			File accountFile = new File(ACCOUNTS);
			FileInputStream inStream = new FileInputStream(accountFile);
			ObjectInputStream objectIn = new ObjectInputStream(inStream);
			accounts = (Hashtable) objectIn.readObject();
			objectIn.close();
			inStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//end catch
		
		//load salt
		salt = null;
		try {
			File saltFile = new File(SALT);
			salt = new byte[8];
			FileInputStream inStream = new FileInputStream(saltFile);
			inStream.read(salt);
			inStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//end catch
		
		//ip listen loop
		while(true) {
			try {
				//listen
				ServerSocket getter = new ServerSocket(GETPORT);
				Socket socketIn = getter.accept();
				DataInputStream streamIn = new DataInputStream(socketIn.getInputStream());
				int length = streamIn.readInt();
				byte[] dataIn = new byte[length];
				streamIn.readFully(dataIn,0,length);
				String input = new String(dataIn);
				streamIn.close();
				socketIn.close();
				getter.close();
				
				//check for type of request
				if(input.contains("dis")) {
					//disconnect
					String ip = input.split("-")[0];
					connections.get(ip).disconnect();
					connections.remove(ip);
				}else {
					//connect
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							addMessage("New client at " + input);
						}//end run
					});//end Runnable
					addConnection(input);
				}//end else
			}catch(Exception e) {
				System.out.println("Main Listener Error");
			}//end catch
		}//end while
	}//end main
	
	/**
	 * Create a new TwoWay connection with client
	 * @param ip the address of client
	 */
	private static void addConnection(String ip) {
		boolean continueSend = true;
		while(continueSend)
			continueSend = !send(ip, salt);
		//get ports
		int portIn = getPort();
		int portOut = getPort();
		//create a TwoWay
		connections.put(ip, new TwoWay(portIn, portOut, ip));
		//send ports to client
		String ports = Integer.toString(portIn) + "-" + Integer.toString(portOut);
		continueSend = true;
		while(continueSend)
			continueSend = !send(ip, ports.getBytes());
	}//end addConnection
	
	/**
	 * Find and open port
	 * @return An integer port value
	 */
	public static int getPort() {
		Random rand = new Random();
		boolean search = true;
		int port = 0;
		while(search) {
			//get a port
			port = rand.nextInt(64510) + 1025;
			if(!inUse(port) && isOpen(port))
				search = false;
		}//end while
		usedPorts.add(port);
		return port;
	}//end getPort
	
	/**
	 * Check if given port is open
	 * @param port the port to check
	 * @return true if open, false is closed
	 */
	private static boolean isOpen(int port) {
		try {
			ServerSocket ss = new ServerSocket(port);
			ss.close();
			return true;
		}catch(Exception e) {
			return false;
		}//end catch
	}//end isOpen
	
	/**
	 * Check if port is being used by this server
	 * @param port the port to check
	 * @return false if not used, true if being used
	 */
	private static boolean inUse(int port) {
		return usedPorts.contains(port);
	}//end inUse
	
	/**
	 * Sends a byte message to connection ip on out
	 * @param msg the message to be sent in bytes
	 * @return if send was success or not
	 */
	private static boolean send(String ip, byte[] msg) {
		try {
			Socket socket = new Socket(ip, SENDPORT);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(msg.length);
			out.write(msg);
			out.close();
			socket.close();
			Delay d = new Delay();
			while(!d.delay());
			return true;
		}catch(Exception e) {
			return false;
		}//end try
	}//end sendToServer
	
	/**
	 * Gets the user accounts
	 * @return the accounts Hashtable
	 */
	public static Hashtable getAccounts() {
		return accounts;
	}//end getAccounts
	
	/**
	 * add a username and password to account file
	 * @param user the username to add
	 * @param pass the password to add
	 */
	public static void addAccount(String user, byte[] pass) {
		accounts.put(user, pass);
		try {
			File accountFile = new File(ACCOUNTS);
			FileOutputStream outStream = new FileOutputStream(accountFile);
			ObjectOutputStream objectOut = new ObjectOutputStream(outStream);
			objectOut.writeObject(accounts);
			objectOut.close();
			outStream.close();
		}catch(Exception e) {
			e.printStackTrace();
		}//end catch
	}//end addAccount
	
	/**
	 * Returns the salt for password hashing
	 * @return salt the salt bytes from file
	 */
	public static byte[] getSalt() {
		return salt;
	}//end getSalt
	
	/**
	 * Removes the port from use
	 * @param port the port to be removed
	 */
	public static void removePort(int port) {
		usedPorts.add(new Integer(port));
	}//end removePort
	
	/**
	 * Add message to console
	 * @param txt the message to add
	 */
	public static void addMessage(String txt) {
		console.updateText(txt);
	}//end addMessage
	
	/**
	 * Change the directory of server
	 */
	private static void changeDirectory() {
		//file chooser
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Please Select The Server Folder");
				int result = chooser.showOpenDialog(null);
				if(result == JFileChooser.APPROVE_OPTION) {
					DIRECTORY = chooser.getSelectedFile().getAbsolutePath();
				}else {
					System.exit(0);
				}//end else
			}//end run
			});//end Runnable
		} catch (Exception e) {
			System.exit(0);
		}//end catch
	}//end changeDirectory
	
}//end RunServer
