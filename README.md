# Grid Localisation App (Final Year Project)
This repo is one of a number from my final year project at university. This in particular is for a piece of software used to control the robot remotely and view an estimate of its position. More specific details about the project and the Grid Localisation App in particular can be found below.

<p align="center">
<img src="https://github.com/kevinchar93/University_Project_Grid_Loclisation_App/blob/master/OneDimSim_out.gif" 
alt="The Complete Robot" width="480" height="345" border="10" />
</p>

### About the Project
My final year project at university was inspired somewhat by my placement year where I worked on programming embedded systems in the form of TV set top boxes, having had this experience I decided to work on a project that dealt with some form of embedded system. In the end I decided to construct and program a robot capable of localising itself in two dimensions.

By the end of the project I'd encountered issues that made me scale down the project. Ultimately I produced a robot that is capable of localising itself in only one dimension, however another piece of software which presents a simple simulation of two dimensional localisation was implemented instead.

The project report titled "[**An Implementation of a Mobile Robot with Localisation Capabilities**](https://github.com/kevinchar93/University_Project_Mobile_Platform_SW/blob/master/An%20Implementation%20of%20a%20Mobile%20Robot%20with%20Localisation%20Capabilities.pdf)" can be found in the root directory of this repo.

In total for the project 4 deliverables were created (links to repos in brackets):
* The actual physical robot
* The software on the robot's embedded platform ([Mobile Platform SW](https://github.com/kevinchar93/University_Project_Mobile_Platform_SW))
* Software used to control the robot remotely & estimate its position ([Grid Localisation App](https://github.com/kevinchar93/University_Project_Grid_Loclisation_App))
* A two dimensional Monte Carlo localisation simulator  ([Particle Filter Simulator App](https://github.com/kevinchar93/University_Project_Particle_Filter_Simulator_App))

### More about the Grid Localisation App

The "Grid Localisation App" is the piece of software that runs remotely on a laptop, it is used to send control commands to the robot via a Bluetooth connection and present a visual estimate of the robot's position in the form of a histogram. At its core the application uses a version of the Grid Localisation algorithm shown in the diagram below. Algorithm sourced from Thrun, Fox, Wolfram (2005, p239, Probabalistic Robotics).

<p align="center">
<img src="https://github.com/kevinchar93/University_Project_Grid_Loclisation_App/blob/master/basic_grid_localisation_algorithm_flow.png" 
alt="The Basic Grid Localisation Algorithm" width="799" height="544" border="10" />
</p>

For the purpose of this project it is assumed the robot can only move to the right or left in the world and that it can sense the doors, walls and ends of the world. This specific implementation of Grid Localisation makes use of a simple sensor and motion model presented in Udacity Course CS 373 (Georgia Institute of Technology 2013), in the motion model the robot has a probability of undershooting, overshooting or arriving exactly at its intended target this is used to probabilistically predict what grid cell the robot will be in after a motion. The sensor model specifies multiples that are used to raise or lower the probability that the robot is in a given cell given measurements from the sensor. These simple motion and sensor models were used as they are suitable at this level and developing models that are more specific were outside the scope of the project.

Flow chart showing how the Grid Localisation App works:
<p align="center">
<img src="https://github.com/kevinchar93/University_Project_Grid_Loclisation_App/blob/master/modified_grid_localisation_algorithm_flowchart.png" 
alt="The Basic Grid Localisation Algorithm" width="677" height="548" border="10" />
</p>

See the report "[**An Implementation of a Mobile Robot with Localisation Capabilities**](https://github.com/kevinchar93/University_Project_Mobile_Platform_SW/blob/master/An%20Implementation%20of%20a%20Mobile%20Robot%20with%20Localisation%20Capabilities.pdf)" for more details.

## License

Copyright © 2016 Kevin Charles

Distributed under the MIT License
