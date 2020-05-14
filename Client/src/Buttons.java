/*
 * This class manages the button grid for the console.
 * It is a child of the JPanel class
 * The ActionListener is implemented as well
 */

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Buttons extends JPanel implements ActionListener{
	
	//vars
	private JButton directoryBtn, syncBtn, pullBtn, backgroundBtn;
	private boolean backSync;
	private GridLayout grid;
	private BackgroundSyncer syncer;

	/*
	 * The constructor for Buttons. This sets up the button grid.
	 */
	public Buttons() {
		backSync = false;
		syncer = new BackgroundSyncer();
		
		//setup buttons
		directoryBtn = new JButton("Change Directory");
		syncBtn = new JButton("Sync");
		pullBtn = new JButton("Pull");
		backgroundBtn = new JButton("Background Sync: Off");
		backgroundBtn.setOpaque(true);
		backgroundBtn.setForeground(Color.RED);
		
		//action listener
		directoryBtn.addActionListener(this);
		syncBtn.addActionListener(this);
		backgroundBtn.addActionListener(this);
		pullBtn.addActionListener(this);
		
		//grid
		grid = new GridLayout(2, 2);
		setLayout(grid);
		add(directoryBtn);
		add(syncBtn);
		add(backgroundBtn);
		add(pullBtn);
		
	}//end constructor

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == syncBtn) {
			try {
				Client.getTwoWay().sync();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}//end catch
		}else if(e.getSource() == pullBtn) {
			Client.getTwoWay().pull();
		}else if(e.getSource() == directoryBtn) {
			changeDirectory();
		}else if(e.getSource() == backgroundBtn) {
			toggleSync();
		}//end if
		
	}//end actionPerformed
	
	/*
	 * This method changes the background sync settings
	 */
	public void toggleSync() {
		if(backSync) {
			syncer.disable();
			backSync = false;
			backgroundBtn.setForeground(Color.RED);
			backgroundBtn.setText("Background Sync: Off");
		}else {
			syncer.enable();
			backSync = true;
			backgroundBtn.setForeground(Color.GREEN);
			backgroundBtn.setText("Background Sync: On");
		}//end else
		
	}//end toggleSync
	
	/*
	 * Change server folder directly with a file chooser
	 */
	private static void changeDirectory() {
		//file chooser
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Please Select The Folder Your Want To Sync");
			int result = chooser.showOpenDialog(null);
			if(result == JFileChooser.APPROVE_OPTION) {
				Client.mainDir = chooser.getSelectedFile().getAbsolutePath();
				Client.utilitiesDir = Client.mainDir + "/Utility";
				Client.keyDir = Client.utilitiesDir + "/key";
				Client.tempDir = Client.utilitiesDir + "/temp";
				Client.getTwoWay().getConsole().update("Sync Location Changed To:\n" + Client.mainDir);
			}else {
				//no else
			}//end else
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error!");
			System.exit(0);
		} //end catch
	}//end changeDirectory
}//end Buttons
