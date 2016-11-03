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
	public static final float BLOCK_COLOR = 3;
	public static final int BLOCK_DISTANCE = 5;
	public static final int LOST_BLOCK_DISTANCE = 50;
	public static final int SWEEP_ANGLE = 10;
	public static boolean foundBlock;
	private int[] color = new int[3];
	public static boolean isBlockFound = false;
	private Navigation nav;
	private Odometer odo;
	private USLocalizer usl;
	private double objectAngle, objectDistance;

	public ObjectFinder(Odometer odo, Navigation nav, USLocalizer usl){
		this.odo = odo;
		this.nav = nav;
		this.usl = usl;
	}
	public void run(){
		findObject();
	}

	//this thread is to find objects while the robot is moving
	public void findObject(){
		mainLoop:
		while(true){
			if(usl.distance < USLocalizer.OBJECT_DETECTION_THRESHOLD && usl.isComplete && usl.objectDetectionCounter > 5){
				nav.leftMotor.stop(true);
				nav.rightMotor.stop(true);
				int objectDistance = usl.distance;
				this.objectAngle = odo.getAng();
				LCD.clear(7);
				//Button.LEDPattern(1);
				LCD.drawString("OBJECT DETECTED", 0, 7);
				
				USLocalizer.isCloseToObject = true;
				Delay.msDelay(1000);
				LCD.clear(7);
				LCD.drawString("TURNING TO OBJECT", 0, 7);
				nav.turnTo(objectAngle, true);
				LCD.clear(7);
				LCD.drawString("TURNED TO OBJECT", 0, 7);

				nav.leftMotor.setSpeed(Navigation.SLOW);
				nav.rightMotor.setSpeed(Navigation.SLOW);
				nav.leftMotor.rotate(nav.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, 30), true);
				nav.rightMotor.rotate(-nav.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, 30), false);
				Delay.msDelay(100);
				movingToBlockLoop:
				while(usl.distance > BLOCK_DISTANCE){
					LCD.clear(7);
					LCD.drawString("MOVING TO OBJECT", 0, 7);
					int sweepCounter = 1;
					//if the robot sees an object in its vicinity, sweep to adjust its heading until the object is in front of it again.
					outerloop:
					while(usl.distance > LOST_BLOCK_DISTANCE){
						for(int i =0; i < sweepCounter; i++){
							nav.leftMotor.rotate(nav.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, SWEEP_ANGLE), true);
							nav.rightMotor.rotate(-nav.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, SWEEP_ANGLE), false);
							if(usl.distance < LOST_BLOCK_DISTANCE){
								break outerloop;
							}
						}
						//THIS PART ACTUALLY WORKS BUT SENSOR IS NOT WORKING
						for(int i = 0; i < 2*sweepCounter; i++){
							nav.leftMotor.rotate(-nav.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, SWEEP_ANGLE), true);
							nav.rightMotor.rotate(nav.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, SWEEP_ANGLE), false);
							if(usl.distance < LOST_BLOCK_DISTANCE){
								break outerloop;
							}
						}
						sweepCounter++;
					}

					nav.leftMotor.forward();
					nav.rightMotor.forward();
				}
				nav.leftMotor.stop(true);
				nav.rightMotor.stop(true);
				Delay.msDelay(500);
				
				int noBlockCount = 0;
				//object identification loop:
				while(true){
					if(/*color[0] > BLOCK_COLOR && color[1] > BLOCK_COLOR &&*/color[2] > BLOCK_COLOR ){
						LCD.clear();
						Sound.playTone(4000, 100);
						Delay.msDelay(50);
						Sound.playTone(4000, 100);
						Button.LEDPattern(7);
						LCD.clear(7);
						LCD.drawString("BLOCK FOUND!!!", 0, 7);
						isBlockFound = true;
						Delay.msDelay(1000);
						break mainLoop;
					} else if (color[0] != 0 || color[1] != 0 || color[2] != 0){
						//do some bang bang controller avoiding
						Sound.beep();
						LCD.clear(7);
						LCD.drawString("NO BLOCK :((((", 0, 7);
						Button.LEDPattern(9);
						noBlockCount++;
						Delay.msDelay(1000);
					}
					if(noBlockCount > 2){
						LCD.clear(7);
						LCD.drawString("AVOIDING OBSTACLE", 0, 7);
						nav.moveBackwards();
						Delay.msDelay(2000);
						//nav.leftMotor.stop(true);
						//nav.rightMotor.stop(true);
						usl.objectDetectionCounter = 0;
						USLocalizer.isCloseToObject = false;
						break;
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
