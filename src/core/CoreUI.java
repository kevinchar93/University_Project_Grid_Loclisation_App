package core;

import java.util.concurrent.*;
import controlP5.*;
import org.gicentre.utils.stat.*;

import at.mukprojects.console.Console;
import map.Map;
import processing.core.*;
import processing.serial.Serial;
import robot.*;

public class CoreUI extends PApplet {
	
	/* Details about the environment the robot is in */
	int Grid_Size_MM = 300;
	int Wall_Distance_MM = 180;
	int Num_Grid_Cells = 5;
	float Wall_Threshold_Percent = 1.2f;
	final int BAUD_RATE = 9600;
	
	Direction Side_Doors_Are_On;
	boolean Is_World_Cyclic;
	
	/* Detail about the robot */
	Robot robot;
	
	/* ------------------------------------------------------ */
	
	
	public void settings() {
		size(1200, 800);
	}
	
	
	public void setup() {
		
		
		setupUserInterface();
		
		setupListeners(); 
		
		//Robot robot = new Robot(this, Grid_Size_MM, Wall_Distance_MM, Wall_Threshold_Percent, serialData);
		//robot.connect("/dev/tty.HC-05-DevB", 9600);
		int[] doors = {1, 2};
		Map map = new Map(Num_Grid_Cells, doors);
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
	
	
	public void setupUserInterface() {
		
		/* UI setup ------------------------------------------------------------------- */
		cp5 = new ControlP5(this);
		ArialFont20 = createFont("arial", 20, true);
		ArialFont14 = createFont("arial", 14, true);
		
		/* Bar chart  setup ----------------------------------------------------------- */
		barChart = new BarChart(this);
		barChart.setMinValue(0.0f);
		barChart.setMaxValue(1.0f);
		barChart.showValueAxis(true);
		barChart.showCategoryAxis(true);
		barChart.setCategoryAxisLabel("Grid Cells");
		
		barChartWidth = width-400;
		barChartHeight = height -200;
		barChartX = 50;
		barChartY = 20;
		
		/* control value field -------------------------------------------------------- */
		controlValueHeight = 35;
		controlValueWidth = barChartWidth/8;
		controlValueX = barChartX;
		controlValueY = barChartY + barChartHeight + 8;
		controlValue = cp5.addTextfield("Value")
				.setPosition(controlValueX, controlValueY)
				.setSize(controlValueWidth, controlValueHeight)
				.setFont(ArialFont20)
				.setCaptionLabel("Grid Spaces")
				.setColorCaptionLabel(0)
				.setColor(this.color(255, 255, 255));
		
		controlValue.getCaptionLabel()
					.setSize(14);
		
		/* control button bar at bottom ----------------------------------------------- */
		controlBtnBarHeight = 35;
		controlBtnBarWidth = (barChartWidth/8) * 7 ;
		controlBtnBarX = controlValueX + controlValueWidth + 20;
		controlBtnBarY = barChartY + barChartHeight + 8;
		
		controlBtnBar = cp5.addButtonBar("controlBtnBar")
				 .setPosition(controlBtnBarX, controlBtnBarY)
			 	 .setSize(controlBtnBarWidth, controlBtnBarHeight)
			 	 .addItems(split("Move Left, Sense, Move Right", ","));
		
		int spaceBetweenChartAndEnd = width - (barChartX + barChartWidth);
		int standardFieldHeight = 35;
		int standardFieldWidth = (spaceBetweenChartAndEnd / 3) * 2;
		
		/* console at bottom --------------------------------------------------------- */
		console = new Console(this);
		console.start();
		consoleWidth = (controlBtnBarX+controlBtnBarWidth) - controlValueX ;
		consoleY = controlBtnBarY + controlBtnBarHeight + 20;
		consoleHeight = height;
		consoleX = barChartX;
		
		/* connection port field ----------------------------------------------------- */
		connectionPortHeight = standardFieldHeight;
		connectionPortWidth = ((standardFieldWidth / 3) * 2) - 10;
		connectionPortX = (barChartX + barChartWidth) + 100;
		connectionPortY = (height / 20) * 1;
		connectionPort = cp5.addTextfield("connectionPort")
							.setPosition(connectionPortX, connectionPortY)
							.setSize(connectionPortWidth, connectionPortHeight)
							.setFont(ArialFont14)
							.setCaptionLabel("Connection Port")
							.setColorCaptionLabel(0)
							.setColor(this.color(255, 255, 255));
		
		connectionPort.getCaptionLabel()
					  .setSize(14);
		
		connectionPort.setText("/dev/tty.HC-05-DevB");
		
		/* connect button ------------------------------------------------------------ */
		connectButtonHeight = standardFieldHeight;
		connectButtonWidth = ((standardFieldWidth / 3) * 1) -10;
		connectButtonX = (connectionPortX + connectionPortWidth) + 20;
		connectButtonY = (height / 20) * 1;
		connectButton = cp5.addButton("connectButton")
					   .setPosition(connectButtonX, connectButtonY)
					   .setSize(connectButtonWidth, connectButtonHeight)
					   .setCaptionLabel("Connect");
		
		connectButton.getCaptionLabel()
					  .setSize(14);
		
		/* world size field ---------------------------------------------------------- */
		worldSizeHeight = standardFieldHeight;
		worldSizeWidth = (standardFieldWidth/2) - 10;
		worldSizeX = (barChartX + barChartWidth) + 100;
		worldSizeY = (height / 20) * 3;
		worldSize = cp5.addTextfield("worldSize")
					   .setPosition(worldSizeX, worldSizeY)
					   .setSize(worldSizeWidth, worldSizeHeight)
					   .setFont(ArialFont20)
					   .setCaptionLabel("World Size")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		worldSize.getCaptionLabel()
					  .setSize(14);
		
		/* door positions field ------------------------------------------------------ */
		doorPositionsHeight = standardFieldHeight;
		doorPositionsWidth = (standardFieldWidth/2) - 10;;
		doorPositionsX = worldSizeX + worldSizeWidth + 20;
		doorPositionsY = (height / 20) * 3;
		doorPositions = cp5.addTextfield("doorPositions")
					   .setPosition(doorPositionsX, doorPositionsY)
					   .setSize(doorPositionsWidth, doorPositionsHeight)
					   .setFont(ArialFont20)
					   .setCaptionLabel("Door Positions")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		doorPositions.getCaptionLabel()
					  .setSize(14);
		
		/* is world cyclic check box ------------------------------------------------- */
		cyclicWorldHeight = standardFieldHeight;
		cyclicWorldWidth = standardFieldWidth/4;
		cyclicWorldX = (barChartX + barChartWidth) + 100;
		cyclicWorldY = (height / 20) * 5;
		cyclicWorld = cp5.addRadioButton("cyclicWorld")
						   .setPosition(cyclicWorldX, cyclicWorldY)
						   .setItemsPerRow(2)
						   .setSize(cyclicWorldWidth, cyclicWorldHeight)
						   .setCaptionLabel("Is World Cyclic?")
						   .setColorLabel(color(0))
						   .setSpacingColumn(cyclicWorldWidth)
						   .addItem("Cyclic", 1)
						   .addItem("Non Cyclic", 0);
		
		cyclicWorld.getItem(0)
				   .getCaptionLabel()
				   .setSize(12)
				   .setColor(0, true);
		
		cyclicWorld.getItem(1)
		   		   .getCaptionLabel()
		   		   .setSize(12)
		   		   .setColor(0, true);
		
		/* sensor hit field --------------------------------------------------------- */
		senseHitProductHeight = standardFieldHeight;
		senseHitProductWidth = standardFieldWidth;
		senseHitProductX = (barChartX + barChartWidth) + 100;
		senseHitProductY = (height / 20) * 7;
		senseHitProduct = cp5.addTextfield("senseHitProduct")
					   .setPosition(senseHitProductX, senseHitProductY)
					   .setSize(senseHitProductWidth, senseHitProductHeight)
					   .setFont(ArialFont20)
					   .setCaptionLabel("Sensor Hit Product")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		senseHitProduct.getCaptionLabel()
					  .setSize(14);
		
		/* sensor miss field ------------------------------------------------------- */
		senseMissProductHeight = standardFieldHeight;
		senseMissProductWidth = standardFieldWidth;
		senseMissProductX = (barChartX + barChartWidth) + 100;
		senseMissProductY = (height / 20) * 9;
		senseMissProduct = cp5.addTextfield("senseMissProduct")
					   .setPosition(senseMissProductX, senseMissProductY)
					   .setSize(senseMissProductWidth, senseMissProductHeight)
					   .setFont(ArialFont20)
					   .setCaptionLabel("Sensor Miss Product")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		senseMissProduct.getCaptionLabel()
					  .setSize(14);
		
		/* probability exact motion ------------------------------------------------ */
		probExactMotionHeight = standardFieldHeight;
		probExactMotionWidth = standardFieldWidth;
		probExactMotionX = (barChartX + barChartWidth) + 100;
		probExactMotionY = (height / 20) * 11;
		probExactMotion = cp5.addTextfield("probExactMotion")
					   .setPosition(probExactMotionX, probExactMotionY)
					   .setSize(probExactMotionWidth, probExactMotionHeight)
					   .setFont(ArialFont20)
					   .setCaptionLabel("Probability Exact Motion")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		probExactMotion.getCaptionLabel()
					  .setSize(14);
		
		/* probability motion overshoot -------------------------------------------- */
		probInexactMotionOverHeight = standardFieldHeight;
		probInexactMotionOverWidth = standardFieldWidth;
		probInexactMotionOverX = (barChartX + barChartWidth) + 100;
		probInexactMotionOverY = (height / 20) * 13;
		probInexactMotionOver = cp5.addTextfield("probInexactMotionOver")
					   .setPosition(probInexactMotionOverX, probInexactMotionOverY)
					   .setSize(probInexactMotionOverWidth, probInexactMotionOverHeight)
					   .setFont(ArialFont20)
					   .setCaptionLabel("Probability Motion Overshoot")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		probInexactMotionOver.getCaptionLabel()
					  .setSize(14);
		
		/* probability motion undershoot ------------------------------------------- */
		probInexactMotionUnderHeight = standardFieldHeight;
		probInexactMotionUnderWidth = standardFieldWidth;
		probInexactMotionUnderX = (barChartX + barChartWidth) + 100;
		probInexactMotionUnderY = (height / 20) * 15;
		probInexactMotionUnder = cp5.addTextfield("probInexactMotionUnder")
					   .setPosition(probInexactMotionUnderX, probInexactMotionUnderY)
					   .setSize(probInexactMotionUnderWidth, probInexactMotionUnderHeight)
					   .setFont(ArialFont20)
					   .setCaptionLabel("Probability Motion Undershoot")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		probInexactMotionUnder.getCaptionLabel()
					  .setSize(14);
		
		/* door side check box ----------------------------------------------------- */
		doorSideHeight = standardFieldHeight;
		doorSideWidth = standardFieldWidth/4;
		doorSideX = (barChartX + barChartWidth) + 100;
		doorSideY = (height / 20) * 17;
		doorSide = cp5.addRadioButton("		doorSide")
						   .setPosition(doorSideX, doorSideY)
						   .setItemsPerRow(2)
						   .setSize(doorSideWidth, doorSideHeight)
						   .setCaptionLabel("Door Side")
						   .setColorLabel(color(0))
						   .setSpacingColumn(cyclicWorldWidth)
						   .addItem("Left", 1)
						   .addItem("Right", 0);
				
		doorSide.getItem(0)
			    .getCaptionLabel()
			    .setSize(12)
			    .setColor(0, true);
				
		doorSide.getItem(1)
	   		    .getCaptionLabel()
	   		    .setSize(12)
	   		    .setColor(0, true);
		
		/* set button -------------------------------------------------------------- */
		setButtonHeight = standardFieldHeight;
		setButtonWidth = standardFieldWidth;
		setButtonX = (barChartX + barChartWidth) + 100;
		setButtonY = ((height / 20) * 19) - 30;
		setButton = cp5.addButton("setButton")
					   .setPosition(setButtonX, setButtonY)
					   .setSize(setButtonWidth, setButtonHeight)
					   .setCaptionLabel("Set");
		
		setButton.getCaptionLabel()
					  .setSize(14);
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
		
		
		connectButton.onClick(new CallbackListener () {

			@Override
			public void controlEvent(CallbackEvent ev) {
				
				String portName = connectionPort.getText();
				
				if (portName.isEmpty()) {
					System.out.println("No port name provided");
					connectButton.setColorBackground(color(255, 80, 80));
				}
				else {
					boolean result = robot.connect(portName, BAUD_RATE);
				}
			}
			
		});
		
	}
	
	
	public void draw() {
		background(255);
		
		
		
		barChart.draw(barChartX, barChartY, barChartWidth, barChartHeight);
		console.draw(consoleX, consoleY, consoleWidth, consoleHeight, 14, 14);
		
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
	
	
	/* User Interface Variables */
	Console console;
	int consoleWidth;
	int consoleHeight;
	int consoleX;
	int consoleY;
	
	BarChart barChart;
	int barChartWidth;
	int barChartHeight;
	int barChartX;
	int barChartY;
	
	ControlP5 cp5;
	PFont ArialFont20;
	PFont ArialFont14;
	
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
	
	Textfield connectionPort;
	int connectionPortHeight;
	int connectionPortWidth;
	int connectionPortX;
	int connectionPortY;
	
	Button connectButton;
	int connectButtonHeight;
	int connectButtonWidth;
	int connectButtonX;
	int connectButtonY;
	
	Textfield worldSize;
	int worldSizeHeight;
	int worldSizeWidth;
	int worldSizeX;
	int worldSizeY;
	
	Textfield doorPositions;
	int doorPositionsHeight;
	int doorPositionsWidth;
	int doorPositionsX;
	int doorPositionsY;
	
	RadioButton cyclicWorld;
	int cyclicWorldHeight;
	int cyclicWorldWidth;
	int cyclicWorldX;
	int cyclicWorldY;
	
	Textfield senseHitProduct;
	int senseHitProductHeight;
	int senseHitProductWidth;
	int senseHitProductX;
	int senseHitProductY;
	
	Textfield senseMissProduct;
	int senseMissProductHeight;
	int senseMissProductWidth;
	int senseMissProductX;
	int senseMissProductY;
	
	Textfield probExactMotion;
	int probExactMotionHeight;
	int probExactMotionWidth;
	int probExactMotionX;
	int probExactMotionY;
	
	Textfield probInexactMotionOver;
	int probInexactMotionOverHeight;
	int probInexactMotionOverWidth;
	int probInexactMotionOverX;
	int probInexactMotionOverY;
	
	Textfield probInexactMotionUnder;
	int probInexactMotionUnderHeight;
	int probInexactMotionUnderWidth;
	int probInexactMotionUnderX;
	int probInexactMotionUnderY;
	
	RadioButton doorSide;
	int doorSideHeight;
	int doorSideWidth;
	int doorSideX;
	int doorSideY;
	
	Button setButton;
	int setButtonHeight;
	int setButtonWidth;
	int setButtonX;
	int setButtonY;
}
