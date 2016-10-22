package searching;
import lejos.robotics.SampleProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import java.util.ArrayList;

//
//  Control of the wall follower is applied periodically by the 
//  UltrasonicPoller thread.  The while loop at the bottom executes
//  in a loop.  Assuming that the us.fetchSample, and cont.processUSData
//  methods operate in about 20mS, and that the thread sleeps for
//  50 mS at the end of each loop, then one cycle through the loop
//  is approximately 70 mS.  This corresponds to a sampling rate
//  of 1/70mS or about 14 Hz.
//


public class UltrasonicPoller extends Thread{
	private SampleProvider us;
	private UltrasonicController cont;
	public int distance;
	private float[] usData;
	public ArrayList<Integer> usDistances = new ArrayList<Integer>();
	
	public UltrasonicPoller(SampleProvider us, float[] usData, UltrasonicController cont) {
		this.us = us;
		this.cont = cont;
		this.usData = usData;
	}

//  Sensors now return floats using a uniform protocol.
//  Need to convert US result to an integer [0,255]
	
	public void run() {
		int distance;
		while (true) {
			
			
			us.fetchSample(usData,0);							
			distance=(int)(usData[0]*100.0);
			if(distance == Integer.MAX_VALUE)
				distance = 255;
			this.distance = distance;
			if(usDistances.size() > 20)
				usDistances.remove(0);
			usDistances.add(distance);
			LCD.clear(3);
			LCD.drawString("US Dist.: " + Integer.toString(distance), 0, 3);
			cont.processUSData(distance, true , 0);		
			// now take action depending on value
			try { Thread.sleep(10); } catch(Exception e){}		// Poor man's timed sampling
		}
	}

}
