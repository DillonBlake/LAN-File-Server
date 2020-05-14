import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressPanel extends JPanel implements ActionListener{

	private GridLayout grid;
	private JProgressBar[] bars;
	private JButton[] buttons;
	
	/*
	 * Constructor
	 * @param int count: Number of pipes
	 */
	public ProgressPanel(int count) {
		//layout
		grid = new GridLayout(count, 2);
		setLayout(grid);
		
		//setup buttons
		for(int i = 0; i < count; i++) {
			JButton btn = new JButton("Cancel File #" + count);
			btn.addActionListener(this);
			buttons[i] = btn;
		}//end for
		
		//setup progress bars
		for(int i = 0; i < count; i++) {
			JProgressBar prog = new JProgressBar();
			bars[i] = prog;
		}//end for
		
		//add the components
		for(int i = 0; i < count; i++) {
			add(bars[i]);
			add(buttons[i]);
		}//end for
		
	}//end constructor

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
