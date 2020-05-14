import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;

public class Pipe extends Thread{

	public static final String ALG = "AES";
	
	private String url, address, fileLocation;
	private final String TEMP_DIREC = "/Users/dillonblake/Desktop/Sync/Utility/temp";
	private final String DIRECTORY = "/Users/dillonblake/Desktop/Sync/";
	private boolean complete;
	private int mode, port;
	SecretKey key;
	
	public Pipe(String url, int p, String sendTo, SecretKey key) throws NoSuchAlgorithmException {
		this.url = url;
		this.key = key;
		port = p;
		address = sendTo;
		complete = false;
		mode = 0;
		start();
	}//end constructor
	
	public Pipe(int p, String getFrom, SecretKey key, String fLoc) throws NoSuchAlgorithmException {
		this.key = key;
		port = p;
		address = getFrom;
		fileLocation = fLoc;
		complete = false;
		mode = 1;
		start();
	}
	
	public void run() {
		if(mode == 0) {
			sendFile();
		}else if(mode == 1) {
			receive();
		}
	}//end run
	
	public byte[] trim(byte[] input) {
		byte[] trimmed = new byte[input.length - 2];
		for(int i = 0; i < input.length; i++)
			if(i >= 2)
				trimmed[i - 2] = input[i];
	
		ArrayList<Byte> cutBytes = new ArrayList<Byte>();
		for(int i = 0; i < trimmed.length; i++)
			if(i % 2 != 0) 
				cutBytes.add(trimmed[i]);
		
		byte[] finalBytes = new byte[cutBytes.size()];
		for(int  i = 0; i < cutBytes.size(); i++)
			finalBytes[i] = cutBytes.get(i);
		
		return finalBytes;
	}
	
	/*
	 * This method first receives and decrypts the name of the file.
	 * Then, it received the file, decrypts it, and decompresses it.
	 * Boolean complete is set to true at the end.
	 */
	public void receive() {
		try {
			//setup decryption
			Cipher cipher = Cipher.getInstance(ALG);
			
			//listen
			Listener listen = new Listener(port);
			int responseLength = 0;
			ArrayList<byte[]> responseList = null;
			while(responseLength != 2) {
				responseList = listen.getMessages();
				responseLength = responseList.size();
				System.out.println(responseLength);
				sleep(50);
			}//end while
			
			System.out.println("got data");
		
			
			//decrypt name
			byte[] secureNameByte = responseList.get(0);
			byte[] nameByte;
			cipher.init(Cipher.DECRYPT_MODE, key);
			nameByte = cipher.doFinal(Base64.getDecoder().decode(secureNameByte));
			String name = new String(trim(nameByte));
			System.out.println(name);
			System.out.println("name decrypted");
			
			//get the data
			byte[] secureCompData = responseList.get(1);
			
			//decrypt to compressed file
			byte[] compData;
			compData = cipher.doFinal(Base64.getDecoder().decode(secureCompData));
			System.out.println("data decrypted");
			
			//decompress compressed bytes
			byte[] fileData;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Inflater inflater = new Inflater();
			inflater.setInput(compData);
			while(!inflater.finished()) {
				int n = inflater.inflate(buffer);
				baos.write(buffer, 0, n);
			}//end while
			baos.close();
			inflater.end();
			fileData = baos.toByteArray();
			
			//write new file
			File newFile = new File(fileLocation + "/" + name);
			FileOutputStream fOut = new FileOutputStream(newFile);
			fOut.write(fileData);
			fOut.close();
			System.out.println("written final");
	
			//set complete to true
			complete = true;
			
		}catch(Exception e) {
			//close program
			JOptionPane.showMessageDialog(null, "Server Connection Error...Disconnecting");
			Client.getTwoWay().disconnect();
			System.exit(0);
		}//end catch
		
	}//end receive
	
	/*
	 * This method encrypts the file name and then sends it over the socket. 
	 * Then, the method compresses the file, encrypts it, and sends it as well.
	 * Boolean complete is set to true at the end.
	 */
	public void sendFile() {
		try {
			//setup encryption
			Cipher cipher = Cipher.getInstance(ALG);
			
			//encrypt name
			String[] split = url.split("/");
			String name = split[split.length - 1];
			System.out.println("name: " + name);
			byte[] secureName;
			cipher.init(Cipher.ENCRYPT_MODE, key);
			secureName = cipher.doFinal(name.getBytes("UTF-16"));
			
			//encode name
			byte[] encodedSecureName = Base64.getEncoder().encode(secureName);
			
			//send name
			boolean continueSend = true;
			while(continueSend)
				continueSend = !send(encodedSecureName);
			
			//open and read original
			File originalFile = new File(url);
			FileInputStream fIn = new FileInputStream(originalFile);
			byte[] originalData = new byte[(int)originalFile.length()];
			fIn.read(originalData);
			fIn.close();
			
			//compress a temporary file
			byte[] compData;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream(originalData.length);
			Deflater deflater = new Deflater();
			deflater.setInput(originalData);
			deflater.finish();
			while(!deflater.finished()) {
				int n = deflater.deflate(buffer);
				baos.write(buffer, 0, n);
			}//end while
			deflater.end();
			baos.close();
			compData = baos.toByteArray();
			
			//encrypt the compressed data
			byte[] secureCompData;
			secureCompData = cipher.doFinal(compData);
			
			//encode date
			byte[] encodedSecureCompData = Base64.getEncoder().encode(secureCompData);
			
			//send the compressed, encoded, encrypted data
			continueSend = true;
			while(continueSend)
				continueSend = !send(encodedSecureCompData);
			
			//set complete flag true
			complete = true;
			
		}catch(Exception e) {
			//close program
			JOptionPane.showMessageDialog(null, "Server Connection Error...Disconnecting");
			Client.getTwoWay().disconnect();
			System.exit(0);
		}//end catch
	}//end send
	
	/*
	 * Sends a byte message to connection ip on out
	 * @param byte[] msg: The message to be sent in bytes
	 */
	public boolean send(byte[] msg) {
		try {
			Socket socket = new Socket(address, port);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(msg.length);
			out.write(msg);
			out.close();
			socket.close();
			return true;
		}catch(Exception e) {
			//close program
			System.out.println("send error");
			//JOptionPane.showMessageDialog(null, "Server Connection Error...Disconnecting");
			//Client.getTwoWay().disconnect();
			//System.exit(0);
			return false;
		}//end try
	}//end sendToServer
	
	/*
	 * Checks if session is complete
	 * @return boolean complete
	 */
	public boolean isComplete() {
		return complete;
	}//end isComplete
	
	/*
	 * Returns name of file
	 * @return String name
	 */
	public String getFileName() {
		String[] list = url.split("/");
		return list[list.length - 1];
	}//end getFileName
	
}//end Pipe
