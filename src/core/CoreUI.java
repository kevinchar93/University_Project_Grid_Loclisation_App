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
	
	/* Details about various settings*/
	int Grid_Cell_Size_MM = 300;
	int Wall_Distance_MM = 180;
	float Wall_Threshold_Percent = 1.2f;
	int Baud_Rate = 9600;
	
	int World_Size_Num_Grid_Cells = 5;
	int[] Door_Positions;
	boolean Is_World_Cyclic;
	
	float Sensor_Hit_Product;
	float Sensor_Miss_Product;
	
	float Prob_Motion_Exact;
	float Prob_Motion_Over;
	float Prob_Motion_Under;
	
	Direction Side_Doors_Are_On;
	
	
	
	boolean SETTINGS_INITIALISED = false;
	boolean DISTRIBUTION_CHANGED = false;
	
	Robot robot;
	Map map;
	/* ------------------------------------------------------ */
	
	
	public void settings() {
		size(1200, 800);
	}
	
	
	public void setup() {
		
		robot = new Robot(this, serialData);
		map = new Map();
		
		setupUserInterface();
		setupListeners(); 
	}
	
	public void draw() {
		
		// First draw the background
		background(255);
		
		// Check to see if user has given us settings before allowing use of control buttons
		// or drawing of the probability distribution
		if (true == SETTINGS_INITIALISED) {
			// Enable the control buttons
			controlBtnBar.setLock(false);
			
			// if the probability distribution in the map changed we need to update the data
			// being displayed by the bar graph
			if (true == DISTRIBUTION_CHANGED) {
				barChart.setData(map.getProbabilityList());
				DISTRIBUTION_CHANGED = false;
			}
			barChart.draw(barChartX, barChartY, barChartWidth, barChartHeight);
		}
		else
		{
			// Disable the control buttons
			controlBtnBar.setLock(true);
		}
		
		// Draw the console on every frame
		console.draw(consoleX, consoleY, consoleWidth, consoleHeight, 14, 14);
	}
	
	public void postRobotMotion (boolean result, int value, Direction direction) {
		
	}
	
	
	public void postRobotSensing (boolean result) {
		
	}
	
	
	public void setupUserInterface() {
		// UI painstakingly hand crafted! :( , no WISIWYG!
		
		/* UI setup ------------------------------------------------------------------- */
		cp5 = new ControlP5(this);
		ArialFont16 = createFont("arial", 16, true);
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
		
		final int spaceBetweenChartAndEnd = width - (barChartX + barChartWidth);
		final int standardFieldHeight = 25;
		final int standardFieldWidth = (spaceBetweenChartAndEnd / 3) * 2;
		final int standardLabelTextSize = 12;
		final int standardHeightDivisor = 30;
		
		/* control value field -------------------------------------------------------- */
		int controlValueHeight = 35;
		int controlValueWidth = barChartWidth/8;
		int controlValueX = barChartX;
		int controlValueY = barChartY + barChartHeight + 8;
		controlValue = cp5.addTextfield("Value")
				.setPosition(controlValueX, controlValueY)
				.setSize(controlValueWidth, controlValueHeight)
				.setFont(ArialFont16)
				.setCaptionLabel("Grid Spaces")
				.setColorCaptionLabel(0)
				.setColor(this.color(255, 255, 255));
		
		controlValue.getCaptionLabel()
					.setSize(standardLabelTextSize);
		
		/* control button bar at bottom ----------------------------------------------- */
		int controlBtnBarHeight = 35;
		int controlBtnBarWidth = (barChartWidth/8) * 7 ;
		int controlBtnBarX = controlValueX + controlValueWidth + 20;
		int controlBtnBarY = barChartY + barChartHeight + 8;
		
		controlBtnBar = cp5.addButtonBar("controlBtnBar")
				 .setPosition(controlBtnBarX, controlBtnBarY)
			 	 .setSize(controlBtnBarWidth, controlBtnBarHeight)
			 	 .addItems(split("Move Left, Sense, Move Right", ","));
		
		controlBtnBar.getValueLabel()
					 .setSize(standardLabelTextSize);
		
		/* console at bottom --------------------------------------------------------- */
		console = new Console(this);
		console.start();
		consoleWidth = (controlBtnBarX+controlBtnBarWidth) - controlValueX ;
		consoleY = controlBtnBarY + controlBtnBarHeight + 20;
		consoleHeight = height;
		consoleX = barChartX;
		
		/* connection port field ----------------------------------------------------- */
		int connectionPortHeight = standardFieldHeight;
		int connectionPortWidth = ((standardFieldWidth / 3) * 2) - 10;
		int connectionPortX = (barChartX + barChartWidth) + 100;
		int connectionPortY = (height / standardHeightDivisor) * 1;
		connectionPort = cp5.addTextfield("connectionPort")
							.setPosition(connectionPortX, connectionPortY)
							.setSize(connectionPortWidth, connectionPortHeight)
							.setFont(ArialFont14)
							.setCaptionLabel("Connection Port")
							.setColorCaptionLabel(0)
							.setColor(this.color(255, 255, 255));
		
		connectionPort.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		connectionPort.setText("/dev/tty.HC-05-DevB");
		
		/* connect button ------------------------------------------------------------ */
		int connectButtonHeight = standardFieldHeight;
		int connectButtonWidth = ((standardFieldWidth / 3) * 1) -10;
		int connectButtonX = (connectionPortX + connectionPortWidth) + 20;
		int connectButtonY = (height / standardHeightDivisor) * 1;
		connectButton = cp5.addButton("connectButton")
					   .setPosition(connectButtonX, connectButtonY)
					   .setSize(connectButtonWidth, connectButtonHeight)
					   .setCaptionLabel("Connect");
		
		connectButton.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* baud rate field ----------------------------------------------------------- */
		int baudRateHeight = standardFieldHeight;
		int baudRateWidth = (standardFieldWidth/2) - 10;
		int baudRateSizeX = (barChartX + barChartWidth) + 100;
		int baudRateY = (height / standardHeightDivisor) * 3;
		baudRate = cp5.addTextfield("baudRate")
					  .setPosition(baudRateSizeX, baudRateY)
					  .setSize(baudRateWidth, baudRateHeight)
					  .setFont(ArialFont14)
					  .setCaptionLabel("Baud Rate")
					  .setColorCaptionLabel(0)
					  .setColor(this.color(255, 255, 255));

		baudRate.getCaptionLabel()
			    .setSize(standardLabelTextSize);
		
		baudRate.setText("9600");
		
		/* grid cell size field ------------------------------------------------------ */
		int gridCellSizeHeight = standardFieldHeight;
		int gridCellSizeWidth = (standardFieldWidth/2) - 10;
		int gridCellSizeX = (baudRateSizeX + baudRateWidth) + 20;
		int gridCellSizeY = (height / standardHeightDivisor) * 3;
		gridCellSize = cp5.addTextfield("gridCellSize")
						  .setPosition(gridCellSizeX, gridCellSizeY)
						  .setSize(gridCellSizeWidth, gridCellSizeHeight)
						  .setFont(ArialFont14)
						  .setCaptionLabel("Grid Cell Size MM")
						  .setColorCaptionLabel(0)
						  .setColor(this.color(255, 255, 255));
		
		gridCellSize.getCaptionLabel()
		  			 .setSize(standardLabelTextSize);
		
		/* wall distance field ------------------------------------------------------- */
		int wallDistanceHeight = standardFieldHeight;
		int wallDistanceWidth = (standardFieldWidth/2) - 10;
		int wallDistanceX = (barChartX + barChartWidth) + 100;
		int wallDistanceY = (height / standardHeightDivisor) * 5;
		wallDistance = cp5.addTextfield("wallDistance")
						  .setPosition(wallDistanceX, wallDistanceY)
						  .setSize(wallDistanceWidth, wallDistanceHeight)
						  .setFont(ArialFont14)
						  .setCaptionLabel("Wall Distance MM")
						  .setColorCaptionLabel(0)
						  .setColor(this.color(255, 255, 255));
		
		wallDistance.getCaptionLabel()
	  			    .setSize(standardLabelTextSize);
		
		/* measurement threshold field ----------------------------------------------- */
		int measurementThresholdHeight = standardFieldHeight;
		int measurementThresholdWidth = (standardFieldWidth/2) - 10;
		int measurementThresholdX = (wallDistanceX + wallDistanceWidth) + 20;
		int measurementThresholdY = (height / standardHeightDivisor) * 5;
		measurementThreshold = cp5.addTextfield("measurementThreshold")
								  .setPosition(measurementThresholdX, measurementThresholdY)
								  .setSize(measurementThresholdWidth, measurementThresholdHeight)
								  .setFont(ArialFont14)
								  .setCaptionLabel("% Threshold")
								  .setColorCaptionLabel(0)
								  .setColor(this.color(255, 255, 255));
		
		measurementThreshold.getCaptionLabel()
		  			 		.setSize(standardLabelTextSize);
		
		/* world size field ---------------------------------------------------------- */
		int worldSizeHeight = standardFieldHeight;
		int worldSizeWidth = (standardFieldWidth/2) - 10;
		int worldSizeX = (barChartX + barChartWidth) + 100;
		int worldSizeY = (height / standardHeightDivisor) * 7;
		worldSize = cp5.addTextfield("worldSize")
					   .setPosition(worldSizeX, worldSizeY)
					   .setSize(worldSizeWidth, worldSizeHeight)
					   .setFont(ArialFont16)
					   .setCaptionLabel("World Size")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		worldSize.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* door positions field ------------------------------------------------------ */
		int doorPositionsHeight = standardFieldHeight;
		int doorPositionsWidth = (standardFieldWidth/2) - 10;;
		int doorPositionsX = worldSizeX + worldSizeWidth + 20;
		int doorPositionsY = (height / standardHeightDivisor) * 7;
		doorPositions = cp5.addTextfield("doorPositions")
					   .setPosition(doorPositionsX, doorPositionsY)
					   .setSize(doorPositionsWidth, doorPositionsHeight)
					   .setFont(ArialFont16)
					   .setCaptionLabel("Door Positions")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		doorPositions.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* is world cyclic check box ------------------------------------------------- */
		int cyclicWorldHeight = standardFieldHeight;
		int cyclicWorldWidth = standardFieldWidth/4;
		int cyclicWorldX = (barChartX + barChartWidth) + 100;
		int cyclicWorldY = (height / standardHeightDivisor) * 9;
		cyclicWorld = cp5.addRadioButton("cyclicWorld")
						   .setPosition(cyclicWorldX, cyclicWorldY)
						   .setItemsPerRow(2)
						   .setSize(cyclicWorldWidth, cyclicWorldHeight)
						   .setCaptionLabel("Is World Cyclic?")
						   .setColorLabel(color(0))
						   .setSpacingColumn(cyclicWorldWidth)
						   .addItem("Cyclic", 1)
						   .addItem("Non Cyclic", 2);
		
		cyclicWorld.getItem(0)
				   .getCaptionLabel()
				   .setSize(12)
				   .setColor(0, true);
		
		cyclicWorld.getItem(1)
		   		   .getCaptionLabel()
		   		   .setSize(12)
		   		   .setColor(0, true);
		
		/* sensor hit field --------------------------------------------------------- */
		int senseHitProductHeight = standardFieldHeight;
		int senseHitProductWidth = standardFieldWidth;
		int senseHitProductX = (barChartX + barChartWidth) + 100;
		int senseHitProductY = (height / standardHeightDivisor) * 11;
		senseHitProduct = cp5.addTextfield("senseHitProduct")
					   .setPosition(senseHitProductX, senseHitProductY)
					   .setSize(senseHitProductWidth, senseHitProductHeight)
					   .setFont(ArialFont16)
					   .setCaptionLabel("Sensor Hit Product")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		senseHitProduct.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* sensor miss field ------------------------------------------------------- */
		int senseMissProductHeight = standardFieldHeight;
		int senseMissProductWidth = standardFieldWidth;
		int senseMissProductX = (barChartX + barChartWidth) + 100;
		int senseMissProductY = (height / standardHeightDivisor) * 13;
		senseMissProduct = cp5.addTextfield("senseMissProduct")
					   .setPosition(senseMissProductX, senseMissProductY)
					   .setSize(senseMissProductWidth, senseMissProductHeight)
					   .setFont(ArialFont16)
					   .setCaptionLabel("Sensor Miss Product")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		senseMissProduct.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* probability exact motion ------------------------------------------------ */
		int probExactMotionHeight = standardFieldHeight;
		int probExactMotionWidth = standardFieldWidth;
		int probExactMotionX = (barChartX + barChartWidth) + 100;
		int probExactMotionY = (height / standardHeightDivisor) * 15;
		probExactMotion = cp5.addTextfield("probExactMotion")
					   .setPosition(probExactMotionX, probExactMotionY)
					   .setSize(probExactMotionWidth, probExactMotionHeight)
					   .setFont(ArialFont16)
					   .setCaptionLabel("Probability Exact Motion")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		probExactMotion.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* probability motion overshoot -------------------------------------------- */
		int probInexactMotionOverHeight = standardFieldHeight;
		int probInexactMotionOverWidth = standardFieldWidth;
		int probInexactMotionOverX = (barChartX + barChartWidth) + 100;
		int probInexactMotionOverY = (height / standardHeightDivisor) * 17;
		probInexactMotionOver = cp5.addTextfield("probInexactMotionOver")
					   .setPosition(probInexactMotionOverX, probInexactMotionOverY)
					   .setSize(probInexactMotionOverWidth, probInexactMotionOverHeight)
					   .setFont(ArialFont16)
					   .setCaptionLabel("Probability Motion Overshoot")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		probInexactMotionOver.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* probability motion undershoot ------------------------------------------- */
		int probInexactMotionUnderHeight = standardFieldHeight;
		int probInexactMotionUnderWidth = standardFieldWidth;
		int probInexactMotionUnderX = (barChartX + barChartWidth) + 100;
		int probInexactMotionUnderY = (height / standardHeightDivisor) * 19;
		probInexactMotionUnder = cp5.addTextfield("probInexactMotionUnder")
					   .setPosition(probInexactMotionUnderX, probInexactMotionUnderY)
					   .setSize(probInexactMotionUnderWidth, probInexactMotionUnderHeight)
					   .setFont(ArialFont16)
					   .setCaptionLabel("Probability Motion Undershoot")
					   .setColorCaptionLabel(0)
					   .setColor(this.color(255, 255, 255));
		
		probInexactMotionUnder.getCaptionLabel()
					  .setSize(standardLabelTextSize);
		
		/* door side check box ----------------------------------------------------- */
		int doorSideHeight = standardFieldHeight;
		int doorSideWidth = standardFieldWidth/4;
		int doorSideX = (barChartX + barChartWidth) + 100;
		int doorSideY = (height / standardHeightDivisor) * 21;
		doorSide = cp5.addRadioButton("		doorSide")
						   .setPosition(doorSideX, doorSideY)
						   .setItemsPerRow(2)
						   .setSize(doorSideWidth, doorSideHeight)
						   .setCaptionLabel("Door Side")
						   .setColorLabel(color(0))
						   .setSpacingColumn(cyclicWorldWidth)
						   .addItem("Left", 1)
						   .addItem("Right", 2);
				
		doorSide.getItem(0)
			    .getCaptionLabel()
			    .setSize(12)
			    .setColor(0, true);
				
		doorSide.getItem(1)
	   		    .getCaptionLabel()
	   		    .setSize(12)
	   		    .setColor(0, true);
		
		/* set button -------------------------------------------------------------- */
		int setButtonHeight = standardFieldHeight;
		int setButtonWidth = standardFieldWidth;
		int setButtonX = (barChartX + barChartWidth) + 100;
		int setButtonY = (height / standardHeightDivisor) * 23;
		setButton = cp5.addButton("setButton")
					   .setPosition(setButtonX, setButtonY)
					   .setSize(setButtonWidth, setButtonHeight)
					   .setCaptionLabel("Set");
		
		setButton.getCaptionLabel()
					  .setSize(standardLabelTextSize);
	}
	
	
	public void setupListeners() {
		
		// control bar buttons functionality
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
		
		// connect button functionality
		connectButton.onClick(new CallbackListener () {

			@Override
			public void controlEvent(CallbackEvent ev) {
				String text = baudRate.getText();
				int intValue;
				String portName = connectionPort.getText();
				
				if (!text.isEmpty()) {
					try {
						intValue = Integer.parseInt(text);
						if (intValue > 0) {
							Baud_Rate = intValue;
						}
						else {
							System.out.println("Baud Rate must be greater than 0");
							connectButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid integer was not provided as Baud Rate");
						connectButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No Baud Rate provided");
					connectButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				if (portName.isEmpty()) {
					System.out.println("No port name provided");
					connectButton.setColorBackground(color(255, 80, 80));
				}
				else {
					boolean result = robot.connect(portName, Baud_Rate);
					if (result) {
						System.out.println("Connection Successful");
						connectButton.setColorBackground(color(0, 204, 102));
					}
					else {
						System.out.println("Could not connect");
						connectButton.setColorBackground(color(255, 80, 80));
					}
				}
			}
			
		});
		
		
		// set button functionality
		setButton.onClick(new CallbackListener () {
			
			@Override
			public void controlEvent(CallbackEvent ev) {
				
				SETTINGS_INITIALISED = false;
				String text;
				int intValue;
				float floatValue;
				
				/* get, verify and set world size ------------------------------------------------ */
				text = worldSize.getText();
				
				if (!text.isEmpty()) {
					try {
						intValue = Integer.parseInt(text);
						if (intValue > 0) {
							World_Size_Num_Grid_Cells = intValue;
						}
						else {
							System.out.println("World size must be greater than 0");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid integer was not provided as world size");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No world size provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and set door positions ------------------------------------------- */
				text = doorPositions.getText();
				
				if (!text.isEmpty()) {
					String[] strNums = text.split(",");
					int[] intNums = new int[strNums.length];
					
					for (int i = 0; i < strNums.length; i++) {
						try {
							intValue = Integer.parseInt(strNums[i]);
							if (intValue >= 0) {
								intNums[i] = intValue;
							}
							else {
								System.out.println("Door positons must be 0 or greater");
								setButton.setColorBackground(color(255, 80, 80));
								return;
							}
						}
						catch (NumberFormatException  e) {
							System.out.println("Valid integer/s not provided for door positions");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					Door_Positions = intNums;
				}
				else {
					System.out.println("No door positions were given");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and set cyclic or non cyclic world ------------------------------- */
				final int CYCLIC_SELECTED = 1;
				final int NON_CYCLIC_SELECTED = 2;
				intValue = (int) cyclicWorld.getValue();
				
				switch (intValue) {
				
					case CYCLIC_SELECTED:
						Is_World_Cyclic = true;
						break;
					case NON_CYCLIC_SELECTED:
						Is_World_Cyclic = false;
						break;
					default:
						System.out.println("No selection for if world is cyclic");
						setButton.setColorBackground(color(255, 80, 80));
						return;
				}
				
				/* get, verify and set sensor hit product --------------------------------------- */
				text = senseHitProduct.getText();
				
				if (!text.isEmpty()) {
					try {
						floatValue = Float.parseFloat(text);
						if (floatValue > 0) {
							Sensor_Hit_Product = floatValue;
						}
						else {
							System.out.println("Hit product must be greater than 0");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid float was not provided as hit product");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No hit product provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and set sensor miss product -------------------------------------- */
				text = senseMissProduct.getText();
				
				if (!text.isEmpty()) {
					try {
						floatValue = Float.parseFloat(text);
						if (floatValue > 0) {
							Sensor_Miss_Product = floatValue;
						}
						else {
							System.out.println("Miss product must be greater than 0");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid float was not provided as miss product");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No miss product provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and set probability exact motion --------------------------------- */
				text = probExactMotion.getText();
				
				if (!text.isEmpty()) {
					try {
						floatValue = Float.parseFloat(text);
						if (floatValue >= 0 && floatValue <= 1) {
							Prob_Motion_Exact = floatValue;
						}
						else {
							System.out.println("Probability of exact motion must be between 0 and 1 (inclusive)");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid float was not provided as probability of exact motion");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No probability of exact motion provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and set probability motion overshoot ----------------------------- */
				text = probInexactMotionOver.getText();
				
				if (!text.isEmpty()) {
					try {
						floatValue = Float.parseFloat(text);
						if (floatValue >= 0 && floatValue <= 1) {
							Prob_Motion_Over = floatValue;
						}
						else {
							System.out.println("Probability of motion overshoot must be between 0 and 1 (inclusive)");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid float was not provided as probability of motion overshoot");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No probability of motion overshoot provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and set probability motion undershoot ----------------------------- */
				text = probInexactMotionUnder.getText();
				
				if (!text.isEmpty()) {
					try {
						floatValue = Float.parseFloat(text);
						if (floatValue >= 0 && floatValue <= 1) {
							Prob_Motion_Under = floatValue;
						}
						else {
							System.out.println("Probability of motion undershoot must be between 0 and 1 (inclusive)");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid float was not provided as probability of motion undershoot");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No probability of motion undershoot provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* check that motion probabilities sum to 1  ------------------------------------- */
				if ((Prob_Motion_Exact + Prob_Motion_Over + Prob_Motion_Under) != 1) {
					System.out.println("Total probability of exact motion, motion over/under shoot must equal 1");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and set set side door is on --------------------------------------- */
				final int DOOR_ON_LEFT = 1;
				final int DOOR_ON_RIGHT = 2;
				intValue = (int) doorSide.getValue();
				
				switch (intValue) {
				
					case DOOR_ON_LEFT:
						Side_Doors_Are_On = Direction.LEFT;
						break;
					case DOOR_ON_RIGHT:
						Side_Doors_Are_On = Direction.RIGHT;
						break;
					default:
						System.out.println("No selection for what side doors are on");
						setButton.setColorBackground(color(255, 80, 80));
						return;
				}
			
				/* get, verify and the size of a grid cell -------------------------------------- */
				text = gridCellSize.getText();
				
				if (!text.isEmpty()) {
					try {
						intValue = Integer.parseInt(text);
						if (intValue > 0) {
							Grid_Cell_Size_MM = intValue;
						}
						else {
							System.out.println("Grid cell size must be greater than 0");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid integer was not provided for grid cell size");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No grid cell size provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and the distance to the wall --------------------------------------*/
				text = wallDistance.getText();
				
				if (!text.isEmpty()) {
					try {
						intValue = Integer.parseInt(text);
						if (intValue > 0) {
							Wall_Distance_MM = intValue;
						}
						else {
							System.out.println("Wall distance must be greater than 0");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid integer was not provided for wall distance");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No wall distance provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* get, verify and the distance to the wall --------------------------------------*/
				text = measurementThreshold.getText();
				
				if (!text.isEmpty()) {
					try {
						floatValue = Float.parseFloat(text);
						if (floatValue >= 0) {
							Wall_Threshold_Percent = floatValue;
						}
						else {
							System.out.println("Measurment threshold must be greater than or equal to 0");
							setButton.setColorBackground(color(255, 80, 80));
							return;
						}
					}
					catch (NumberFormatException  e) {
						System.out.println("Valid float was not provided for the measurment threshold");
						setButton.setColorBackground(color(255, 80, 80));
						return;
					}
				}
				else {
					System.out.println("No measurement threshold provided");
					setButton.setColorBackground(color(255, 80, 80));
					return;
				}
				
				/* Use the settings gathered to initialise the program -------------------------- */
				robot.init(Grid_Cell_Size_MM, Wall_Distance_MM, Wall_Threshold_Percent);
				
				map.init(World_Size_Num_Grid_Cells, Door_Positions);
				map.setSensorModel(Sensor_Hit_Product, Sensor_Miss_Product);
				map.setMotionModel(Prob_Motion_Exact, Prob_Motion_Under, Prob_Motion_Over);
				
				/* set variable indicating that everything has been setup ----------------------- */
				SETTINGS_INITIALISED = true;
				DISTRIBUTION_CHANGED = true;
				setButton.setColorBackground(color(0, 204, 102));
				System.out.println("Settings Applied");
			}
		});
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
	PFont ArialFont16;
	PFont ArialFont14;
	
	ButtonBar controlBtnBar;
	Textfield controlValue;
	Textfield connectionPort;
	Button connectButton;
	Textfield worldSize;
	Textfield doorPositions;
	Textfield baudRate;
	Textfield gridCellSize;
	Textfield wallDistance;
	Textfield measurementThreshold;
	RadioButton cyclicWorld;
	Textfield senseHitProduct;
	Textfield senseMissProduct;
	Textfield probExactMotion;
	Textfield probInexactMotionOver;
	Textfield probInexactMotionUnder;
	RadioButton doorSide;
	Button setButton;
}
 