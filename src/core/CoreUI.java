package core;

import java.util.concurrent.*;

import processing.core.PApplet;
import processing.serial.Serial;
import robot.*;

public class CoreUI extends PApplet {
	
	/* Details about the environment the robot is in */
	private int Grid_Size_MM = 300;
	private int Wall_Distance_MM = 180;
	private float Wall_Threshold_Percent = 1.2f;
	/* ----------------------------------------------*/
	
	public void settings() {
		
	}
	
	public void setup() {
		
		Robot robot = new Robot(this, Grid_Size_MM, Wall_Distance_MM, Wall_Threshold_Percent, serialData);
		robot.connect("/dev/tty.HC-05-DevB", 9600);
		
		System.out.println(robot.checkForDoor(Direction.RIGHT));
	}
	
	public void draw() {
		
	}
	
	public void drawDistribution() {
		
	}
	
	/* Information related to string building from the serial port and queue creation */
	private BlockingQueue<String> serialData = new LinkedBlockingQueue<>(25);
	private StringBuffer bufferString;
	private boolean finishStrBuild = true;
	/* -------------------------------------------------------------------------------*/
	
	public void serialEvent(Serial port) {
		
		char tempChar;
		
		if (true == finishStrBuild) {
			bufferString = new StringBuffer();
			finishStrBuild = false;
		}
		
		if (port.available() > 0){
			
			tempChar = port.readChar();
			
			if (';' == tempChar){
				finishStrBuild = true;
				serialData.offer(PApplet.trim(bufferString.toString()));
			}
			else {
				bufferString.append(tempChar);
			}
		}
	}

}
