package searching;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import java.util.ArrayList;

//this class will be used for localization AND object detection.
//only start object detection once localization is completed.
public class USLocalizer extends Thread implements UltrasonicController {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 60;
	public static int acceleration = 600;
	public static int WALL_DISTANCE = 40;
	public static int WALL_DISTANCE_FACING = 45;
	public static int FILTER_THRESHOLD = 100;
	public static int OBJECT_DETECTION_THRESHOLD = 30;
	int highSpeed = 200;
	int lowSpeed = 100;
	int bandCenter = 15;
	int bandWidth = 2;
	public static boolean isComplete = false;
	public static boolean isCloseToObject = false;
	private double deltaTheta;
	private int initialDistance = 0;
	private int distance;
	private Odometer odo;
	private Navigation nav;
	private SampleProvider usSensor;
	private float[] usData;
	private float[] usSample;
	private int usIndexCounter = 0;
	private LocalizationType locType;

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	public void run(){
		doLocalization();
	}

	public USLocalizer(
			Odometer odo,  
			Navigation nav,
			SampleProvider usSensor, 
			float[] usData, 
			LocalizationType locType, 
			EV3LargeRegulatedMotor leftMotor, 
			EV3LargeRegulatedMotor rightMotor ) {
		this.odo = odo;
		this.nav = nav;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		//getting motors from odometer
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usSample = new float[]{0, 0};

		leftMotor.setAcceleration(acceleration);
		rightMotor.setAcceleration(acceleration);
	}

	public void doLocalization() {
		
		double [] pos = new double [3];
		double angleA, angleB;

		//set speed of motor for rotation

		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);	
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			
			Delay.msDelay(100);
			while(this.initialDistance < 5){
				this.initialDistance = this.distance;
				LCD.clear(7);
				LCD.drawString("Init: " + Integer.toString(this.initialDistance), 0, 7);
			}
			
			//RISING EDGE: robot starts away from the wall, start detecting walls right away 
			if(this.initialDistance > WALL_DISTANCE){
				
				while(true){
					//keep rotating clockwise until it sees a wall
					turnClockwise();
					//if it does, record the angle and stop
					if(this.usSample[0] < WALL_DISTANCE && //this.usSample[1] < WALL_DISTANCE &&
							 this.usSample[0] > this.usSample[1]
									 &&this.usSample[0] != 0 && this.usSample[1] != 0			 
							){
						angleA = Odometer.fixDegAngle(odo.getAng());
						LCD.drawString("ang1: " + Double.toString(angleA), 0, 5);
						Button.LEDPattern(1);
						Sound.beep();
						break;
					}
				}
				//reset the us data before tryign to find the other wall
				this.usSample = new float[]{0,0};
				//then start turning the other way
				turnCounterClockwise();
				Delay.msDelay(3000);
				Button.LEDPattern(0);
				//turn ccw until the next wall is found
				while(true){
					turnCounterClockwise();
					if(this.usSample[0] < WALL_DISTANCE && //this.usSample[1] < WALL_DISTANCE &&
							 this.usSample[0] > this.usSample[1]
							&&this.usSample[0] != 0 && this.usSample[1] != 0		 
							){
						angleB = Odometer.fixDegAngle(odo.getAng());
						LCD.drawString("ang2: " + Double.toString(angleB), 0, 6);
						Button.LEDPattern(1);
						Sound.beep();
						rightMotor.stop(true);
						leftMotor.stop(true);
						break;
					}
				}
			} else {
				//FALLING EDGE: robot starts facing a wall
				
				//rotate the robot until it's away from a wall, then stat detecting walls
				while(true){
					turnClockwise();
					if(this.usSample[0] > WALL_DISTANCE && this.usSample[1] > WALL_DISTANCE){
						this.usSample = new float[]{0,0};
						break;
					}
						
				}
				//rotate clockwise until the robot sees a wall
				while(true){
					turnClockwise();
					//if it does, record the angle and stop
					if(this.usSample[0] < WALL_DISTANCE && this.usSample[1] < WALL_DISTANCE_FACING
							&& this.usSample[0] != 0 && this.usSample[1] != 0
							&& this.usSample[0] < this.usSample[1]){
						angleA = Odometer.fixDegAngle(odo.getAng());
						//LCD.drawString("ang1: " + Double.toString(angleA), 0, 5);
						Sound.beep();
						break;
					}
				}
				//reset the us data
				this.usSample = new float[]{0,0};
				//then start turning the other way
				turnCounterClockwise();
				Delay.msDelay(2000);
				
				//turn ccw until the next wall is found
				while(true){
					turnCounterClockwise();
					if(this.usSample[0] < WALL_DISTANCE_FACING && this.usSample[1] < WALL_DISTANCE_FACING
							&& this.usSample[0] < this.usSample[1]
									 &&this.usSample[0] != 0 && this.usSample[1] != 0	
									 ){
						angleB = Odometer.fixDegAngle(odo.getAng());
						//LCD.drawString("ang2: " + Double.toString(angleB), 0, 6);
						Sound.beep();
						Button.LEDPattern(1);
						rightMotor.stop(true);
						leftMotor.stop(true);
						break;
					}
				}
			}
			
