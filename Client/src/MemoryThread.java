
public class MemoryThread extends Thread{

	public MemoryThread() {
		start();
	}
	
	public void run() {
		MemoryManager m = new MemoryManager();
	}
}
