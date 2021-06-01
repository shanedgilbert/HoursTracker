import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class HourTracker {
    private String fileName;
    private final String hoursSheetName = "Weekly Hours";
    final static int MAX_COL = 2;
    Map<String, StaffData> roster = new HashMap<>();

    int dayShiftFirstRow = 0;
    int dayShiftLastRow = 0;
    int midShiftFirstRow = 0;
    int midShiftLastRow = 0;
    int nightShiftFirstRow = 0;
    int nightShiftLastRow = 0;

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
        return totalHours;
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
                roster.put(rosterFileScanner.nextLine(), new StaffData());
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

            //Words to skip on schedule
            String[] tabooWords = {"Staff Name", "Staff", "Shift", "Day Shift 0700-1530", "Mid Shift 1500-2330", "Night Shift 2300-0730", "", " "};
            List<String> tabooWordsList = Arrays.asList(tabooWords);

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
                    while(cellIterator.hasNext() && currentColumn < MAX_COL) {
                        Cell cell = cellIterator.next();
                        if(cell.getColumnIndex() < MAX_COL) {
                            if (currentColumn == 0) {
                                currentStaffName = cell.getStringCellValue();
                            } else if (currentColumn == 1) {
                                //Checks for names in roster and updates their hours
                                if (roster.containsKey(currentStaffName)) {
                                    double shiftHours = calculateHoursForShift(cell.getStringCellValue());
                                    if (shiftHours > 6) {
                                        shiftHours -= 0.5;
                                    }

                                    //double newHours = staffCurrentHours + shiftHours;
                                    roster.get(currentStaffName).updateShiftHours(shiftHours);
                                    roster.get(currentStaffName).addShiftDay(currentSheet.getSheetName().substring(0, 2));
                                }

                                //Checks for empty data ("taboo words")
                                else if (tabooWordsList.contains(currentStaffName) || (currentStaffName.contains("[") && currentStaffName.contains("]"))) {
                                    break;
                                }

                                //Checks if staff doesn't exist and adds
                                else {
                                    double shiftHours = calculateHoursForShift(cell.getStringCellValue());
                                    if (shiftHours > 6) {
                                        shiftHours -= 0.5;
                                    }
                                    StaffData currentStaffData = new StaffData();
                                    currentStaffData.updateShiftHours(shiftHours);
                                    currentStaffData.addShiftDay(currentSheet.getSheetName().substring(0, 2));
                                    roster.put(currentStaffName, currentStaffData);
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
        dayCount.setCellValue("Number of days worked");

        //Updates each cell of the row with the staff data. ie: staff name, hours, days, # of days
        Object[] rosterArray = roster.keySet().toArray();
        for(int i = 1; i < roster.size() + 1; i++) {
            Row newRow = hoursSheet.createRow(i);
            for(int j = 0; j < 4; j++) {
                Cell newColumnCell = newRow.createCell(j);
                //Staff names
                if(j == 0) {
                    newColumnCell.setCellValue(rosterArray[i - 1].toString());
                }
                //Staff hours
                else if(j == 1) {
                    newColumnCell.setCellValue(roster.get(rosterArray[i - 1].toString()).getStaffHours());
                }
                //Days staff are shifted
                else if(j == 2) {
                    newColumnCell.setCellValue(roster.get(rosterArray[i - 1].toString()).getShiftDates());
                }
                //Day count
                else {
                    newColumnCell.setCellValue(roster.get(rosterArray[i - 1].toString()).getDayCount());
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
     * Updates the roster file
     */
    public void updateRosterList() {
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

                copyToNewSheet(outputWorkbook, currentSheet, currentMiniSheet);

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
     * Copies the contents from one sheet to another
     * For use in copying the names and shift times from a master schedule to a mini version
     * @param outputWorkbook Current workbook that contains the master schedule
     * @param currentSheet Current sheet being copied from
     * @param currentMiniSheet New Sheet containing the data from the old sheet
     */
    private void copyToNewSheet(XSSFWorkbook outputWorkbook, XSSFSheet currentSheet, XSSFSheet currentMiniSheet) {
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
                    for (int j = 0; j < MAX_COL; j++) {
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

        //Iterates over all rows in current sheet to find table breakpoints
        for(int k = 0; k < currentSheet.getLastRowNum(); k++) {
            //Writes to new file
            for (int j = 0; j < MAX_COL; j++) {
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

        //GET CellRangeAddress[]
        CellRangeAddress[] dayShiftRange   = {new CellRangeAddress(dayShiftFirstRow, dayShiftLastRow, 0, 1)};
        CellRangeAddress[] midShiftRange   = {new CellRangeAddress(midShiftFirstRow, midShiftLastRow, 0, 1)};
        CellRangeAddress[] nightShiftRange = {new CellRangeAddress(nightShiftFirstRow, nightShiftLastRow, 0, 1)};
        sheetFormatting.addConditionalFormatting(dayShiftRange, alternatingBlue);
        sheetFormatting.addConditionalFormatting(midShiftRange, alternatingBlue);
        sheetFormatting.addConditionalFormatting(nightShiftRange, alternatingBlue);

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