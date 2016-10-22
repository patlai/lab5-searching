package searching;
import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

public class Lab5 {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3MediumRegulatedMotor usMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final Port colorPort = LocalEV3.get().getPort("S1");	
	private static final Port usPort = LocalEV3.get().getPort("S2");		


	public static void main(String[] args) {

		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned

		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("RGB");		// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned

		//use threads for both sensors to have them poll continuosly
		
		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		Navigation nav = new Navigation(odo);
		LCDInfo lcd = new LCDInfo(odo);



		// perform the ultrasonic localization
		//note: localization type is always set to falling but the robot will set its own type based on the distance it starts from the wall.
		USLocalizer usl = new USLocalizer(odo, nav, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE, leftMotor, rightMotor);
		UltrasonicPoller usPoller = new UltrasonicPoller(usValue, usData, usl);
		usPoller.start();
		usl.start();
		
		ObjectFinder finder = new ObjectFinder();
		ColorPoller cPoller = new ColorPoller(colorValue, colorData, finder); 
		cPoller.start();
		finder.start();
		//lsl.start();
		//lsl.doLocalization();			

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	

	}

}
