/*
 * This class is the console frame for the client side.
 * It is a child of the JFrame class and it implements WindowListener to handle exits.
 * It adds a Buttons object and an OutputWin object to the frame
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Console extends JFrame implements WindowListener{
	
	private GridLayout grid;
	private JPanel panel;
	private OutputWin output;
	
	/*
	 * The constructor for Console. This creates the JFrame.
	 */
	public Console() {
		//housekeeping
		setSize(600, 300);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setTitle("Client Console");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		//Create panel
		grid = new GridLayout(1, 2);
		
		panel = new JPanel();
		panel.setLayout(grid);
		
		//add
		Buttons buttons = new Buttons();
		panel.add(buttons);
		output = new OutputWin();
		panel.add(output);
		
		//visible
		add(panel);
		setVisible(true);
	}//end constructor
	
	/*
	 * Update the console with additional text
	 * @param String msg: The message to add
	 */
	public void update(String msg) {
		output.addOutput(msg);
	}//end update

	@Override
	public void windowOpened(WindowEvent e) {
		//NOT USED
	}

	@Override
	/*
	 * This overrides the windowClosing method so that when the window is closed, 
	 * the user is prompted as to whether or not they want to disconnect.
	 * @param WindowEvent e: The WindowEvent that has occurred
	 */
	public void windowClosing(WindowEvent e) {
		int confirm = JOptionPane.showOptionDialog(
	             null, "Are You Sure to Close Application and Disconnect From Server?", 
	             "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
	             JOptionPane.QUESTION_MESSAGE, null, null, null);
	    if (confirm == 0) {
	    	//disconnect and close if confirmed
	    	Client.getTwoWay().disconnect();
	        System.exit(0);
	    }//end if confirm
	}//end windowCLosing

	@Override
	public void windowClosed(WindowEvent e) {
		//NOT USED
	}//end windowClosed

	@Override
	public void windowIconified(WindowEvent e) {
		//NOT USED
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		//NOT USED
	}

	@Override
	public void windowActivated(WindowEvent e) {
		//NOT USED
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		//NOT USED
	}
	
}//end Console
