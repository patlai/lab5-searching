package searching;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.Navigator;
import lejos.utility.Delay;
import java.util.Arrays;

public class ObjectFinder extends Thread implements ColorController{
	private static final float BLOCK_COLOR = 3;
	private int[] color = new int[3];
	public boolean isBlockFound = false;
	private Navigation nav;
	private Odometer odo;
	private USLocalizer usl;
	private double objectAngle;

	public ObjectFinder(Odometer odo, Navigation nav, USLocalizer usl){
		this.odo = odo;
		this.nav = nav;
		this.usl = usl;
	}
	public void run(){
		findObject();
	}

	public void findObject(){
		while(true){
			if(usl.distance < USLocalizer.OBJECT_DETECTION_THRESHOLD && usl.isComplete){
				USLocalizer.isCloseToObject = true;
				LCD.clear(7);
				//Button.LEDPattern(1);
				LCD.drawString("OBJECT DETECTED", 0, 7);
				while(true){
					if(/*color[0] > BLOCK_COLOR && color[1] > BLOCK_COLOR &&*/color[2] > BLOCK_COLOR ){
						this.objectAngle = odo.getAng();
						LCD.clear();
						Sound.playTone(4000, 100);
						Delay.msDelay(50);
						Sound.playTone(4000, 100);
						Button.LEDPattern(7);
						LCD.clear(7);
						LCD.drawString("BLOCK FOUND!!!", 0, 7);
						this.isBlockFound = true;
						Delay.msDelay(1000);
					} else if (color[0] != 0 || color[1] != 0 || color[2] != 0){
						//do some bang bang controller avoiding
						this.objectAngle = odo.getAng();
						Sound.beep();
						Button.LEDPattern(9);
						Delay.msDelay(1000);
					}
				}
			}
			Button.LEDPattern(0);
		}
	}

	public void processColorData(int[] color){
		for(int i = 0; i < color.length; i++){
			this.color[i] = color[i];
		}
	}




	public int[] readColorData(){
		return this.color;
	}
}
