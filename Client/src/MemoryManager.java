import java.awt.GridLayout;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class MemoryManager extends JFrame{
	
	private GridLayout grid;
	private JProgressBar bar;
	private OutputWin con;
	
	
	public MemoryManager() {
		//housekeeping
		setSize(400, 400);
		setTitle("Memory");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		grid = new GridLayout(2, 1);
		setLayout(grid);
		
		//bar
		bar = new JProgressBar();
		add(bar);
		
		//Console
		try {
			con = new OutputWin();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		add(con);
		
		setVisible(true);
		
		//loop
		while(true) {
			//get memory
			long total = Runtime.getRuntime().totalMemory();
			long used = total - Runtime.getRuntime().freeMemory();
			
			//set bars
			bar.setMaximum((int)total);
			bar.setValue((int)used);
			
			//Update console
			con.addOuput(Long.toString(used) + "/" + Long.toString(total));
			
			Delay d = new Delay(100);
			while(!d.delay());
			
		}//end loop
		
	}//end constructor

}//end MemoryManager
