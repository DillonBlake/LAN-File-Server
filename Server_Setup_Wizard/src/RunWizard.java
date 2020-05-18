/**
 * This program is a simple setup wizard.
 * It asks the user where they want to setup their server then creates the file structure.
 * Then, the file url is output for the user to copy.
 */

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class RunWizard {
	
	private static String slash;
	
	/**
	 * The main method creates the file structure
	 * @param args the parameters passed when main is called
	 */
	public static void main(String[] args) {
		//check os
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("windows"))
			slash = "\\";
		else
			slash = "/";
				
		//intro
		JOptionPane.showMessageDialog(null, "Welcome to the local network server setup wizard.\n"
				+ "This program is used to setup the file structure on the server side.\n"
				+ "You will now choose a location for the server folder.");
		
		//file chooser
		try {
			EventQueue.invokeAndWait(new Runnable() {
			    @Override
			    public void run() {
			        JFileChooser chooser = new JFileChooser();
			        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			        int result = chooser.showOpenDialog(null);
			        if(result == JFileChooser.APPROVE_OPTION) {
			        	setup(chooser.getSelectedFile());
			        }else {
			        	System.exit(0);
			        }
			    }//end run
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error!");
			System.exit(0);
		} //end catch
		
	}//end main
	
	/**
	 * This method performs the setup
	 * @param file the File chosen from the chooser
	 */
	public static void setup(File file) {
		//setup paths
		String url = file.getAbsolutePath();
		String mainFolder = url + slash + "LAN-Server";
		String utilities = mainFolder + slash + "utilities";
		String accounts = utilities + slash + "accounts";
		String salt = utilities + slash + "salt";
		
		//make folders
		File folderFile = new File(mainFolder);
		folderFile.mkdir();
		File utilFile = new File(utilities);
		utilFile.mkdir();
		
		//setup accounts
		Hashtable<String, byte[]> accountHash = new Hashtable<String, byte[]>();
		FileOutputStream outStream;
		File accountsFile = new File(accounts);
		try {
			outStream = new FileOutputStream(accountsFile);
			ObjectOutputStream objectOut = new ObjectOutputStream(outStream);
			objectOut.writeObject(accountHash);
			objectOut.close();
			outStream.close();
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error!");
			System.exit(0);
		}//end catch
		
		//setup Salt
		byte[] saltBytes = new byte[8];
		SecureRandom srand = new SecureRandom();
		srand.nextBytes(saltBytes);
		File saltFile = new File(salt);
		try {
			outStream = new FileOutputStream(saltFile);
			outStream.write(saltBytes);
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error!");
			System.exit(0);
		}//end catch
		
		//output info
		JOptionPane.showMessageDialog(null, "Setup Complete! Please remember the path bellow to open the server.\n\n" + mainFolder);
	}//end setup

}//end RunWizard
