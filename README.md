# COS30018 Delivery System
This will be the final project. Coding standards should be met, largely a refactor of initial project.

## Group Members
* Alexander Rousseaux - 102098573
* Timothy Keesman - 102120418

## Getting Started
### Installing Eclipse
Download [here](https://www.eclipse.org/downloads/) and install.

### Libraries Needed
* JADE Bins [here](https://jade.tilab.com/download/jade/license/jade-download/?x=69&y=9)
    * `jade.jar`
* Google OR-Tools [here](https://developers.google.com/optimization/install/java/windows)
    * `com.google.ortools.jar`
    * `protobuf.jar`
    * `jniortools.dll`
* MySQL Connector [here](https://www.javatpoint.com/example-to-connect-to-the-mysql-database)
    * `mysql-connector.jar`
* CHOCO Solver [here](https://github.com/chocoteam/choco-solver/releases/tag/4.10.1)
    * `choco-solver-4.10.1.jar`

### Tools Needed
* XAMPP [here](https://www.apachefriends.org/index.html)

### Installing MySQL
1. Run the XAMPP installer
2. Start the MySQL Module
3. Navigate to `localhost/phpmyadmin` in your web browser.

### Setting up the Project
1. Go to eclispe workbench
2. Right-click Package Explorer -> New -> Java Project
3. In the Create a Java Project dialogue, set name and note the location. Press finish.
4. Add a new folder in the main directory called "lib"
5. Add `jade.jar`, `com.google.ortools.jar`, `protobuf.jar`, `jniortools.dll`, `mysql-connector.jar` to the "lib" folder.
6. Refresh (f5) the project in eclipse (it should recognize the new folder with the files).
7. Go to Properties -> Java Build Path -> Libraries and press "Add JARS..."
8. Select all `.jar` files (except mysql-connector) and press OK. Apply.
9. Go to Properties -> Java Build Path -> Source
10. Select "Native library location", press Edit...
11. Press Workspace and select the "lib" folder, press OK. Apply and Close.
12. Right-click project -> Build-Path -> Add external archives, add `mysql-connector.jar`
13. Copy the contents of "src" from the master branch into the Eclipse project "src" folder.
14. Refresh Eclipse (f5).
15. Run from "DeliveryController.java"