			Button.LEDPattern(0);
			double aveAngle = (angleA + angleB)/2;

			if(angleA < angleB){
				deltaTheta = 45 - aveAngle;

			}

			else if (angleA > angleB){
				deltaTheta = 225 - aveAngle;
			}

			double newAngle = Odometer.fixDegAngle( deltaTheta+ odo.getAng());
			LCD.clear(7);
			LCD.drawString("newAng: " + Double.toString(newAngle), 0, 7);
			if(this.initialDistance < WALL_DISTANCE){
				turnTo(newAngle);
				leftMotor.stop(true);
				rightMotor.stop(true);
			}
			else {
				nav.turnTo(newAngle, true);
				leftMotor.stop(true);
				rightMotor.stop(true);
			}
			Delay.msDelay(4000);
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			leftMotor.stop(true);
			rightMotor.stop(true);
			Delay.msDelay(200);
			LCD.clear(7);
//			LCD.drawString("TURNING TO 45", 0, 7);
//			nav.turnTo(315, true);
//			Delay.msDelay(5000);
			Sound.playTone(4000, 500);
			isComplete = true;
			while(true){
				if(this.distance < OBJECT_DETECTION_THRESHOLD){
					this.isCloseToObject = true;
					LCD.clear(7);
					//Button.LEDPattern(1);
					LCD.drawString("OBJECT DETECTED", 0, 7);
				} 
				//the object finder class will change the value after it decides
				//what to do with the object. when it does do
				//something else
				//light sensor distance can be inside us deadband, so can't run
				//both at the same time and expect good results
				
			}

		} else {
			//nothing implemented, option chosen in main class is always the same and rising or falljng is selected based on initial distance
					}
	}

	//LOCALIZATION CONTROLS
	public void turnClockwise(){
		leftMotor.forward();
		rightMotor.backward();
	}

	public void turnCounterClockwise(){
		leftMotor.backward();
		rightMotor.forward();
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	private void turnTo(double theta){

		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);

		leftMotor.rotate(convertAngle(odo.leftRadius, odo.width, theta), true);
		rightMotor.rotate(-convertAngle(odo.leftRadius, odo.width, theta), false);


	}

	//OBSTACLE AVOIDANCE CONTROLS
	private void bangbangAvoidance(){
		if(distance < bandCenter - bandWidth){
			turnRight();
		} else if (distance > bandCenter + bandWidth){
			turnLeft();
		} else{
			moveForward();
		}
	}
	
	private void turnLeft(){
//		navigator.leftMotor.stop();
//		navigator.rightMotor.stop();
		leftMotor.setSpeed(lowSpeed);
		rightMotor.setSpeed(highSpeed);
		leftMotor.forward();
		rightMotor.forward();
	}

	private void turnRight(){
		
		leftMotor.setSpeed(highSpeed);
		rightMotor.setSpeed(lowSpeed);
		leftMotor.forward();
		rightMotor.forward();
	}

	private void moveForward(){
		
		leftMotor.setSpeed(highSpeed);
		rightMotor.setSpeed(highSpeed);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	@Override
	public void processUSData(int distance, boolean isSensorForward, int count){
//		if(distance == Integer.MAX_VALUE )
//			distance = 255;
		if(distance > FILTER_THRESHOLD)
			distance = FILTER_THRESHOLD;
		
		this.distance = distance;
		float tmp = this.usSample[0];
		this.usSample[1] = tmp;
		this.usSample[0] = distance;
	}


	@Override
	public int readUSDistance(){
		return this.distance;
	}
	


	
}
