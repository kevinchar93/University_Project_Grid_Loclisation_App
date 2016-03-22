package core;

import java.util.concurrent.*;
import controlP5.*;
import org.gicentre.utils.stat.*;

import map.Map;
import processing.core.*;
import processing.serial.Serial;
import robot.*;

public class CoreUI extends PApplet {
	
	/* Details about the environment the robot is in */
	int Grid_Size_MM = 300;
	int Wall_Distance_MM = 180;
	float Wall_Threshold_Percent = 1.2f;
	
	Direction Side_Doors_Are_On;
	boolean Is_World_Cyclic;
	
	/* Detail about the robot */
	Robot robot;
	
	/* Details about the chart to be displayed and general UI */
	BarChart barChart;
	int barChartWidth;
	int barChartHeight;
	int barChartX;
	int barChartY;
	
	ControlP5 cp5;
	PFont ArialFont20;
	
	ButtonBar controlBtnBar;
	int controlBtnBarHeight;
	int controlBtnBarWidth;
	int controlBtnBarX;
	int controlBtnBarY;
	
	Textfield controlValue;
	int controlValueHeight;
	int controlValueWidth;
	int controlValueX;
	int controlValueY;
	
	/* ------------------------------------------------------ */
	
	public void settings() {
		
		size(1200, 700);
		
	}
	
	public void setup() {
		
		
		// UI setup
		cp5 = new ControlP5(this);
		ArialFont20 = createFont("arial", 20);
		
		
		// Bar chart  setup
		barChart = new BarChart(this);
		barChart.setMinValue(0.0f);
		barChart.setMaxValue(1.0f);
		barChart.showValueAxis(true);
		barChart.showCategoryAxis(true);
		barChart.setCategoryAxisLabel("Grid Cells");
		
		barChartWidth = width-400;
		barChartHeight = height -200;
		barChartX = 50;
		barChartY = 50;
		
		controlValueHeight = 50;
		controlValueWidth = barChartWidth/8;
		controlValueX = barChartX;
		controlValueY = barChartHeight + 100;
		
		controlValue = cp5.addTextfield("Value")
				.setPosition(controlValueX, controlValueY)
				.setSize(controlValueWidth, controlValueHeight)
				.setFont(ArialFont20)
				.setColor(this.color(255, 255, 255));
		
		controlBtnBarHeight = 50;
		controlBtnBarWidth = (barChartWidth/8) * 7 ;
		controlBtnBarX = controlValueX + controlValueWidth + 20;
		controlBtnBarY = barChartHeight + 100;
		
		controlBtnBar = cp5.addButtonBar("controlBtnBar")
				 .setPosition(controlBtnBarX, controlBtnBarY)
			 	 .setSize(controlBtnBarWidth, controlBtnBarHeight)
			 	 .addItems(split("Move Left, Sense, Move Right", ","));
		
		setupListeners(); 
		
		//Robot robot = new Robot(this, Grid_Size_MM, Wall_Distance_MM, Wall_Threshold_Percent, serialData);
		//robot.connect("/dev/tty.HC-05-DevB", 9600);
		int[] doors = {1, 2};
		Map map = new Map(5, doors);
		System.out.println(map);
		
		map.setSensorModel(0.9, 0.1);
		try {
			map.setMotionModel(0.8, 0.1, 0.1);
		} catch (Exception e) {
			System.out.println("Probabilities don't add upto one!");
		}
		
		map.sensorUpdate(true);
		System.out.println(map);
		map.motionUpdate(2, Direction.LEFT);
		map.sensorUpdate(true);
		System.out.println(map);
		
		barChart.setData(map.getProbabilityList());
	}
	
	public void postRobotMotion (boolean result, int value, Direction direction) {
		
	}
	
	public void postRobotSensing (boolean result) {
		
	}
	
	public void setupListeners() {
		
		controlBtnBar.onClick(new CallbackListener () {

			@Override
			public void controlEvent(CallbackEvent ev) {
				
				ButtonBar bar = (ButtonBar)ev.getController();
				
				final String strValue = controlValue.getText();
				final int gridSpaces = Integer.parseInt(strValue.isEmpty()? "1": strValue);
				final Direction sideDoorIsOn  = Side_Doors_Are_On;
				final boolean isCyclic = Is_World_Cyclic;
				
				final int MOVE_LEFT = 0;
				final int SENSE = 1;
				final int MOVE_RIGHT = 2;
				
				final String DOOR_DETECTED = "DOOR";
				final String NO_DOOR_DETECTED = "NO_DOOR";
				
				boolean result;
				
				switch (bar.hover()) {
				
					case MOVE_LEFT:
						result = robot.move(gridSpaces, Direction.LEFT, isCyclic);
						postRobotMotion(result, gridSpaces, Direction.LEFT);
						System.out.println("Move left " + gridSpaces);
						break;
						
					case SENSE:
						String resultStr = robot.checkForDoor(sideDoorIsOn);
						if(resultStr.equals(DOOR_DETECTED) || resultStr.equals(NO_DOOR_DETECTED)) {
							result = resultStr.equals(DOOR_DETECTED);
							postRobotSensing(result);
						}
						else {
							// error message to console
						}
						System.out.println("Sense");
						break;
							
					case MOVE_RIGHT:
						result = robot.move(gridSpaces, Direction.RIGHT, isCyclic);
						postRobotMotion(result, gridSpaces, Direction.RIGHT);
						System.out.println("Move right " + gridSpaces);
						break;
						
					default:
						break;
				}
			}
		});
		
	}
	
	public void draw() {
		background(255);
		barChart.draw(barChartX, barChartY, barChartWidth, barChartHeight); 
		
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
