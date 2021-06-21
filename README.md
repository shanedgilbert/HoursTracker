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
*Fill this section with instructions on how to use each button/functionality*
*Include pictures*

1. Hour Tracker
   
2. Names Only

3. Lunch Data

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