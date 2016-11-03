package searching;

public interface UltrasonicController {
	
	public void processUSData(int distance, boolean isSensorForward, int count);
	
	
	
	public int readUSDistance();
}
