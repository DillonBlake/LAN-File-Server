/**
 * This class creates a simple GUI to see what is going on with the server.
 */

import java.awt.GridLayout;
import javax.swing.JFrame;

public class ServerConsole extends JFrame{

	private OutputWin textWin;
	private GridLayout grid;
	
	/**
	 * The constructor for ServerConsole. The JFrame is setup and the OutputWin is added.
	 */
	public ServerConsole() {
		//housekeeping
		setSize(400, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Server Console");
		grid = new GridLayout(1, 1);
		setLayout(grid);
		
		//add OutputWin
		textWin = new OutputWin();
		add(textWin);
		
		//final stuff
		setVisible(true);
	}//end constructor
	
	/**
	 * Adds output to the console window
	 * @param txt the text to add
	 */
	public void updateText(String txt) {		
		textWin.addOutput(txt);
	}//end updateText
	
}//end ServerConsole
