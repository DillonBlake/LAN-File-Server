
/**
 * This class is used to delay processes within the communication structure
 */

public class Delay extends Thread{
	
	private final int defaultTime = 50;
	private int time;
	
	/**
	 * Default constructor sets the delay time to the default
	 */
	public Delay() {
		time = defaultTime;
	}//end constructor

	/**
	 * Constructor sets delay time to the parameter
	 * @param t the delay time
	 */
	public Delay(int t) {
		time = t;
	}//end constructor
	
	/**
	 * Sleep for this time and return true when done
	 * @return true when delay is complete
	 */
	public boolean delay() {
		try {
			sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}//end delay
}//end Delay
