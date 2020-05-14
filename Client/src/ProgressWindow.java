import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class ProgressWindow extends JFrame{
	
	private ProgressPanel panel;
	private JScrollPane scroller;
	
	/*
	 * Constructor
	 * @param int count: The number of pipes
	 */
	public ProgressWindow(int count) {
		//housekeeping
		setSize(300, 300);
		setTitle("Pull Progress");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//create pane
		panel = new ProgressPanel(count);
		scroller = new JScrollPane(panel);
		add(scroller);
		
		//final
		setVisible(true);
		
	}//end constructor
	
	
}//end class
