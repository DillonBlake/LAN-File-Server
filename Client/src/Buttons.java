/**
 * This class manages the button grid for the console.
 * It is a child of the JPanel class
 * The ActionListener is implemented as well
 */

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Buttons extends JPanel implements ActionListener{
	
	//vars
	private JButton directoryBtn, syncBtn, pullBtn, backgroundBtn;
	private boolean backSync;
	private GridLayout grid;
	private BackgroundSyncer syncer;

	/**
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
			Runnable r = new Runnable() {
				public void run(){
					Client.getTwoWay().sync();
				}//end run
			};//end Runnable
			Thread t = new Thread(r);
			t.start();
		}else if(e.getSource() == pullBtn) {
			Client.getTwoWay().pull();
		}else if(e.getSource() == directoryBtn) {
			changeDirectory();
		}else if(e.getSource() == backgroundBtn) {
			toggleSync();
		}//end if
		
	}//end actionPerformed
	
	/**
	 * This method changes the background sync settings
	 */
	public void toggleSync() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {	
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
			}//end run
		});//end Runnable
	}//end toggleSync
	
	/**
	 * Change server folder directly with a file chooser
	 */
	private static void changeDirectory() {
		//check os
		String slash;
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("windows"))
			slash = "\\";
		else
			slash = "/";
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//file chooser
				try {
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setDialogTitle("Please Select The Folder Your Want To Sync");
					int result = chooser.showOpenDialog(null);
					if(result == JFileChooser.APPROVE_OPTION) {
						Client.mainDir = chooser.getSelectedFile().getAbsolutePath();
						Client.utilitiesDir = Client.mainDir + slash + "Utility";
						Client.keyDir = Client.utilitiesDir + slash + "key";
						Client.tempDir = Client.utilitiesDir + slash + "temp";
						Client.getTwoWay().getConsole().update("Sync Location Changed To:\n" + Client.mainDir);
					}else {
						//no else
					}//end else
				} catch (Exception e) {
					Client.throwError("Problem Finding Directory...Exiting");
				} //end catch
			}//end run
		});//end Runnable
	}//end changeDirectory
	
}//end Buttons
