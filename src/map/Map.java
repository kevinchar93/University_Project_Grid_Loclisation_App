package map;

import java.util.*;
import robot.Direction;

public class Map {

	double _matchScaleFactor;
	double _missScaleFactor;
	
	double _probCorrectMotion;
	double _probIncorrectMotion_Undershoot;
	double _probIncorrectMotion_Overshoot;
	
	List<MapCell> _grid;
	
	public Map (int mapSize, int[] doorPositions) {
		
		// create list of size mapSize
		_grid = new ArrayList<>(mapSize);
		
		// init with uniform probability and set all doors to false
		for (int i = 0; i < mapSize; i++) {
			_grid.add(new MapCell(1.0/mapSize, false));
		}
		
		// set all the door positions to be set to true
		for (int idx : doorPositions) {
			if (_grid.size() > idx) {
				MapCell cell = _grid.get(idx);
				cell._isDoor = true;
				_grid.set(idx, cell);
			}
		}
	}
	
	
	public void setSensorModel(double matchScaleFactor, double missScaleFactor) {
		_matchScaleFactor = matchScaleFactor;
		_missScaleFactor = missScaleFactor;
	}
	
	
	public void setMotionModel(double correctMotion, double underShoot, double overShoot ) throws Exception {
		
		if (1 != (correctMotion+underShoot+overShoot)) {
			throw new Exception();
		}
		
		_probCorrectMotion = correctMotion;
		_probIncorrectMotion_Undershoot = underShoot;
		_probIncorrectMotion_Overshoot = overShoot;
	}
	
	
	public void motionUpdate(int motionVal, final Direction dir) {
		
		// If direction is left we will be shifting by negative amount
		if (Direction.LEFT == dir) {
			motionVal = -motionVal;
		}
		
		List<MapCell> _newGrid = new ArrayList<>(_grid.size());
		
		for (int i = 0; i < _grid.size(); i++) {
			
			// calculate probability after motion from corresponding cells
			// (all the cells that could have gotten us to newCell)
			int idxExact = Map.mod(i-motionVal, _grid.size());
			int idxUndershoot  = Map.mod(i-motionVal -1, _grid.size());
			int idxOvershoot  = Map.mod(i-motionVal +1, _grid.size());
			
			// multiply the beliefs for each type of motion by respective probability
			double probExact = _grid.get(idxExact)._belief * _probCorrectMotion;
			double probUndershoot = _grid.get(idxUndershoot)._belief * _probIncorrectMotion_Undershoot;
			double probOvershoot = _grid.get(idxOvershoot)._belief * _probIncorrectMotion_Overshoot;
			
			MapCell newCell = new MapCell(probExact + probUndershoot + probOvershoot, _grid.get(i)._isDoor);
			_newGrid.add(newCell);
		}
		
		_grid = _newGrid;
	}
	
	
	public void sensorUpdate(final boolean doorMeasurement) {
		
		double beliefSum = 0;
		
		for (int i = 0; i < _grid.size(); i++) {
			
			MapCell cell = _grid.get(i);
			if (doorMeasurement == cell._isDoor) {
				// cell matches measurement, apply match product
				cell._belief *= _matchScaleFactor;
			}
			else {
				// cell doesn't match measurement, apply miss product
				cell._belief *= _missScaleFactor;
			}
			
			// sum the total belief for normalisation
			beliefSum += cell._belief;
		}
		
		// normalise the distribution
		for (int i = 0; i < _grid.size(); i++) {
			MapCell cell = _grid.get(i);
			cell._belief /= beliefSum;
		}
	}
	
	
	public String toString() {
		
		StringJoiner joiner = new StringJoiner(",");
		
		for (MapCell cell : _grid) {
			joiner.add(cell.toString());
		}
		
		return "[ "+joiner.toString()+" ]";
	}
	
	
	public static int mod(int numA, int numB) {
		return (numA % numB + numB) % numB;
	}
	
}
