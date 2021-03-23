import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class HourTracker {
    private String fileName;
    private final String hoursSheetName = "Weekly Hours";
    private int maxCol;
    Map<String, StaffData> roster = new HashMap<>();

    /**
     * Empty constructor
     */
    public HourTracker() {
        maxCol = 0;
        fileName = "";
    }

    /**
     * Overloaded constructor with fileName
     * @param fileName name of file being imported
     */
    public HourTracker(String fileName) {
        maxCol = 2;
        this.fileName = fileName;
    }

    /**
     * Calculates the hours from each shift
     * @param hours hour range from schedule (xxxx-xxxx)
     * @return Hours for shift
     */
    private double calculateHoursForShift(String hours) {
        double totalHours = 0;

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
                //System.out.println("Sheet: " + (i + 1));
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
                    while(cellIterator.hasNext() && currentColumn < maxCol) {
                        Cell cell = cellIterator.next();
                        if(cell.getColumnIndex() < maxCol) {
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
//                                    System.out.println("Staff " + currentStaffName + " doesn't exist on the current roster!");
//                                    System.out.println("Adding " + currentStaffName + " to roster...");

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

    private void saveHoursAsSheet(XSSFWorkbook workbook) {
        System.out.println("Saving hours as excel sheet to current workbook...");

        //Remove the hours sheet if it already exists
        if(workbook.getSheet(hoursSheetName) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(hoursSheetName));
        }

        //Creates new hours sheet
        Sheet hoursSheet = workbook.createSheet(hoursSheetName);

        Object[] rosterArray = roster.keySet().toArray();
        for(int i = 0; i < roster.size(); i++) {
            Row newRow = hoursSheet.createRow(i);
            for(int j = 0; j < 3; j++) {
                Cell newColumnCell = newRow.createCell(j);
                if(j == 0) {
                    newColumnCell.setCellValue(rosterArray[i].toString());
                }
                else if(j == 1) {
                    newColumnCell.setCellValue(roster.get(rosterArray[i].toString()).getStaffHours());
                }
                else {
                    newColumnCell.setCellValue(roster.get(rosterArray[i].toString()).getShiftDates());
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
            if (userResponse.toLowerCase().equals("yes") || userResponse.toLowerCase().equals("y")) {
                while(true) {
                    System.out.println("Enter name to remove from roster");
                    String rosterName = in.next();
                    if(roster.contains(rosterName)) {
                        roster.remove(rosterName);
                        System.out.println("Would you like to remove another staff?");
                        while(true) {
                            userResponse = in.next();
                            if(userResponse.toLowerCase().equals("no") || userResponse.toLowerCase().equals("n")) {
                                RosterWriteToFile(rosterFileName, roster);
                                return;
                            }
                            else if(userResponse.toLowerCase().equals("yes") || userResponse.toLowerCase().equals("y")) {
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
            } else if (userResponse.toLowerCase().equals("no") || userResponse.toLowerCase().equals("n")) {
                System.out.println("Exiting...");
                RosterWriteToFile(rosterFileName, roster);
            } else {
                System.out.println("Please enter yes (y) or no (n)");
            }
        }
    }

    private void RosterWriteToFile(String rosterFileName, ArrayList<String> roster) {
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