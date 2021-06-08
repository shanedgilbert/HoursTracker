import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Main class used for handling functionality for Hour Tracker
 * Handles:
 *  -Creating shifted days at the end of the original schedule
 *  -Creating a separate sheet for only names and shifts
 *  -Creating a separate sheet for tracking scheduled staff, their lunches, and shifted hours for each day
 */

//TODO: Don't require roster
public class HourTracker {
    private String fileName;
    private String rosterFileName;
    private final String hoursSheetName = "Weekly Hours";
    final static int SHIFT_HOURS = 2;   //Col that ends at the shift times
    final static int LUNCH_HOURS = 3;   //Col that ends at the lunch times
    Map<String, StaffData> StaffDataMap = new HashMap<>();

    //Words to skip on schedule
    String[] tabooWords = {"Staff Name", "Staff", "Shift", "Day Shift 0700-1530", "Mid Shift 1500-2330", "Night Shift 2300-0730", "", " "};
    List<String> tabooWordsList = Arrays.asList(tabooWords);
    String[] headerWords = {"Staff Name", "Staff", "Shift", "", " ", "[", "]"};
    List<String> headerWordsList = Arrays.asList(headerWords);

    /**
     * Empty constructor
     */
    public HourTracker() {
        fileName = "";
    }

    /**
     * Overloaded constructor with fileName
     * @param fileName name of file being imported
     */
    public HourTracker(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Calculates the hours from each shift
     * @param hours hour range from schedule (xxxx-xxxx)
     * @return Hours for shift
     */
    private double calculateHoursForShift(String hours) {
        double totalHours;

        //Checks for invalid shift times
        if(hours.contains("AM") || hours.contains("PM") || hours.contains(":")) {
            System.out.println("Shift times are not in the correct format. Please ensure that shift time follow: 'xxxx-yyyy' in military time");
        }
        if(hours.contains("Charge")) {
            totalHours = 8.5;
        }
        else if(hours.contains("-")) {
            String[] hoursString = hours.split("-");

            //Military time to Civilian time
            double hour1 = (int)(Double.parseDouble(hoursString[0]) / 100) + ((Double.parseDouble(hoursString[0]) % 100) / 60);
            double hour2 = (int)(Double.parseDouble(hoursString[1]) / 100) + ((Double.parseDouble(hoursString[1]) % 100) / 60);
            totalHours = hour2 - hour1;

            //When shifts go into next day
            if(totalHours < 0) {
                totalHours += 24;
            }
        }
        else {
            totalHours = 8.5;
        }

        if (totalHours > 5) {
            totalHours -= 0.5;
        }
        return totalHours;
    }

    /**
     * Returns the start of the shift
     * @param hours The String representation of the staff shift time. ie: xxxx-yyyy
     * @return The String representation of the start of the staff shift. ie: xxxx
     */
    private String getShiftStart(String hours) {
        if(hours.contains("-")) {
            String[] hoursString = hours.split("-");
            return hoursString[0];
        }
        return "";
    }

    /**
     * Returns the end of the shift
     * @param hours The String representation of the staff shift time. ie: xxxx-yyyy
     * @return The String representation of the end of the staff shift. ie: yyyy
     */
    private String getShiftEnd(String hours) {
        if(hours.contains("-")) {
            String[] hoursString = hours.split("-");
            return hoursString[1];
        }
        return "";
    }

    /**
     * Builds the roster map from a roster.txt file
     */
    public void buildMapFromRoster() {
        String rosterFileName = "roster.txt";
        File rosterFile = new File(rosterFileName);

        System.out.println("Building initial roster list...");
        try {
            Scanner rosterFileScanner = new Scanner(rosterFile);
            while(rosterFileScanner.hasNextLine()) {
                StaffDataMap.put(rosterFileScanner.nextLine(), new StaffData());
            }
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found: " + rosterFileName);
        }
        System.out.println("Roster list built");
    }

    /**
     * Imports the excel file and converts the names and shifts into a map with names and hours
     */
    private void inputFile() {
        System.out.println("Searching for file...");
        try {
            File inputFile = new File(fileName);
            FileInputStream fis = new FileInputStream(inputFile);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            System.out.println("File found");
            System.out.println("Importing workbook...");

            int sheetCount = workbook.getNumberOfSheets();

            System.out.println("Calculating shift hours...");
            //Loops through each sheet in workbook
            for(int i = 0; i < sheetCount; i++) {
                Sheet currentSheet = workbook.getSheetAt(i);

                //Checks for hours sheet at end of workbook
                if(currentSheet.getSheetName().equals(hoursSheetName)) {
                    break;
                }

                //Iterates over all rows in current sheet
                for(Row row : currentSheet) {
                    Iterator<Cell> cellIterator = row.cellIterator();

                    int currentColumn = 0;
                    String currentStaffName = "";

                    //Iterates over each column
                    while(cellIterator.hasNext() && currentColumn < SHIFT_HOURS) {
                        Cell cell = cellIterator.next();
                        if(cell.getColumnIndex() < SHIFT_HOURS) {
                            if (currentColumn == 0) {
                                currentStaffName = cell.getStringCellValue();
                            } else if (currentColumn == 1) {
                                //Checks for names in roster and updates their hours
                                if (StaffDataMap.containsKey(currentStaffName)) {
                                    double shiftHours = calculateHoursForShift(cell.getStringCellValue());

                                    //double newHours = staffCurrentHours + shiftHours;
                                    StaffDataMap.get(currentStaffName).updateShiftHours(shiftHours);
                                    StaffDataMap.get(currentStaffName).addShiftDay(currentSheet.getSheetName().substring(0, 2));
                                }

                                //Checks for empty data ("taboo words")
                                else if (tabooWordsList.contains(currentStaffName) || (currentStaffName.contains("[") && currentStaffName.contains("]"))) {
                                    break;
                                }

                                //Checks if staff doesn't exist and adds
                                else {
                                    double shiftHours = calculateHoursForShift(cell.getStringCellValue());
                                    StaffData currentStaffData = new StaffData();
                                    currentStaffData.updateShiftHours(shiftHours);
                                    currentStaffData.addShiftDay(currentSheet.getSheetName().substring(0, 2));
                                    StaffDataMap.put(currentStaffName, currentStaffData);
                                }
                            }
                            currentColumn++;
                        }
                    }
                }
            }
            saveHoursAsSheet(workbook);
            workbook.close();
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found: " + fileName);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Finished calculating all shift hours for all staff on schedule!");
    }

    /**
     * Saves the calculated staff hours as a separate sheet at the end of the workbook.
     * Contains staff names, the hours they worked, the days they worked and the number of days worked
     * @param workbook Input Excel workbook
     */
    private void saveHoursAsSheet(XSSFWorkbook workbook) {
        System.out.println("Saving hours as excel sheet to current workbook...");

        //Remove the hours sheet if it already exists
        if(workbook.getSheet(hoursSheetName) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(hoursSheetName));
        }

        //Creates new hours sheet
        XSSFSheet hoursSheet = workbook.createSheet(hoursSheetName);

        //Creates header for table
        Row headers = hoursSheet.createRow(0);
        Cell staffNames = headers.createCell(0);    //Name header (1)
        staffNames.setCellValue("Name");

        Cell weeklyHours = headers.createCell(1);   //Hours per week header (2)
        weeklyHours.setCellValue("Hours/wk");

        Cell daysWorked = headers.createCell(2);    //Days worked header (3)
        daysWorked.setCellValue("Days Worked");

        Cell dayCount = headers.createCell(3);      //Number of days worked header (4)
        dayCount.setCellValue("# of days worked");

        //Updates each cell of the row with the staff data. ie: staff name, hours, days, # of days
        Object[] rosterArray = StaffDataMap.keySet().toArray();
        for(int i = 1; i < StaffDataMap.size() + 1; i++) {
            Row newRow = hoursSheet.createRow(i);
            for(int j = 0; j < 4; j++) {
                Cell newColumnCell = newRow.createCell(j);
                //Staff names
                if(j == 0) {
                    newColumnCell.setCellValue(rosterArray[i - 1].toString());
                }
                //Staff hours
                else if(j == 1) {
                    newColumnCell.setCellValue(StaffDataMap.get(rosterArray[i - 1].toString()).getStaffHours());
                }
                //Days staff are shifted
                else if(j == 2) {
                    newColumnCell.setCellValue(StaffDataMap.get(rosterArray[i - 1].toString()).getShiftDates());
                }
                //Day count
                else {
                    newColumnCell.setCellValue(StaffDataMap.get(rosterArray[i - 1].toString()).getDayCount());
                }
            }
        }
        try (OutputStream fileOutput = new FileOutputStream(fileName)){
            workbook.write(fileOutput);
        }
        catch(FileNotFoundException fnf) {
            System.out.println(fileName + " is currently open. Please close the file to save hours sheet.");
            return;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Sheet saved as '" + hoursSheetName + "'");
    }

    /**
     * Wrapper method for inputFile()
     */
    public void inputExcelFile() {
        inputFile();
    }

    /**
     * Sets a new file name for the imported schedule
     * @param fileName new file name for schedule
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Helper method used to retrieve a roster list from excel sheet
     * @return The roster arraylist. Returns an empty arraylist if the roster file is incorrect.
     */
    private ArrayList<String> importRosterListFromXlsx() {
        ArrayList<String> rosterList= new ArrayList<>();
        try {
            File rosterFile = new File(rosterFileName);

            FileInputStream fis = new FileInputStream(rosterFile);
            XSSFWorkbook rosterWorkbook = new XSSFWorkbook(fis);
            XSSFSheet rosterSheet = rosterWorkbook.getSheetAt(0);

            int firstNameColIndex = getColIndex(rosterSheet, "First");
            int lastNameColIndex = getColIndex(rosterSheet, "Last");

            //Checks for valid column index
            if(firstNameColIndex == -1 || lastNameColIndex == -1) {
                System.out.println("Can't find first or last names. Incorrect Roster File!");
            }
            else {
                //Iterate over rows and adds staff to roster array list
                rosterList = getRosterList(rosterSheet, firstNameColIndex, lastNameColIndex);
            }
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found: " + fileName);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return rosterList;
    }

    /**
     * Sets the roster file name
     * @param rosterFileName Name of the roster file
     */
    public void setRosterFileName(String rosterFileName) {
        this.rosterFileName = rosterFileName;
    }

    /**
     * Helper method to return the index of the column containing search word
     * @param rosterSheet Current sheet being analyzed for search word
     * @return the int index of the column containing the search word. Returns -1 if it can't find the index
     */
    private int getColIndex(XSSFSheet rosterSheet, String searchWord) {

        //Iterates over all rows in sheet
        for(int i = 0; i < rosterSheet.getLastRowNum(); i++) {
            Row currentRow = rosterSheet.getRow(i);

            //Iterates over all Cells in row
            for(int j = 0; j < currentRow.getLastCellNum() - 1; j++) {
                Cell currentCell = currentRow.getCell(j);

                //Checks for null cells
                if(currentCell != null) {
                    //Checks for cells that match header
                    if (currentCell.getStringCellValue().toUpperCase().contains(searchWord.toUpperCase())) {
                        return j;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Returns an arraylist of staff names from the roster sheet
     * @param rosterSheet Excel sheet containing the names of the staff
     * @param firstCol Column index for first names
     * @param lastCol Column index for last names
     * @return The arraylist of staff names
     */
    private ArrayList<String> getRosterList(XSSFSheet rosterSheet, int firstCol, int lastCol) {
        ArrayList<String> rosterList = new ArrayList<>();
        for(int i = 0; i < rosterSheet.getLastRowNum() + 1; i++) {
            Row currentRow = rosterSheet.getRow(i);
            if(currentRow != null) {
                if(!currentRow.getCell(firstCol).getStringCellValue().contains("STAFF") && !currentRow.getCell(firstCol).getStringCellValue().contains("First")) {
                    //Retrieves the staff's full name from sheet
                    String firstName = currentRow.getCell(firstCol).getStringCellValue();
                    String lastName = currentRow.getCell(lastCol).getStringCellValue();
                    String fullName = firstName + " " + lastName;
                    rosterList.add(fullName);
                }
            }
        }
        return rosterList;
    }

    /**
     * Updates the roster file
     */
    public void updateRosterListFromTxt() {
        String rosterFileName = "roster.txt";
        File rosterFile = new File(rosterFileName);
        ArrayList<String> roster = new ArrayList<>();

        try {
            Scanner rosterFileScanner = new Scanner(rosterFile);
            while(rosterFileScanner.hasNextLine()) {
                roster.add(rosterFileScanner.nextLine());
            }
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found: " + rosterFileName);
        }

        System.out.println("Would you like to update the roster?");
        Scanner in = new Scanner(System.in);
        while (true) {
            String userResponse = in.next();
            if (userResponse.equalsIgnoreCase("yes") || userResponse.equalsIgnoreCase("y")) {
                while(true) {
                    System.out.println("Enter name to remove from roster");
                    String rosterName = in.next();
                    if(roster.contains(rosterName)) {
                        roster.remove(rosterName);
                        System.out.println("Would you like to remove another staff?");
                        while(true) {
                            userResponse = in.next();
                            if(userResponse.equalsIgnoreCase("no") || userResponse.equalsIgnoreCase("n")) {
                                rosterWriteToFile(rosterFileName, roster);
                                return;
                            }
                            else if(userResponse.equalsIgnoreCase("yes") || userResponse.equalsIgnoreCase("y")) {
                                break;
                            }
                            else {
                                System.out.println("Please enter yes (y) or no (n)");
                            }
                        }
                    }
                    else {
                        System.out.println("Staff does not exist on current roster. Please try again");
                    }
                }
            } else if (userResponse.equalsIgnoreCase("no") || userResponse.equalsIgnoreCase("n")) {
                System.out.println("Exiting...");
                rosterWriteToFile(rosterFileName, roster);
            } else {
                System.out.println("Please enter yes (y) or no (n)");
            }
        }
    }

    /**
     * Saves a separate excel file with the staff names and their shifts
     */
    public void saveNamesOnlySheets() {
        try {
            File sourceFile = new File(fileName);
            File scheduleMiniFile = new File("FINAL SCHEDULE - Names Only.xlsx");

            FileInputStream inputFis = new FileInputStream(sourceFile);
            XSSFWorkbook inputWorkbook = new XSSFWorkbook(inputFis);
            XSSFWorkbook outputWorkbook = new XSSFWorkbook();

            System.out.println("Creating sheet with only names and shifts...");
            int sheetCount = inputWorkbook.getNumberOfSheets();

            //Loops through each sheet in workbook
            for(int i = 0; i < sheetCount; i++) {
                XSSFSheet currentSheet = inputWorkbook.getSheetAt(i);

                outputWorkbook.createSheet(currentSheet.getSheetName());
                XSSFSheet currentMiniSheet = outputWorkbook.getSheetAt(i);
                currentMiniSheet.setZoom(55);

                //Checks for hours sheet at end of workbook
                if(currentSheet.getSheetName().equals(hoursSheetName)) {
                    break;
                }

                copyNamesToNewSheet(outputWorkbook, currentSheet, currentMiniSheet);

                //Sets the alternating color rule for the current sheet
                setFormatting(currentMiniSheet);
            }
            try (OutputStream fileOutput = new FileOutputStream(scheduleMiniFile)){
                outputWorkbook.write(fileOutput);
            }
            catch(FileNotFoundException fnf) {
                System.out.println(fileName + " is currently open. Please close the file.");
                return;
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            outputWorkbook.close();
            inputWorkbook.close();
            System.out.println("Finished creating sheet with names only!");
            System.out.println("File is saved in the same folder as this application.");
        }
        catch(IOException io) {
            System.out.println("Error with output file!");
            io.printStackTrace();
        }
    }

    /**
     * Loops through each sheet in the schedule, creates a new workbook, and fills it with staff data from schedule
     */
    public void saveLunches() {
        ArrayList<LunchData> lunchDataList;

        try {
            File sourceFile = new File(fileName);
            File scheduleLunchFile = new File("FINAL SCHEDULE - Lunch Data.xlsx");

            FileInputStream inputFis = new FileInputStream(sourceFile);
            XSSFWorkbook inputWorkbook = new XSSFWorkbook(inputFis);
            XSSFWorkbook lunchWorkbook = new XSSFWorkbook();

            System.out.println("Creating sheet with lunches, shift times, and shifted hours...");
            lunchWorkbook.createSheet("Lunch Data");
            XSSFSheet lunchSheet = lunchWorkbook.getSheetAt(0);

            int sheetCount = inputWorkbook.getNumberOfSheets();     //Number of sheets in the input workbook

            //Loops through each sheet in workbook
            for(int i = 0; i < sheetCount; i++) {
                XSSFSheet currentSheet = inputWorkbook.getSheetAt(i);

                //Checks for hours sheet at end of workbook
                if(currentSheet.getSheetName().equals(hoursSheetName)) {
                    break;
                }

                //Saves the data from the current sheet to an ArrayList
                lunchDataList = saveLunchDataToList(currentSheet);

                //Copies the data from the ArrayList to the lunch sheet
                copyLunchDataToNewSheet(lunchDataList, lunchSheet);

            }
            try (OutputStream fileOutput = new FileOutputStream(scheduleLunchFile)){
                lunchWorkbook.write(fileOutput);
            }
            catch(FileNotFoundException fnf) {
                System.out.println(fileName + " is currently open. Please close the file.");
                return;
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            lunchWorkbook.close();
            inputWorkbook.close();
            System.out.println("Finished creating sheet with lunches, shifts, and hours!");
            System.out.println("File is saved in the same folder as this application.");
        }
        catch(IOException io) {
            System.out.println("Error with output file!");
            io.printStackTrace();
        }
    }

    /**
     * Loops through a sheet and adds the staffed shifts to an arraylist in the form of LunchData.
     * Returns the updated arraylist .
     * @param currentSheet Current schedule for a given day, taken from the master schedule workbook
     * @return Updated arraylist containing all staff data from daily schedule sheet
     */
    private ArrayList<LunchData> saveLunchDataToList(XSSFSheet currentSheet) {
        ArrayList<LunchData> lunchDataList = new ArrayList<>();
        String date = createDayName(currentSheet.getSheetName());
        String staffName;
        String shiftStart = "";
        String shiftEnd = "";
        String lunchStart;
        String lunchEnd;
        double shiftHours;
        int currentShift;
        currentShift = 0;

        //Loops through all rows, checks for blank cells, and adds the data to an arraylist. Skips first row (date row)
        for(int j = 1; j < currentSheet.getLastRowNum(); j++) {
            Row currentRow = currentSheet.getRow(j);
            if(currentRow != null) {        //Checks if row exists
                if (currentRow.getCell(0) != null && !currentRow.getCell(0).getStringCellValue().equals("")) {       //Checks for un-staffed rows
                    if(currentRow.getCell(0).getStringCellValue().contains("Mid") ||                          //Checks for mid or night shift since shift start times are different
                            currentRow.getCell(0).getStringCellValue().contains("Night")) {
                        currentShift++;
                    }
                    else if (currentRow.getCell(2) != null) {   //Checks for non-null lunch rows
                        if(!headerWordsList.contains(currentRow.getCell(0).getStringCellValue())) {           //Checks for header names/staff-less row
                            if(currentRow.getCell(1).getStringCellValue().contains("-")) {                    //Verifies that the cell is a shift time
                                shiftStart = getShiftStart(currentRow.getCell(1).getStringCellValue());
                                shiftEnd = getShiftEnd(currentRow.getCell(1).getStringCellValue());
                            }
                            else {      //Sets times for unconventional shifts. ie: UNIT LEAD/CHARGE and accounts for different shifts
                                switch (currentShift) {
                                    case 0:
                                        shiftStart = "0700";
                                        shiftEnd = "1530";
                                        break;
                                    case 1:
                                        shiftStart = "1500";
                                        shiftEnd = "2330";
                                        break;
                                    case 2:
                                        shiftStart = "2300";
                                        shiftEnd = "0730";
                                        break;
                                    default:
                                        break;
                                }
                            }
                            //Compares name from schedule with name from roster list to obtain full name
                            ArrayList<String> rosterList = importRosterListFromXlsx();
                            staffName = currentRow.getCell(0).getStringCellValue();
                            for(String s : rosterList) {
                                if(s.contains(staffName) && s.charAt(1) == staffName.charAt(1)) {
                                    staffName = rosterList.get(rosterList.indexOf(s));
                                }
                                else {
                                    staffName = getFullNameFromNickname(staffName);
                                }
                            }
                            lunchStart = getShiftStart(currentRow.getCell(2).getStringCellValue());
                            lunchEnd = getShiftEnd(currentRow.getCell(2).getStringCellValue());
                            shiftHours = calculateHoursForShift(currentRow.getCell(1).getStringCellValue());
                            lunchDataList.add(new LunchData(staffName, shiftStart, shiftEnd, lunchStart, lunchEnd, shiftHours, date));
                        }
                    }
                }
            }
        }
        //Checks if lunches were entered
        if(lunchDataList.isEmpty()) {
            System.out.println("There are currently no lunches for this day. " +
                    "Please ensure lunches are input in the following format: xxxx-yyyy");
        }
        return lunchDataList;
    }

    /**
     * Searches a map for the nickname key and returns the fullname value
     * @param nickname nickname being searched for
     * @return Full name corresponding to nickname
     */
    private String getFullNameFromNickname(String nickname) {
        Map<String, String> nicknames = buildNicknameMap();
        if(nicknames.containsKey(nickname)) {
            return nicknames.get(nickname);
        }
        return nickname;
    }

    /**
     * Builds a map with specific nicknames
     * @return Map Keys: nicknames and Value: full names
     */
    private Map<String, String> buildNicknameMap() {
        Map<String, String> nicknames = new HashMap<>();
        addToNicknameMap(nicknames, "NANCY M.", "NANCY MENDOZA");
        addToNicknameMap(nicknames, "JA-SAI", "JA SAI PORTER");
        addToNicknameMap(nicknames, "MONIQUE", "LETTY YBARRA");
        addToNicknameMap(nicknames, "PATY", "PATRICIA ZARATE");
        addToNicknameMap(nicknames, "JASMINE C.", "JASMINE CENICEROS");
        addToNicknameMap(nicknames, "JASMINE N.", "JASMINE NKETIAH");
        addToNicknameMap(nicknames, "KIMIE", "KIMSOUR NIP");
        addToNicknameMap(nicknames, "ANDREA D.", "ANDREA DOUGLAS");
        addToNicknameMap(nicknames, "ANDREA R.", "ANDREA RAZO");
        addToNicknameMap(nicknames, "NANCY L.", "NANCY LOPEZ");
        addToNicknameMap(nicknames, "JENA", "JINJOO YANG");
        addToNicknameMap(nicknames, "SUNNY", "SUNINT MADAN");
        addToNicknameMap(nicknames,"MARTIN", "MARTIN NUNEZ");
        addToNicknameMap(nicknames, "FRANCES", "MARY-FRANCES SALONGA");

        return nicknames;
    }

    /**
     * Adds new entries to the nickname map. Key: Nickname, Value: Full Name
     * @param nicknames Map of nicknames
     * @param nickname Nickname being added
     * @param fullName Full name being added
     */
    private void addToNicknameMap(Map<String, String> nicknames, String nickname, String fullName) {
        if(!nicknames.containsKey(nickname)) {
            nicknames.put(nickname, fullName);
        }

    }

    /**
     * Goes through LunchData arraylist and adds to the end of the lunch sheet.
     * Creates a continuous sheet with all of the data from the arraylist
     * @param lunchDataList ArrayList containing lunch data for all shifts during a given day
     * @param currentLunchSheet Sheet containing all shift data (Lunch Data)
     */
    private void copyLunchDataToNewSheet(ArrayList<LunchData> lunchDataList, XSSFSheet currentLunchSheet) {
        int headerCount = 7;        //Number of headers

        //Creates headers for lunch data
        if(currentLunchSheet.getLastRowNum() == 0) {
            Row firstRow = currentLunchSheet.createRow(0);
            for(int i = 0; i < 7; i++) {
                firstRow.createCell(i, CellType.BLANK);
            }
            firstRow.getCell(0).setCellValue("Staff Name");
            firstRow.getCell(1).setCellValue("Date");
            firstRow.getCell(2).setCellValue("Shift Start");
            firstRow.getCell(3).setCellValue("Lunch Start");
            firstRow.getCell(4).setCellValue("Lunch End");
            firstRow.getCell(5).setCellValue("Shift End");
            firstRow.getCell(6).setCellValue("Hours");
        }

        //Copies data from LunchData arraylist to lunch sheet
        for(LunchData data : lunchDataList) {
            Row currentRow = currentLunchSheet.createRow(currentLunchSheet.getLastRowNum() + 1);
            for(int i = 0; i < headerCount; i++) {
                Cell currentCell = currentRow.createCell(i);

                //Sets cell data corresponding to header from LunchData
                switch(i) {
                    case 0:
                        currentCell.setCellValue(data.getStaffName());
                        break;
                    case 1:
                        currentCell.setCellValue(data.getDate());
                        break;
                    case 2:
                        currentCell.setCellValue(data.getShiftStart());
                        break;
                    case 3:
                        currentCell.setCellValue(data.getLunchStart());
                        break;
                    case 4:
                        currentCell.setCellValue(data.getLunchEnd());
                        break;
                    case 5:
                        currentCell.setCellValue(data.getShiftEnd());
                        break;
                    case 6:
                        currentCell.setCellValue(data.getShiftedHours());
                        break;
                    default:
                        break;
                }
            }
        }

        //Auto-sizes columns
        for(int i = 0; i < headerCount; i++) {
            currentLunchSheet.autoSizeColumn(i);
        }
    }

    /**
     * Removes the preceding day name from the input string
     * ie: "THUR10JUN2021" -> "10JUN2021"
     * @param date The date that needs shortening
     * @return The shortened version of the date without the day name
     */
    private String createDayName(String date) {
        for(int i = 0; i < date.length(); i++) {
            if(Character.isDigit(date.charAt(i))) {
                date = date.substring(i);
                break;
            }
        }
        return date;
    }

    /**
     * Copies the contents from one sheet to another
     * For use in copying the names and shift times from a master schedule to a mini version
     * @param outputWorkbook Current workbook that contains the master schedule
     * @param currentSheet Current sheet being copied from
     * @param currentMiniSheet New Sheet containing the data from the old sheet
     */
    private void copyNamesToNewSheet(XSSFWorkbook outputWorkbook, XSSFSheet currentSheet, XSSFSheet currentMiniSheet) {
        //Writes staff names (first 2 columns) to new file
        int newRow = 0;
        String cellContent;

        //Iterates over all rows in current sheet
        for(int k = 0; k < currentSheet.getLastRowNum(); k++) {
            Row row = currentSheet.getRow(k);
            if (row != null) {
                if(row.getCell(0) != null && !row.getCell(0).toString().equals("")) {
                    currentMiniSheet.createRow(newRow);
                    //Writes to new file
                    for (int j = 0; j < SHIFT_HOURS; j++) {
                        currentMiniSheet.getRow(newRow).createCell(j, CellType.BLANK);
                        cellContent = currentSheet.getRow(k).getCell(j).toString();
                        currentMiniSheet.getRow(newRow).getCell(j).setCellValue(cellContent);

                        //Sets the cell content and formatting
                        //Day shift header
                        if(cellContent.contains("[") || cellContent.contains("Day Shift")) {
                            currentMiniSheet.getRow(newRow).getCell(j).setCellStyle(new HourTrackerCellStyle(outputWorkbook, "day").getCellStyle());
                        }
                        //Mid shift header
                        else if(cellContent.contains("Mid Shift")) {
                            currentMiniSheet.getRow(newRow).getCell(j).setCellStyle(new HourTrackerCellStyle(outputWorkbook, "mid").getCellStyle());
                        }
                        //Night shift header
                        else if(cellContent.contains("Night Shift")) {
                            currentMiniSheet.getRow(newRow).getCell(j).setCellStyle(new HourTrackerCellStyle(outputWorkbook, "night").getCellStyle());
                        }
                        //"Staff Name" and "Shift" header
                        else if(cellContent.contains("Staff Name") || cellContent.contains("Shift")) {
                            currentMiniSheet.getRow(newRow).getCell(j).setCellStyle(new HourTrackerCellStyle(outputWorkbook, "header").getCellStyle());
                        }
                        //Shift times
                        else if(j == 1) {
                            currentMiniSheet.getRow(newRow).getCell(j).setCellStyle(new HourTrackerCellStyle(outputWorkbook, "times").getCellStyle());
                        }
                        //Staff names
                        else {
                            currentMiniSheet.getRow(newRow).getCell(j).setCellStyle(new HourTrackerCellStyle(outputWorkbook, "names").getCellStyle());
                        }
                    }
                    newRow++;
                }
            }
        }
    }

    /**
     * Creates the conditional rule for alternating rows in table and adds it to the current sheet
     * Used to color every other row for staff names
     * Needs to be done after copying contents from master schedule due to indexing
     */
    private void setFormatting(XSSFSheet currentSheet) {
        //Instance variables used in tracking the rows where each shift starts and ends
        int dayShiftFirstRow = 0;
        int dayShiftLastRow = 0;
        int midShiftFirstRow = 0;
        int midShiftLastRow = 0;
        int nightShiftFirstRow = 0;
        int nightShiftLastRow = 0;

        //Iterates over all rows in current sheet to find table breakpoints
        for(int k = 0; k < currentSheet.getLastRowNum(); k++) {
            //Writes to new file
            for (int j = 0; j < SHIFT_HOURS; j++) {
                String cellContent = currentSheet.getRow(k).getCell(j).toString();

                //Day shift header
                if(cellContent.contains("[") || cellContent.contains("Day Shift")) {
                    dayShiftFirstRow = k + 2;
                }

                //Mid shift header
                else if(cellContent.contains("Mid Shift")) {
                    //Merges cell
                    currentSheet.addMergedRegion(new CellRangeAddress(k, k, 0, 1));
                    dayShiftLastRow = k - 1;
                    midShiftFirstRow = k + 2;
                }

                //Night shift header
                else if(cellContent.contains("Night Shift")) {
                    //Merges cell
                    currentSheet.addMergedRegion(new CellRangeAddress(k, k, 0, 1));
                    midShiftLastRow = k - 1;
                    nightShiftFirstRow = k + 2;
                    nightShiftLastRow = currentSheet.getLastRowNum();
                }
            }
        }

        //Creates conditional formatting rule: every other row is set to "pale blue"
        SheetConditionalFormatting sheetFormatting = currentSheet.getSheetConditionalFormatting();
        ConditionalFormattingRule alternatingBlue = sheetFormatting.createConditionalFormattingRule("MOD(ROW(),2) = 0");
        PatternFormatting blueFill = alternatingBlue.createPatternFormatting();
        blueFill.setFillBackgroundColor(IndexedColors.PALE_BLUE.getIndex());

        //Checks for empty day shift and formats the shifts with alternating rows
        if(dayShiftFirstRow < dayShiftLastRow) {
            CellRangeAddress[] dayShiftRange = {new CellRangeAddress(dayShiftFirstRow, dayShiftLastRow, 0, 1)};
            sheetFormatting.addConditionalFormatting(dayShiftRange, alternatingBlue);
        }
        if(midShiftFirstRow < midShiftLastRow) {
            CellRangeAddress[] midShiftRange = {new CellRangeAddress(midShiftFirstRow, midShiftLastRow, 0, 1)};
            sheetFormatting.addConditionalFormatting(midShiftRange, alternatingBlue);
        }
        if(nightShiftFirstRow < nightShiftLastRow) {
            CellRangeAddress[] nightShiftRange = {new CellRangeAddress(nightShiftFirstRow, nightShiftLastRow, 0, 1)};
            sheetFormatting.addConditionalFormatting(nightShiftRange, alternatingBlue);
        }

        //Sizes columns to fit
        //Size = 256 * character width
        currentSheet.setColumnWidth(0, 9216);
        currentSheet.setColumnWidth(1, 7168);
    }

    /**
     * Helper method for updateRosterList to save the updated roster file
     * @param rosterFileName file name for roster file
     * @param roster ArrayList of the roster
     */
    private void rosterWriteToFile(String rosterFileName, ArrayList<String> roster) {
        try {
            PrintWriter pw = new PrintWriter(rosterFileName);
            for(String s : roster) {
                pw.println(s);
            }
            pw.flush();
            pw.close();
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found:" + rosterFileName);
        }
    }
}