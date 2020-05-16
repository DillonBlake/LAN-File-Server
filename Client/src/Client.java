/*
 * This is class contains the main method for the client side program.
 * When this is ran, the ip address of the client is sent on the main input line to the server.
 * The server will then respond with two ports to create a two way connection with.
 * With these ports, a ClientTwoWay object is created and the bulk of the processing switches to that object
 */

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Client{
    
    public static final int SENDPORT = 5000;
    public static final int GETPORT = 5001;
    
    public static String mainDir, utilitiesDir, keyDir, tempDir, ip;
    public static int portIn, portOut;
    
    private static ClientTwoWay twoWay;
    private static byte[] salt = null;
    
    /*
     * The main method sets up the ClientTwoWay
     * @param String[] args
     */
	public static void main(String args[]) {
		
		//setup directories
		changeDirectory();
		
		//open listener on the GETPORT
		Listener listen = new Listener(GETPORT);
		
		//get ip
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					ip = JOptionPane.showInputDialog("Please enter the ip adress of the server you are trying to connect to:");
				}//end run
			});
		} catch (Exception e) {
			e.printStackTrace();
		}//end Runnable
		
		try {
			//send ip
			InetAddress localHost = InetAddress.getLocalHost();
			boolean continueSend = true;
			while(continueSend)
				continueSend = !send(localHost.getHostAddress().getBytes());
			
			//wait for response
			int length = 0;
			ArrayList<byte[]> listIn = null;
			while(length != 2) {
				listIn = listen.getMessages();
				length = listIn.size();
				Delay d = new Delay();
				while(!d.delay());
			}//end while listen
			
			//parse the response
			salt = listIn.get(0);
			String ports = new String(listIn.get(1));
			portOut = Integer.parseInt(ports.split("-")[0]);
			portIn = Integer.parseInt(ports.split("-")[1]);
			
			//make a ClientTwoWay
			twoWay = new ClientTwoWay(portIn, portOut, ip);
		}catch(Exception e){
			//close if socket can't be connected
			throwError();
			System.exit(0);
		}//end catch
		
	}//end main
	
	/*
	 * Sends a byte message to connection ip on out
	 * @param byte[] msg: The message to be sent in bytes
	 * @return boolean sent: true if sent successfully, false if failed
	 */
	public static boolean send(byte[] msg) {
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
	
	/*
	 * Get the salt sent from server
	 * @return byte[] salt: The salt sent from server
	 */
	public static byte[] getSalt() {
		return salt;
	}//end getSalt
	
	/*
	 * Shows a message box for an error
	 * @param String txt: The text to show
	 * 
	 */
	public static void throwError(String txt) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
			    @Override
			    public void run() {
			    	JOptionPane.showMessageDialog(null, txt, "Error", JOptionPane.WARNING_MESSAGE);
			    }//end run
			});//end Runnable
		} catch (Exception e) {
			e.printStackTrace();
		}//end catch
	}//end throwError
	
	/*
	 * Shows a message box for a connection error
	 */
	public static void throwError() {
		throwError("Connection Error...Disconnecting");
		if(twoWay != null)
			twoWay.disconnect();
	}//end throwError
	
	/*
	 * Get the TwoWay
	 * @return ClientTwoWay
	 */
	public static ClientTwoWay getTwoWay() {
		return twoWay;
	}//end getTwoWay
	
	/*
	 * Change server folder directly with a file chooser
	 */
	private static void changeDirectory() {
		//file chooser
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Please Select The Folder You Want To Sync");
				int result = chooser.showOpenDialog(null);
				if(result == JFileChooser.APPROVE_OPTION) {
					mainDir = chooser.getSelectedFile().getAbsolutePath();
					if(twoWay != null) {
						twoWay.getConsole().update("Sync Location Changed To:\n" + mainDir);
					}else {
						System.out.println(mainDir);
					}//end else
					utilitiesDir = mainDir + "/Utility";
					keyDir = utilitiesDir + "/key";
					tempDir = utilitiesDir + "/temp";
				}else {
					System.exit(0);
				}//end else
			}//end run
			});//end Runnable
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throwError("Problem Finding Directory...Exiting");
			System.exit(0);
		} //end catch
	}//end changeDirectory

}//end Client
