package searching;

import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;

public class ColorPoller extends Thread {
	private SampleProvider colorProvider;
	private float[] colorData;
	private ColorController cont;
	private int reading;
	public ColorPoller(SampleProvider colorProvider, float[] colorData, ColorController cont){
		this.colorProvider = colorProvider;
		this.colorData = colorData;
		this.cont = cont;
	}
	
	
		public void run() {
			//reading goes from 0 to 100 where 0 is dark and 100 is light
			int[] reading = new int[3];
			while (true) {
				
				
				colorProvider.fetchSample(colorData,0);							
//				reading= (int)(colorData[0]*100.0);
				
				
				for(int i = 0; i < 3; i++){
					reading[i] = (int)(colorData[i]*100.0);
				}
				
				LCD.clear(4);
				LCD.clear(5);
				LCD.clear(6);
				LCD.drawString("Color: " + Float.toString(reading[0]), 0, 4);
				LCD.drawString("Color: " + Float.toString(reading[1]), 0, 5);
				LCD.drawString("Color: " + Float.toString(reading[2]), 0, 6);

				
				cont.processColorData(reading);		
				// now take action depending on value
				try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
			}
		}
	
}
