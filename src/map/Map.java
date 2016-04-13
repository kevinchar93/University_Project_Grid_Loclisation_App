package map;

import java.util.*;
import robot.Direction;

public class Map {

	double _hitScaleFactor;
	double _missScaleFactor;
	
	double _probCorrectMotion;
	double _probIncorrectMotion_Undershoot;
	double _probIncorrectMotion_Overshoot;
	
	List<MapCell> _grid;
	
	public void init (int mapSize, int[] doorPositions) {
		
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
			}
		}
	}
	
	
	public void setSensorModel(double hitScaleFactor, double missScaleFactor) {
		_hitScaleFactor = hitScaleFactor;
		_missScaleFactor = missScaleFactor;
	}
	
	
	public void setMotionModel(double correctMotion, double underShoot, double overShoot ) {
		
		_probCorrectMotion = correctMotion;
		_probIncorrectMotion_Undershoot = underShoot;
		_probIncorrectMotion_Overshoot = overShoot;
	}
	
	
	public void motionUpdate(int motionVal, final Direction dir, boolean isWorldCyclic) {
		
		// If direction is left we will be shifting by negative amount
		if (Direction.LEFT == dir) {
			motionVal = -motionVal;
		}
		
		List<MapCell> _newGrid = new ArrayList<>(_grid.size());
		
		
		// perform convolution in cyclic world
		if (true == isWorldCyclic) {
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
		}
		else {
			
			// perform convolution in non-cyclic world
			double probabilitySum = 0;
			
			for (int i = 0; i < _grid.size(); i++) {
				
				// calculate probability after motion from corresponding cells
				// (all the cells that could have gotten us to newCell, NOTE values
				//  are NOT being wrapped to fit in the world)
				int idxExact	  = i-motionVal;
				int idxUndershoot = i-motionVal -1;
				int idxOvershoot  = i-motionVal +1;
				
				// get the current belief at these indices, if the index is outside
				// the bounds of the grid then a belief of 0 (impossible) is set
				double idxExactBelief 		= (idxExact>= 0 && idxExact < _grid.size()) ? _grid.get(idxExact)._belief : 0;
				double idxUndershootBelief 	= (idxUndershoot>= 0 && idxUndershoot < _grid.size()) ? _grid.get(idxUndershoot)._belief : 0;
				double idxOvershootBelief 	= (idxOvershoot>= 0 && idxOvershoot < _grid.size()) ? _grid.get(idxOvershoot)._belief : 0;
				
				// multiply the beliefs for each type of motion by respective probability
				double probExact = idxExactBelief * _probCorrectMotion;
				double probUndershoot = idxUndershootBelief * _probIncorrectMotion_Undershoot;
				double probOvershoot = idxOvershootBelief * _probIncorrectMotion_Overshoot;
				
				MapCell newCell = new MapCell(probExact + probUndershoot + probOvershoot, _grid.get(i)._isDoor);
				_newGrid.add(newCell);
				
				// sum probability for normalisation later
				probabilitySum += probExact + probUndershoot + probOvershoot;
				
			}
			
			// since the probability of some cells will now be 0 we need to
			// normalise the probability distribution
			for (MapCell cell : _newGrid) {
				cell._belief /= probabilitySum;
			}
		}
		_grid = _newGrid;
	}
	
	
	public void sensorUpdate(final boolean doorMeasurement) {
		
		double beliefSum = 0;
		
		for (MapCell cell : _grid) {
			
			if (doorMeasurement == cell._isDoor) {
				// cell matches measurement, apply match product
				cell._belief *= _hitScaleFactor;
			}
			else {
				// cell doesn't match measurement, apply miss product
				cell._belief *= _missScaleFactor;
			}
			
			// sum the total belief for normalisation
			beliefSum += cell._belief;
		}
		
		// normalise the distribution
		for (MapCell cell : _grid) {
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
	
	
	public float[] getProbabilityList() {
		
		float[] values = new float[_grid.size()];
		
		for (int i = 0; i < _grid.size(); i++) {
			values[i] = (float) _grid.get(i)._belief;
		}
		
		return values;
	}
	
	
	public static int mod(int numA, int numB) {
		return (numA % numB + numB) % numB;
	}
}
