import java.awt.ScrollPane;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JTextPane;

public class OutputWin extends ScrollPane{

	private String textOut;
	private JTextPane textPane;
	
	public OutputWin() throws UnknownHostException {
		//set default text
		InetAddress localHost = InetAddress.getLocalHost();
		textOut = "Console for: " + localHost.getHostAddress();
		
		//create text pane
		textPane = new JTextPane();
		textPane.setText(textOut);
		textPane.setEditable(false);
		add(textPane);
		
	}//end constructor
	
	public void addOuput(String txt) {
		textOut = textOut + "\n" + txt;
		textPane.setText(textOut);
	}//end addOutput
	
}//end OutputWin
