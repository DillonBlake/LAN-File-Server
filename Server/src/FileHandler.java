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

	public FileHandler(int p, String user, int num, int m, TwoWay tw) {
		port = p;
		mode = m;
		username = user;
		conn = tw;
		id = num;
		complete = false;
		start();
	}//end constructor
	
	public FileHandler(int p, String user, int num, int m, String addr, TwoWay tw) {
		port = p;
		mode = m; 
		username = user;
		mode = m;
		id = num;
		conn = tw;
		start();
	}//end constructor 
	
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
		System.out.println("file received");
		
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
		
		System.out.println(fileName + ": got");
		
		//finish
		RunServer.removePort(port);
		complete = true;
		
	}//end catcher
	
	/*
	 * This method sends the stored files back to the client
	 */
	public void sender() {
		System.out.println("Port: " + port);
		boolean continueSend = true;
		System.out.println(id);
		byte[] name = conn.getName(id);
		
		//send name
		while(continueSend) {
			try {
				continueSend = !send(name);
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}//end catch
			System.out.println("sending");
		}//end while
		
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
				System.out.println(continueSend);
			}catch(Exception e) {
				e.printStackTrace();
			}//end catch
		}//end while
		
		//finish
		RunServer.removePort(port);
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
