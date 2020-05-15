/*
 * This class connects to the server to send or receive files
 * It handles the encryption/ decryption process and the compression/decompression.
 * The Advanced Encryption Standard is used and the Deflater class at default compression level is used.
 * Each instance of Pipe is ran on its own thread.
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;	
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;

public class Pipe extends Thread{

	public static final String ALG = "AES";
	private String url, address, fileLocation, fileName;
	private boolean complete;
	private int mode, port;
	private SecretKey key;
	
	/*
	 * The constructor to send a file
	 * @param String url: The file directory
	 * @param int p: The port to send on
	 * @param String sendTo: The ip to send to
	 * @param SecretKey key: The encryption key
	 */
	public Pipe(String url, int p, String sendTo, SecretKey key) throws NoSuchAlgorithmException {
		this.url = url;
		this.key = key;
		port = p;
		address = sendTo;
		complete = false;
		mode = 0;
		start();
	}//end constructor
	
	/*
	 * The constructor to receive a file
	 * @param int p: The port to listen on
	 * @param String getFrom: The ip to that is sending the file
	 * @param SecretKey key: The decryption key
	 * @param String fLoc: The directory to story the file in
	 */
	public Pipe(int p, String getFrom, SecretKey key, String fLoc) throws NoSuchAlgorithmException {
		this.key = key;
		port = p;
		address = getFrom;
		fileLocation = fLoc;
		complete = false;
		mode = 1;
		start();
	}//end constructor
	
	/*
	 * The run method to be called by start()
	 * Either sends or receives based on the mode
	 */
	public void run() {
		if(mode == 0) {
			sendFile();
		}else if(mode == 1) {
			receive();
		}
	}//end run
	
	/*
	 * This trims the file name bytes. 
	 * Extra bytes are added in the sending process and have to be removed
	 * @param byte[] input: The string in bytes
	 */
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
	}//end trim
	
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
				sleep(50);
			}//end while
		
			//decrypt name
			byte[] secureNameByte = responseList.get(0);
			byte[] nameByte;
			cipher.init(Cipher.DECRYPT_MODE, key);
			nameByte = cipher.doFinal(Base64.getDecoder().decode(secureNameByte));
			String name = new String(trim(nameByte));
			fileName = name;
			
			//get the data
			byte[] secureCompData = responseList.get(1);
			
			//decrypt to compressed file
			byte[] compData;
			compData = cipher.doFinal(Base64.getDecoder().decode(secureCompData));
			
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
			fileName = name;
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
	private boolean send(byte[] msg) {
		try {
			Socket socket = new Socket(address, port);
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(msg.length);
			out.write(msg);
			out.close();
			socket.close();
			return true;
		}catch(Exception e) {
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
		return fileName;
	}//end getFileName
	
}//end Pipe
