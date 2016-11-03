package searching;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;

public class Claw extends Thread {
	private EV3LargeRegulatedMotor clawMotor;
	private Navigation nav;
	private final int CLAW_SPEED = 200;
	public static boolean isClawDown = false;
	public Claw(EV3LargeRegulatedMotor clawMotor, Navigation nav){
		this.clawMotor = clawMotor;
		this.nav = nav;
	}
	public void run(){
		while(true){
			if(ObjectFinder.isBlockFound && !isClawDown){
				moveClaw();
				Delay.msDelay(500);
				nav.travelTo(65, 65);
			}
		}
		
		
	}
	private void moveClaw(){
		clawMotor.rotate(180);
		isClawDown = true;
	}
	public void resetClaw(){
		clawMotor.rotate(-180);
		isClawDown = false;
	}
}
