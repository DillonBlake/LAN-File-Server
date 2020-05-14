import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Random;

public class RunServer {
	
	public static final int GETPORT = 5000;
    public static final int SENDPORT= 5001;
	public static final String DIRECTORY = "/Users/dillonblake/Desktop/LAN-Server";
	public static final String UTILITIES = DIRECTORY + "/utilities";
	public static final String ACCOUNTS = UTILITIES + "/accounts";
	public static final String SALT = UTILITIES + "/salt";
	
	private static ArrayList<Integer> usedPorts = new ArrayList<Integer>();
	private static ArrayList<TwoWay> connections = new ArrayList<TwoWay>();
	
	private static String ip;
	private static Dictionary accounts;
	private static byte[] salt;

	public static void main(String[] args) throws IOException {
		usedPorts.add(GETPORT);
		usedPorts.add(SENDPORT);
		
		//load password file
		accounts = null;
		try {
			File accountFile = new File(ACCOUNTS);
			FileInputStream inStream = new FileInputStream(accountFile);
			ObjectInputStream objectIn = new ObjectInputStream(inStream);
			accounts = (Dictionary) objectIn.readObject();
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
			ServerSocket getter = new ServerSocket(GETPORT);
			Socket socketIn = getter.accept();
			System.out.println("client accepted");
			DataInputStream streamIn = new DataInputStream(socketIn.getInputStream());
			int length = streamIn.readInt();
			byte[] dataIn = new byte[length];
			streamIn.readFully(dataIn,0,length);
			ip = new String(dataIn);
			System.out.println(ip);
			streamIn.close();
			socketIn.close();
			getter.close();
			addConnection(ip);
		}//end while
	}//end main
	
	/*
	 * Create a new TwoWay connection with client
	 * @param String ip: Address of client
	 */
	private static void addConnection(String ip) {
		send(salt);
		//get ports
		int portIn = getPort();
		int portOut = getPort();
		//create a TwoWay
		connections.add(new TwoWay(portIn, portOut, ip));
		//send ports to client
		String ports = Integer.toString(portIn) + "-" + Integer.toString(portOut);
		send(ports.getBytes());
		System.out.println("sent");
	}//end addConnection
	
	/*
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
	
	/*
	 * Check if given port is open
	 * @param int port: Port to check
	 * @return boolean: True if open, false is closed
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
	
	/*
	 * Check if port is being used by this server
	 * @param int port: Port to check
	 * @return boolean: False if not used, true if being used
	 */
	private static boolean inUse(int port) {
		return usedPorts.contains(port);
	}//end inUse
	
	/*
	 * Sends a byte message to connection ip on out
	 * @param byte[] msg: The message to be sent in bytes
	 */
	private static void send(byte[] msg) {
		try {
			Socket socket = new Socket(ip, SENDPORT);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(msg.length);
			out.write(msg);
			out.close();
			socket.close();
			Delay d = new Delay();
			while(!d.delay());
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}//end try
	}//end sendToServer
	
	/*
	 * Gets the user accounts
	 * @return Dictionary of accounts
	 */
	public static Dictionary getAccounts() {
		return accounts;
	}//end getAccounts
	
	/*
	 * add a username and password to account file
	 * @param String user: The username to add
	 * @param byte[] pass: The password to add
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
	
	/*
	 * Returns the salt for password hashing
	 * @return byte[] salt: The salt bytes from file
	 */
	public static byte[] getSalt() {
		return salt;
	}//end getSalt
	
	/*
	 * Removes the port from use
	 */
	public static void removePort(int port) {
		usedPorts.add(new Integer(port));
	}//end removePort
	
}//end RunServer
