package searching;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

//this thread is for detecting blocks for part 1
public class BlockDetector extends Thread implements ColorController {
	
	public static float[] color = new float[]{0,0,0};
	
	
	public void run(){
		while(true){
			//styro block detection is based on the blue value from the rgb sensor
			if(/*color[0] > BLOCK_COLOR && color[1] > BLOCK_COLOR &&*/color[2] > ObjectFinder.BLOCK_COLOR ){
			
				LCD.clear();
				Sound.playTone(4000, 100);
				Delay.msDelay(50);
				Sound.playTone(4000, 100);
				Button.LEDPattern(7);
				LCD.clear(7);
				LCD.drawString("BLOCK FOUND!!!", 0, 7);
				
				Delay.msDelay(1000);
				//other object is detected if there are non-zero values that don't correspond to the block
			} else if (color[0] != 0 || color[1] != 0 || color[2] != 0){
				//do some bang bang controller avoiding
				Sound.beep();
				LCD.drawString("NO BLOCK :((((", 0, 7);
				Button.LEDPattern(9);
				Delay.msDelay(1000);
			}
		}
	}
	
	@Override
	public void processColorData(int[] color) {
		for(int i = 0; i < color.length; i++){
			this.color[i] = color[i];
		}

	}

	@Override
	public int[] readColorData() {
		// TODO Auto-generated method stub
		return null;
	}

}
