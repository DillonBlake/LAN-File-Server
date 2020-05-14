/*
 * This is the two way connection with the server.
 * There are two ports to talk to the server. First, the login is handled. 
 * Passwords are sent to the server using the MD5 hash algorithm.
 * A password based key is also created for encryption later on.
 * After login, this class then sends requests to the server to sync or pull.
 */

import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ClientTwoWay {
	
	private int portIn, portOut;
	private String ip, username;
	private SecretKey key;
	private byte[] salt;
	private Listener listen;
	private Console console;
	private Delay delay;
	
	/*
	 * This is the constructor to setup the two way. It starts the login
	 * @param int i: the in port
	 * @param int o: the out port
	 * @param String addr: the ip to send to
	 */
	public ClientTwoWay(int i, int o, String addr) {
		portIn = i;
		portOut = o;
		ip = addr;
		salt = Client.getSalt();
		console = new Console();
		console.setVisible(false);
		console.update("Connected to server at: " + ip);
		listen = new Listener(portIn);
		delay = new Delay();
		
		//get user info
		Login login = new Login();
	}//end constructor
	
	/*
	 * This method logs into the server with a username and password
	 * @param String u: the username
	 * @param Srting p: the password
	 */
	public void login(String u, String p) {
		console.setVisible(true);
		username = u;
		try {
			//setup security key
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		    PBEKeySpec spec = new PBEKeySpec(p.toCharArray(), salt, 65536, 128);
	        SecretKey temp = factory.generateSecret(spec);
	        key = new SecretKeySpec(temp.getEncoded(), "AES"); 
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}//end catch
		
		//send account info
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(p.getBytes());
			byte[] hash = md.digest();
			//send username
			send(username.getBytes());
			System.out.println("username sent");
			//send password
			send(hash);
			System.out.println("pass sent");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		//wait for response
		int length = 0;
		ArrayList<byte[]> listIn = new ArrayList<byte[]>();
		while(length != 1) {
			listIn = listen.getMessages();
			length = listIn.size();
			while(!delay.delay());
		}//end loop
				
		//proccess password response
		String response = new String(listIn.get(0));
		if(response.equals("verified")) {
			console.update("Signed in successfullly as: " + username);
		}else {
			console.update("Sign in failed!");
		}//end else
			
	}//end login
	
	
	/*
	 * Sends a byte message to connection ip on out
	 * @param byte[] msg: The message to be sent in bytes
	 * @return boolean sent: if the message was successfully sent
	 */
	private boolean send(byte[] msg) {
		try {
			Socket socket = new Socket(ip, portOut);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(msg.length);
			out.write(msg);
			out.close();
			socket.close();
			while(!delay.delay());
			return true;
		}catch(Exception e) {
			//close program
			JOptionPane.showMessageDialog(null, "Server Connection Error...Disconnecting");
			Client.getTwoWay().disconnect();
			System.exit(0);
			return false;
		}//end try
	}//end sendToServer
	
	/*
	 * This is the sync method.
	 * The client requests the server for a sync and listens for the pipe ports.
	 * A pipe is opened for each file in the sync folder.
	 * The pipe then sends the file to the server.
	 */
	public void sync() throws NoSuchAlgorithmException {
		console.update("Syncing...");
		
		//get list of files, but filter
		File syncFolder = new File(Client.mainDir);
		File[] files = syncFolder.listFiles(new FilenameFilter() {
	        @Override
	        public boolean accept(File dir, String name) {
	        	if(name.equals(".DS_Store"))
	        		return false;
	        	else if(name.equals("Utility"))
	        		return false;
	            return true;
	        }
	    });//end the filter
		
		
		for(File f: files)
			System.out.println(f.toString());
		
		//request a sync and listen for ports
		listen.clear();
		String msg = "sync-" + Integer.toString(files.length);
		boolean continueSend = true;
		while(continueSend)
			continueSend = !send(msg.getBytes());
		int length = 0;
		ArrayList<byte[]> listIn = new ArrayList<byte[]>();
		while(length != 1) {
			listIn = listen.getMessages();
			length = listIn.size();
			while(!delay.delay());
		}//end listen loop
		String[] ports = new String(listIn.get(0)).split("-");
		
		//open pipes
		ArrayList<Pipe> pipes= new ArrayList<Pipe>();
		for(int i = 0; i < files.length; i++) {	
			String dir = files[i].toString();
			int port = Integer.parseInt(ports[i]);
			pipes.add(new Pipe(dir, port, ip, key));
		}//end for file
		
		ArrayList<Pipe> completed = new ArrayList<Pipe>();
		while(completed.size() != pipes.size())
			for(Pipe p: pipes) {
				if(p.isComplete() && !completed.contains(p)){
					completed.add(p);
					console.update("File sent - " + p.getFileName());
				}//end if
			}//end for
		
		console.update("Sync Complete");
	}//end sync
	
	public void pull() {
		console.update("Pulling files from server");
		
		//get folder location
		String choice = "";
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Please Select Where To Place the Pull Folder");
			int result = chooser.showOpenDialog(null);
			if(result == JFileChooser.APPROVE_OPTION) {
				choice = chooser.getSelectedFile().getAbsolutePath();
			}else {
				console.update("Pull Cancelled");
				return;
			}//end else
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error!");
			System.exit(0);
		} //end catch
		//make folder
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		File folder = new File(choice + "/pull-" + timeStamp.replace(" ", "-"));
		folder.mkdir();
		System.out.println(folder.getAbsolutePath());
		
		//make pull request
		boolean continueSend = true;
		while(continueSend)
			continueSend = !send("pull".getBytes());
		
		//listen for ports
		listen.clear();
		int length = 0;
		ArrayList<byte[]> listIn = new ArrayList<byte[]>();
		while(length != 1) {
			listIn = listen.getMessages();
			length = listIn.size();
			while(!delay.delay());
		}//end listen
		String[] ports = new String(listIn.get(0)).split("-");
		
		//ProgressWindow win = new ProgressWindow(ports.length);
		//open pipes
		ArrayList<Pipe> pipes = new ArrayList<Pipe>();
		for(int i = 0; i < ports.length; i++) {
			try {
				pipes.add(new Pipe(Integer.parseInt(ports[i]), ip, key, folder.getAbsolutePath()));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}//end catch
		}//end for
		
	}//end pull
	
	/*
	 * Disconnects from server
	 */
	public void disconnect() {
		System.out.println("dis");
		send("disconnect".getBytes());
	}//end disconnect
	
	/*
	 * Gets the console object
	 * @return Console
	 */
	public Console getConsole() {
		return console;
	}
}//end TwoWay
