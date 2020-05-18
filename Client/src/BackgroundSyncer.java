/**
 * This class runs a background task to sync on a given time interval
 */

public class BackgroundSyncer extends Thread{
	
	private boolean sync;
	private int sleepTime = 60 * 10 * 1000; //set to 10 minutes
	
	/**
	 * Constructor sets up the syncer. It will not sync until enabled
	 */
	public BackgroundSyncer() {
		sync = false;
		start();
	}//end constructor
	
	/**
	 * The run method for the thread
	 */
	public void run() {
		while(true) {
			//small delay to start
			try {
				sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			//check if should sync and if main delay should be triggered
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

	/**
	 * Enable the background sync
	 */
	public void enable() {
		sync = true;
	}//end enable
	
	/**
	 * Disable the background sync
	 */
	public void disable() {
		sync = false;
	}//end disable
	
}//end BackgroundSyncer
