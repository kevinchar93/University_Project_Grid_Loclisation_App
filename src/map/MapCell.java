package map;

public class MapCell {

		public double _belief;
		public boolean _isDoor;
		
		MapCell(double belief, boolean isDoor) {
			_belief = belief;
			_isDoor = isDoor;
		}
		
		@Override
		public String toString() {
			String beliefStr = String.format("%.3f", _belief);
			String doorStr = _isDoor ? "DOOR" : "WALL";
			return "["+beliefStr+","+doorStr+"]";
		}
}
