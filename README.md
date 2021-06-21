# Hour Tracker
Schedule staffing tool used for shift analysis and schedule building.

## Table of Contents
- [Background](#background)
- [Usage](#usage)
- [Download](#download)
- [Developer Setup](#developer-setup)
    - [Requirements and Tools](#requirements-and-tools)
    - [Initial Setup](#initial-setup)
    - [Building the Program](#building-the-program)
- [FAQ](#faq)
- [License](#license)
- [Maintainer](#maintainer)

## Background
Staff scheduling at WCCT Global is done via Excel Spreadsheets in a set format. 
This program aims to quickly analyze the schedule to obtain the following information:
- Holistic overview of what days staff are scheduled and for what times.
- New Excel spreadsheet containing staff names, and their associated shifts.
- New Excel spreadsheet containing a complete list of all shifts, the staff name, their scheduled lunches, and their scheduled hours.

## Usage
![HourTracker Main Screen](/img/Scene.png)

This tool allows users to analyze a schedule and perform several functions to gain critical data.

### Functionality:
- **Import (Step 1)** 
In order to perform any of the following functions, the schedule needs to be imported.
  
![Import Schedule](/img/select-file.png)

- **Hour Tracker**
Tracks staff across the time period of the input schedule. Calculates their total shifted hours for the time period, shifted days, and number of shifted days.
   Creates a sheet at the end of the input workbook containing the corresponding data.
  
![Generates Hours in Schedule](/img/generate.png)

The created sheet is found at the end of the imported schedule.
   
- **Names Only**
Creates a new Excel workbook containing the same days as the input schedule. Copies over the staff and their shift times ONLY.
  
![Generates the Names Only Sheet](/img/names-only.png)

Creates a new Excel document located in the program folder containing the generated Names Only sheet, ready to be sent out.

- **Lunch Data**
Creates a new Excel workbook containing all staff shifted throughout the input schedule, 
   their shift start, shift end, lunch in/out (if input into input schedule), and their shift length in hours. 
   Optionally takes in a staff roster Excel workbook which is used to gather full names.

*Optional Roster Import*

![Imports the Staff Roster](/img/import-roster.png)
 
![Generates the Lunch Data](/img/generate-data.png)

Creates a new Excel document located in the program folder containing the generated lunch data.

## Download
Releases can be found here at [Github](https://github.com/shanedgilbert/HoursTracker/releases/tag/v1.0).

## Developer Setup
This document aims to provide information on how to set up and build the source code.

### Requirements and Tools
This build utilizes the following:
- Java JDK 16+
- Javafx 11+
- Apache POI 4.1.0+ (for EXCEL API)
- Launch4j (for wrapping as .exe and bundling with jre)
- JRE 1.8.0+ (for bundling)

### Initial Setup
In order to run the application:
1. Import Java JDK, JavaFX, and Apache POI.
2. Run 'HourTrackerExe.java'.

### Building the Program
Building the application into a .jar file:
1. Compile 'HourTrackerExe.java'.
2. Navigate: Build > Build Artifacts > Build.
3. The .jar file will be located under /out/artifacts/Main_java_jar/Main.java.jar.

*Optional*
4. Open Launch4j.
5. Open the settings located here: /settings/HourTracker.xml.
6. Tweak the settings as fit. **Note:** Ensure that the bundled jre is located within the output directory.
   Ensure directories are correct.
7. Run Launch4j.

## FAQ
*Nuances and niche cases*

## License
MIT License Copyright (c) 2021 Shane Gilbert

## Maintainer
[@ShaneGilbert](https://github.com/shanedgilbert)