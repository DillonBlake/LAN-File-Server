import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class Console extends JFrame implements WindowListener{
	
	private GridLayout grid;
	private JPanel panel;
	private OutputWin output;
	
	public Console() throws UnknownHostException {
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
		output.addOuput(msg);
	}//end update

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		int confirm = JOptionPane.showOptionDialog(
	             null, "Are You Sure to Close Application and Disconnect From Server?", 
	             "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
	             JOptionPane.QUESTION_MESSAGE, null, null, null);
	    if (confirm == 0) {
	    	Client.getTwoWay().disconnect();
	        System.exit(0);
	    }//end if confirm
	}//end windowCLosing

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}//end windowClosed

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}//end Console
