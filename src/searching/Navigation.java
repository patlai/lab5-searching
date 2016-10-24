package searching;
/*

 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;

public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 4000;
	final static double DEG_ERR = 5.0, CM_ERR = 1.0;
	private Odometer odometer;
	public  EV3LargeRegulatedMotor leftMotor, rightMotor;
	boolean isEmergency = false;
	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		leftMotor = motors[0];
		rightMotor = motors[1];

		// set acceleration
		leftMotor.setAcceleration(ACCELERATION);
		rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		leftMotor.setSpeed(lSpd);
		rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			leftMotor.backward();
		else
			leftMotor.forward();
		if (rSpd < 0)
			rightMotor.backward();
		else
			rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		leftMotor.setSpeed(lSpd);
		rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			leftMotor.backward();
		else
			leftMotor.forward();
		if (rSpd < 0)
			rightMotor.backward();
		else
			rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.flt(true);
		rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		double minAng;
		//are these defualt error conditions too bad?
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng, false);
			//if there is a block detected, stop going straight and
//			if(USLocalizer.isCloseToObject){
//				break;
//			}
			setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng())) * distance, Math.cos(Math.toRadians(this.odometer.getAng())) * distance);

	}
	
private void moveForward(){
		
		setSpeeds(FAST, FAST);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	
	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	public void scanForObject(){
		//turn 90 degrees one way and then back the other way to look for blocks
		
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		
		leftMotor.rotate(Navigation.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, 90), true);
		rightMotor.rotate(Navigation.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, -90), false);
		Delay.msDelay(500);
		leftMotor.rotate(Navigation.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, -90), true);
		rightMotor.rotate(Navigation.convertAngle(Lab5.WHEEL_RADIUS, Lab5.TRACK, 90), false);
	}
}
