import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Login extends JFrame implements ActionListener{
	
	private GridLayout grid;
	private JLabel userLbl, passLbl;
	private JPasswordField passFld;
	private JTextField userFld;
	private JButton loginBtn;
	
	/*
	 * constructor sets up the frame
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
	public void actionPerformed(ActionEvent e) {
		String username = userFld.getText();
		String password = new String(passFld.getPassword());
		
		if(e.getSource() == loginBtn && !username.equals("")){
			Client.getTwoWay().login(username, password);
			setVisible(false);
			dispose();
		}//end if
		
	}//end actionPerformed

}
