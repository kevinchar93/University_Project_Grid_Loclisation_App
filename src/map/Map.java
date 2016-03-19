package map;

import java.util.List;

import robot.Direction;

public class Map {

	int size;
	List<MapCell> grid;
	
	public Map (int mapSize, int[] doorPositions) {
		// create list of size mapSize
		
		// mark all positions in doorPositions as being doors
		// makre sure you check bounds
	}
	
	public void setSensorModel(double val) {
		
	}
	
	public void setMotionModel(double val) {
		
	}
	
	public void motionUpdate(int val, Direction dir) {
		
	}
	
	public void sensorUpdate(boolean doorPresent) {
		
	}
}
