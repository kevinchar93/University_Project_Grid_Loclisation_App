package robot;

import java.util.concurrent.BlockingQueue;

import processing.core.PApplet;
import processing.serial.*;

public class Robot {

	private Serial _port;
	private int _gridSize;
	private int _wallDistance;
	private float _wallThreshold;
	private PApplet _parent;
	private boolean _connected = false;
	BlockingQueue<String> _serialData;
	
	private final String INS_OK = "INS_OK";
	private final String INS_ERR = "INS_ERR";
	private final String INS_DONE = "INS_DONE";
	private final String INS_DATA_AVAIL = "INS_DATA_AVAIL";
	private final String INS_ERR_CONNECT = "INS_ERR_CONNECT";
	
	public Robot (PApplet parent,  BlockingQueue<String> serialData) {
		_parent = parent;
		_serialData = serialData;
	}
	
	public void init(int gridSize, int wallDistance, float threshold) {
		_gridSize = gridSize;
		_wallDistance = wallDistance;
		_wallThreshold = threshold;
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
			System.out.println("Failed to open port");
			return false;
		}

		while (!connected) {

			if ((_parent.millis() - beginTime) > TIMEOUT_VAL) {
				System.out.println("Connection attempt timed out");
				return false;
			}
			
			try {
				readStr = _serialData.take();
			} catch (InterruptedException e) {
				System.out.println("Connection error: InterruptedException");
				return false;
			}
			

			if (stageSignalA.equals(readStr)) {
				_port.write(responseSignal);
				_port.clear();
			}
			else if (stageSignalB.equals(readStr)) {
				connected = true;
			}
		}
		
		_port.clear();
		_serialData.clear();
		_connected = true;
		System.out.println("Connected");
		return true;
	}
	
	
	public boolean isConnected() {
		if (false == _connected)
		{
			return false;
		}
		return _port.active();
	}

	
	public boolean move (int gridSpaces, Direction dir, boolean isCyclic) {
		
		Instruction instruction = null;
		InstructionSet instructionType = InstructionSet.ERROR;
		
		// check to make sure we have Bluetooth connection to robot
		if (!isConnected()) {
			System.out.println("Can't send command no connection");
			return false;
		}
		
		// check if the move is possible
		if (!isCyclic && !isMovePossible(gridSpaces, dir)) {
			System.out.println("Failed to move, not enough space in the map to move into");
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
		String response;
		try {
			response = _serialData.take();
		} catch (InterruptedException e) {
			System.out.println("Connection error: InterruptedException");
			return false;
		}
		
		if (INS_OK.equals(response)) {
			
			// get completion response
			try {
				response = _serialData.take();
			} catch (InterruptedException e) {
				System.out.println("Connection error: InterruptedException");
				return false;
			}
			
			if (INS_DONE.equals(response)) {
				return true;
			}
		}
		return false;
	}
	
	
	private boolean isMovePossible (int gridSpaces, Direction dir) {
		
		// check to make sure we have Bluetooth connection to robot
		if (!isConnected()) {
			System.out.println("Can't send command no connection");
			return false;
		}
		
		// calculate the distance of the grid spaces we need to move
		final int DIST_TO_TRAVEL = _gridSize * gridSpaces;
		int measurementHeading = 0;
		Instruction instruction;
		
		// get the distance remaining of the map in the given direction
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

		// create the instruction object
		instruction = new Instruction(InstructionSet.LIDAR_AT_ANGLE, measurementHeading, false, 0);
		
		_port.write(instruction.toString());
		
		// get verification response
		String response;
		try {
			response = _serialData.take();
		} catch (InterruptedException e) {
			System.out.println("Connection error: InterruptedException");
			return false;
		}

		if (INS_OK.equals(response)) {
			
			// check for completion which is a signal saying data is ready to be read
			try {
				response = _serialData.take();
			} catch (InterruptedException e) {
				System.out.println("Connection error: InterruptedException");
				return false;
			}
			
			if (INS_DATA_AVAIL.equals(response)) {
				
				// read the data created by the robot
				try {
					response = _serialData.take();
				} catch (InterruptedException e) {
					System.out.println("Connection error: InterruptedException");
					return false;
				}
				
				// parse the data and compare against distanceToTravel
				String[] measurement = PApplet.splitTokens(response, ":"); 
				int distAvail = Integer.parseInt(measurement[0]);
				float heading = Float.parseFloat(measurement[1]);
				
				// if distance we want to travel is more than distance available, we cannot move
				if (DIST_TO_TRAVEL > distAvail) {
					return false;
				}
				else {
					// otherwise the move is possible
					return true;
				}
			}
		}
		
		// did not receive and okay or data available message , return false
		return false;
	}
	
	
	public String checkForDoor(Direction sideDoorIsOn) {
		
		int measurementHeading = 0;
		final String DOOR_DETECTED = "DOOR";
		final String NO_DOOR_DETECTED = "NO_DOOR";
		final String ERROR = "ERROR";
		
		
		// take a measurement at the side the door is on
		switch (sideDoorIsOn) {
		
			case LEFT :
				// left so get distance on left side, 270 degrees
				measurementHeading = 270;
				break;
				
			case RIGHT:
				// right so get distance right, 90 degrees
				measurementHeading = 90;
				break;
		}

		// create and send instruction
		Instruction instruction = new Instruction(InstructionSet.LIDAR_AT_ANGLE, measurementHeading, false, 0);
		_port.write(instruction.toString());
		
		// get response
		String response;
		try {
			response = _serialData.take();
		} catch (InterruptedException e) {
			return ERROR+"Fail to get okay response";
		}

		// check message was received okay and is valid
		if (INS_OK.equals(response)) {
			
			// get response
			try {
				response = _serialData.take();
			} catch (InterruptedException e) {
				return ERROR+" Fail to get completion response";
			}
			
			// check for completion which is a signal saying data is ready to be read
			if (INS_DATA_AVAIL.equals(response)) {
				
				// read the data created by the robot
				try {
					response = _serialData.take();
				} catch (InterruptedException e) {
					return ERROR+" Fail to read measure data";
				}
				
				// parse the data and compare against the _wallDistance
				String[] measurement = PApplet.splitTokens(response, ":"); 
				final int dist = Integer.parseInt(measurement[0]);
				final float heading = Float.parseFloat(measurement[1]);
				System.out.println(dist);
				// check to see if measurement is over the threshold, meaning a door is present
				if ((10*dist) > (_wallDistance *= _wallThreshold)) {
					return DOOR_DETECTED;
				}
				else {
					// otherwise no door is present (next to wall)
					return NO_DOOR_DETECTED;
				}
			}
		}
		
		return ERROR+" ERR response received";
	}
}