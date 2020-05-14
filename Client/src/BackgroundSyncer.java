
public class BackgroundSyncer extends Thread{
	
	private boolean sync;
	private int sleepTime = 20000;
	
	/*
	 * Constructor sets up the syncer
	 */
	public BackgroundSyncer() {
		sync = false;
		start();
	}//end constructor
	
	/*
	 * The run method for the thread
	 */
	public void run() {
		while(true) {
			try {
				sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if(sync) {
				try {
					Client.getTwoWay().sync();
					sleep(sleepTime);
				} catch (Exception e) {
					e.printStackTrace();
				}//end catch
			}//end if
		}//end run loop
	}//end run

	/*
	 * Enable the background sync
	 */
	public void enable() {
		sync = true;
	}//end enable
	
	/*
	 * Disable the background sync
	 */
	public void disable() {
		sync = false;
	}//end disable
	
}//end BackgroundSyncer
