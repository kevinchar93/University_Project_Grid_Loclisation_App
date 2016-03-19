package core;

import java.util.concurrent.*;

import processing.core.PApplet;
import processing.serial.Serial;
import robot.*;

public class CoreUI extends PApplet {
	
	public void settings() {
		
	}
	
	public void setup() {
		
		Robot robot = new Robot(this, 10, 10, 10.0f, serialData);
		robot.connect("/dev/tty.HC-05-DevB", 9600);
		
		robot.isMovePossible(4, Direction.RIGHT);
	}
	
	public void draw() {
		
	}
	
	public void drawDistribution() {
		
	}
	
	/* Information related to string building and queue creation */
	private BlockingQueue<String> serialData = new LinkedBlockingQueue<>(25);
	private StringBuffer bufferString;
	private boolean finishStrBuild = true;
	
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
