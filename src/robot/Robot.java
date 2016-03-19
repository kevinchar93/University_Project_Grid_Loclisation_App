package robot;

import java.util.concurrent.BlockingQueue;

import processing.core.PApplet;
import processing.serial.*;

public class Robot {

	private Serial _port;
	private int _gridSize;
	private int _wallDistance;
	private PApplet _parent;
	BlockingQueue<String> _serialData;
	
	private final String INS_OK = "INS_OK";
	private final String INS_ERR = "INS_ERR";
	private final String INS_DONE = "INS_DONE";
	private final String INS_DATA_AVAIL = "INS_DATA_AVAIL";
	private final String INS_ERR_CONNECT = "INS_ERR_CONNECT";
	
	public Robot (PApplet parent, int gridSize, int wallDistance, float threshold, BlockingQueue<String> serialData) {
		_parent = parent;
		_gridSize = gridSize;
		_wallDistance = wallDistance;
		_serialData = serialData;
	}
	
	
	public boolean connect (String port, int baudRate) {
		
		final int TIMEOUT_VAL = 10000;
		boolean connected = false;
		String readStr;
		int beginTime = _parent.millis();

		final String responseSignal = "S";
		final String stageSignalA = "A";
		final String stageSignalB = "B";
		
		try {
			_port = new Serial(_parent, port, baudRate);
		}
		catch (Exception e) {
			PApplet.println("Failed to open port");
			return false;
		}

		while (!connected) {

			if ((_parent.millis() - beginTime) > TIMEOUT_VAL) {
				PApplet.println("Connection attempt timed out");
				return false;
			}
			
			try {
				readStr = _serialData.take();
				readStr = PApplet.trim(readStr);
			} catch (InterruptedException e) {
				return false;
			}
			

			if (stageSignalA.equals(readStr)) {
				_port.write(responseSignal);
				_port.clear();
			}
			else if (stageSignalB.equals(readStr)) {
				connected = true;
			}
			
			_parent.delay(100);
		}
		
		_port.clear();
		_serialData.clear();
		PApplet.println("Connected");
		return true;
	}
	
	
	public boolean isConnected() {
		return _port.active();
	}

	
	public boolean move (int gridSpaces, Direction dir, boolean isCyclic) {
		
		Instruction instruction = null;
		InstructionSet instructionType = InstructionSet.ERROR;
		
		// check to make sure we have Bluetooth connection to robot
		if (!isConnected()) {
			PApplet.println("Can't send command no connection");
			return false;
		}
		
		// check if the move is possible
		if (!isCyclic && !isMovePossible(gridSpaces, dir)) {
			PApplet.println("Failed to move, not enough space in the map to move into");
			return false;
		}
		
		// create the instruction object
		switch (dir) {
		
			case LEFT :
				instructionType = InstructionSet.MOVE_FORWARD;
				break;
				
			case RIGHT:
				instructionType = InstructionSet.MOVE_BACKWARD;
				break;
		}
		
		instruction = new Instruction(instructionType, gridSpaces, true, _gridSize);
		
		_port.write(instruction.toString());
		
		// get verification response
		String response = getResponse();
		if (INS_OK.equals(response)) {
			
			// get completion response
			response = getResponse();
			if (INS_DONE.equals(response)) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getResponse() {
		
		// check to make sure we have Bluetooth connection to robot
		if (!isConnected()) {
			PApplet.println("Can't send command no connection");
			return INS_ERR_CONNECT;
		}
		
		StringBuffer buffer = new StringBuffer();
		boolean 	 keepBuilding = true;
		char 		 currChar;
		
		// keep looping until the end of string character
		while (keepBuilding) {
			
			// if the port has data read the character, and check for the EOS char
			if (_port.available() > 0) {
				currChar = _port.readChar();
				if (';' == currChar) {
					keepBuilding = false;
				}
				else {
					buffer.append(currChar);
				}
			}
		}
		
		return buffer.toString();
	}
	
	public boolean isMovePossible (int gridSpaces, Direction dir) {
		
		// check to make sure we have Bluetooth connection to robot
		if (!isConnected()) {
			PApplet.println("Can't send command no connection");
			return false;
		}
		
		// calculate the distance of the grid spaces we need to move
		int distanceToTravel = _gridSize * gridSpaces;
		int measurementHeading = 0;
		Instruction instruction;
		
		// get the distance left of the map in the given direction
		// create the instruction object
		switch (dir) {
		
			case LEFT :
				// left so get distance in front, 0 degrees
				measurementHeading = 0;
				break;
				
			case RIGHT:
				// right so get distance behind, 180 degrees
				measurementHeading = 180;
				break;
		}
		
		instruction = new Instruction(InstructionSet.MOVE_FORWARD, 35, false, 424);
		
		_port.write(instruction.toString());
		
		// get verification response
		String response;
		try {
			response = _serialData.take();
		} catch (InterruptedException e) {
			return false;
		}
		PApplet.println(response);
		if (INS_OK.equals(response)) {
			
			// check for completion, a signal saying data is ready to be read
			try {
				response = _serialData.take();
			} catch (InterruptedException e) {
				return false;
			}
			PApplet.println(response);
			
			if (INS_DATA_AVAIL.equals(response)) {
				
				// read the data created by the robot
				try {
					response = _serialData.take();
				} catch (InterruptedException e) {
					return false;
				}
				PApplet.println(response);
				
				// parse the data and compare against distanceToTravel
				//System.out.println(response);
				
			}
		}
		
		// if the grid space distance is greater than the space available can't move
		
		return false;
	}
	
	public boolean checkForDoor(Direction sideDoorIsOn) {
		
		// take a measurement at the given heading
		
		// check to see if measurement is over the threshold
		
		// if the measurement is over the threshold a door is present
		
		return false;
	}

}

 ;