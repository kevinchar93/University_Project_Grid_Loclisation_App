package robot;

import robot.InstructionSet;

public class Instruction {

	InstructionSet _type;
	int _value;
	int _gridSize;
	boolean _useGridMode;

	
	public Instruction(InstructionSet type, int value, boolean gridMode, int gridSize) {
		_type = type;
		_value = value;
		_useGridMode = gridMode;
		_gridSize = gridSize;
	}

	
	public Instruction(String type, int value, boolean gridMode, int gridSize) {
		_value = value;
		_useGridMode = gridMode;
		_gridSize = gridSize;
		
		switch (type){
			case "ERROR":
				_type = InstructionSet.ERROR;
				break;
			case "STOP":
				_type = InstructionSet.STOP;
				break;
			case "MOVE_FORWARD":
				_type = InstructionSet.MOVE_FORWARD;
				break;
			case "MOVE_BACKWARD":
				_type = InstructionSet.MOVE_BACKWARD;
				break;
			case "TURN_ZERO_RIGHT_90":
				_type = InstructionSet.TURN_ZERO_RIGHT_90;
				break;
			case "TURN_ZERO_LEFT_90":
				_type = InstructionSet.TURN_ZERO_LEFT_90;
				break;
			case "TURN_AROUND_180":
				_type = InstructionSet.TURN_AROUND_180;
				break;
			case "LIDAR_360_SWEEP":
				_type = InstructionSet.LIDAR_360_SWEEP;
				break;
	    	case "LIDAR_AT_ANGLE":
	    		_type = InstructionSet.LIDAR_AT_ANGLE;
				break;
		}
	}

	public InstructionSet getType() {
		return _type;
	}

	public int getValue() {
		return _value;
	}

	public int getGridSize() {
		return _gridSize;
	}
	
	public boolean useGridMode() {
		return _useGridMode;
	}

	@Override
	public String toString() {

		StringBuilder instruction = new StringBuilder();
		
		switch (_type) {

			case MOVE_FORWARD:
			case MOVE_BACKWARD:
				instruction.append(_type.ordinal());
				instruction.append(',');
				instruction.append(_value);
				instruction.append(',');
				instruction.append(_useGridMode ? 'T' : 'F');
				instruction.append(',');
				instruction.append(_gridSize);
				break;
			
			case STOP:
			case TURN_ZERO_RIGHT_90:
			case TURN_ZERO_LEFT_90:
			case TURN_AROUND_180:
			case LIDAR_360_SWEEP:
				instruction.append(_type.ordinal());
				break;
	
			case LIDAR_AT_ANGLE:
				instruction.append(_type.ordinal());
				instruction.append(',');
				instruction.append(_value);
				break;
				
			case ERROR:
				break;
				
			default:
			break;
		}
		
		instruction.append(';');
		return instruction.toString();
	}

}
