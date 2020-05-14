import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Listener extends Thread{
	
	private int port;
	private ArrayList<byte[]> messages = new ArrayList<byte[]>();
	
	public Listener(int p) {
		port = p;
		start();
	}//end constructor
	
	/*
	 * Ran when thread is started.
	 * This has a never ending loop of listening for input from the given port.
	 */
	public void run() {
		while(true) {
			try {
				//listen for connection and read
				byte[] data;
				ServerSocket server = new ServerSocket(port);
				Socket socketIn = server.accept();
				DataInputStream stream = new DataInputStream(socketIn.getInputStream());
				int length = stream.readInt();
				data = new byte[length];
				stream.readFully(data, 0, length);
				stream.close();
				socketIn.close();
				server.close();
				messages.add(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//end catch
		}//end loop
	}//end run
	
	/*
	 * Returns the list of messages received
	 * @return ArrayList<byte[]> for the messages received
	 */
	public ArrayList<byte[]> getMessages(){
		return messages;
	}//end getMessages
	
	/*
	 * Removes element from list of messages
	 * @param byte[] b: The array of bytes to remove from list
	 */
	public void removeElement(byte[] b) {
		messages.remove(b);
	}//end removeElement
	
	/*
	 * Resets the list of messages
	 */
	public void clear() {
		messages = new ArrayList<byte[]>();
	}//end clear
	
}//end Listener
