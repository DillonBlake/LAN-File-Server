/*
 * This is the class for the login window.
 * When an instance is created, a window will appear to collect a usernmae and password.
 * Once the sign in button is clicked, the login method in the ClientTwoWay is called
 * and the username and password are passed in as parameters. The login window is then removed.
 * This class is a child of the JFrame class and implements the ActionListener class
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Login extends JFrame implements ActionListener{
	
	private GridLayout grid;
	private JLabel userLbl, passLbl;
	private JPasswordField passFld;
	private JTextField userFld;
	private JButton loginBtn;
	
	/*
	 * Constructor sets up the frame
	 */
	public Login() {
		//housekeeping
		setSize(300, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Login to Server");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		//layout
		grid = new GridLayout(5, 1);
		setLayout(grid);
		
		//create objects
		userLbl = new JLabel("Username:");
		add(userLbl);
		
		userFld = new JTextField();
		add(userFld);
		
		passLbl = new JLabel("Password:");
		add(passLbl);
		
		passFld = new JPasswordField();
		add(passFld);
		
		loginBtn = new JButton("Login or Create New Account");
		loginBtn.addActionListener(this);
		add(loginBtn);
		
		//final housekeeping
		setVisible(true);
		
	}

	@Override
	/*
	 * Listens for a button click and then calls login for the ClientTwoWay created in Client.
	 * @param: ActionEvent e: The event from the JButton
	 */
	public void actionPerformed(ActionEvent e) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String username = userFld.getText();
				String password = new String(passFld.getPassword());
				
				if(e.getSource() == loginBtn && !username.equals("")){
					Client.getTwoWay().login(username, password);
					setVisible(false);
					dispose();
				}//end if
			}//end run
		});//end Runnable
		
	}//end actionPerformed

}//end Login
