/*
 * This is the class that creates the text output for the console.
 * It is a child of ScrollPane.
 */

import java.awt.ScrollPane;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public class OutputWin extends ScrollPane {

	private String textOut;
	private JTextPane textPane;
	
	/*
	 * The constructor for OutputWin. This sets up the ScrollPane with JTextPane
	 */
	public OutputWin() {
		//set default text
		InetAddress localHost;
		textOut = "";
		
		//get the host address
		try {
			localHost = InetAddress.getLocalHost();
			textOut = "Console for: " + localHost.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}//end catch
		
		//create text pane
		textPane = new JTextPane();
		textPane.setText(textOut);
		textPane.setEditable(false);
		add(textPane);
		
	}//end constructor
	
	/*
	 * This adds text to the text output.
	 * @param String txt: The text to add
	 */
	public void addOutput(String txt) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {	
				textOut = textOut + "\n" + txt;
				textPane.setText(textOut);
			}//end run
		});//end Runnable
	}//end addOutput
	
}//end OutputWin
