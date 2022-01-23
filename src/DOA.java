import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * 0. Iterate through DOA sheet and save staff/procedure to map
 * 1. Iterate through each sheet
 * 2. Iterate over each staff
 *  2.1. Iterate through each column per staff
 *  2.2. Compare the color of the procedure with the studies list. Same color = same study
 * 3. Save staff with no DOA to a HashMap. Only staff that aren't delegated
 * 4. Create a new sheet and display all staff with conflict
 */
public class DOA {
    //Instance variables
    private final String scheduleFileName;
    private final String doaFileName;
    private final String DOA_SHEET_NAME = "DOA Analysis";
    private final Map<String, DOAConflicts> doaConflictsMap = new HashMap<>();
    private Map<String, ArrayList<String>> doaStaffStatusMap = new HashMap<>();
    private final ArrayList<String> scheduleDays = new ArrayList<>();
    private final ArrayList<String> nonExistentStudies = new ArrayList<>();

    //Words to skip on schedule
    String[] tabooWords = {"Staff Name", "Staff", "Shift", "Day Shift 0700-1530", "Mid Shift 1500-2330", "Night Shift 2300-0730",
            "FLOAT", "Fishbowl", "TEAM LEAD", "MEALS", "", " "};
    List<String> tabooWordsList = Arrays.asList(tabooWords);
    String[] headerWords = {"Staff Name", "Staff", "Shift", "", " ", "[", "]"};
    List<String> headerWordsList = Arrays.asList(headerWords);

    /**
     * Constructor to set the file name
     */
    public DOA() {
        scheduleFileName = "";
        doaFileName = "";
    }

    /**
     * Overloaded constructor to set fileName
     * @param scheduleFileName name of the schedule xlsx file
     * @param doaFileName name of the DOA xlsx file
     */
    public DOA(String scheduleFileName, String doaFileName) {
        this.scheduleFileName = scheduleFileName;
        this.doaFileName = doaFileName;
        doaStaffStatusMap = analyzeDOAWorkbook();
    }

    /**
     * Reads in the Excel schedule file and returns it as a workbook object
     * @return The workbook object representing the xlsx workbook
     */
    private XSSFWorkbook inputFile(String fileName) {
        System.out.println("Searching for " + fileName);
        XSSFWorkbook Workbook = new XSSFWorkbook();
        try {
            File inputFile = new File(fileName);
            FileInputStream fis = new FileInputStream(inputFile);
            Workbook = new XSSFWorkbook(fis);

            System.out.println(fileName + " found");
            System.out.println("Importing " + fileName + "...");
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found: " + fileName);
        }
        catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return Workbook;
    }

    /**
     * Analyzes the workbook object by iterating through each sheet and checking if each staff is delegated to their assigned study
     */
    private void analyzeScheduleWorkbook() {
        XSSFWorkbook scheduleWorkbook = inputFile(scheduleFileName);
        try {
            scheduleWorkbook = removeDOASheet(scheduleWorkbook);                //Removed DOA Analysis sheet if it exists
            int sheetCount = scheduleWorkbook.getNumberOfSheets();

            //Loops through each sheet in workbook
            for (int i = 0; i < sheetCount; i++) {
                Sheet currentSheet = scheduleWorkbook.getSheetAt(i);            //Current sheet
                scheduleDays.add(currentSheet.getSheetName());

                //Checks for DOA sheet at end of workbook
                if (currentSheet.getSheetName().equals(DOA_SHEET_NAME)) {
                    break;
                }
                HashMap<Color, String> studyMap = saveStudies(currentSheet);    //Map of studies
                readScheduleSheet(currentSheet, studyMap);                      //Analyzes each sheet for staff delegation
            }
            saveDOAAnalysisAsSheet(scheduleWorkbook);                           //Saves a report at the end of the workbook
            scheduleWorkbook.close();
            displayNonexistentStudies();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove the DOA analysis sheet if it already exists
     * @param workbook Workbook being checked for DOA analysis sheet
     * @return the workbook without the DOA Analysis sheet
     */
    private XSSFWorkbook removeDOASheet(XSSFWorkbook workbook) {

        if(workbook.getSheet(DOA_SHEET_NAME) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(DOA_SHEET_NAME));
        }
        return workbook;
    }


    /**
     * Reads the DOA Tracker and saves the staff to and their studies to a map
     * @return Returns the map with staff and their delegated studies
     */
    private Map<String, ArrayList<String>> analyzeDOAWorkbook() {
        Map<String, ArrayList<String>> delegatedStaffStudiesMap = new HashMap<>();
        ArrayList<String> studiesList = importStudies();
        XSSFWorkbook doaWorkbook = inputFile(doaFileName);
        Map<Integer, String> studyColumnMap = new HashMap<>();            //Tracks the study columns

        Sheet currentSheet = doaWorkbook.getSheetAt(0);             //Current sheet. There is only 1 sheet that matters to us (first)
        for (Row row : currentSheet) {
            if(row.getRowNum() == 0) {                                    //First row represents column names
                for(int cellNum = 3; cellNum < row.getLastCellNum(); cellNum++) {
                    String studyName = row.getCell(cellNum).getStringCellValue();

                    //Iterates over the study list to find the base name of the study
                    Iterator<String> studyIterator = studiesList.iterator();
                    while(studyIterator.hasNext()) {
                        String study = studyIterator.next();
                        if(studyName.contains(study)) {                   //Finds the study matching from the study list and saves the column index
                            studyColumnMap.put(cellNum, study);
                            break;
                        }
                        if(!studyIterator.hasNext()) {                    //Studies that aren't found in the list
                            if(!nonExistentStudies.contains(studyName)) {
                                nonExistentStudies.add(studyName);
                            }
                        }
                    }
                }
            }

            //Checks which staff are delegated for all studies
            else if(!checkForEmptyRow(row)) {                                           //Staff rows
                String name = row.getCell(0).getStringCellValue() + " " + row.getCell(1).getStringCellValue();
                for(int cellNum = 3; cellNum < row.getLastCellNum(); cellNum++) {
                    Cell currentCell = row.getCell(cellNum);
                    if(!checkForEmptyCell(currentCell)) {                               //Checks for empty cells
                        String studyName = studyColumnMap.get(cellNum);
                        delegatedStaffStudiesMap.putIfAbsent(name, new ArrayList<>());
                        if(!delegatedStaffStudiesMap.get(name).contains(studyName)) {   //Checks if the study is already on the list
                            delegatedStaffStudiesMap.get(name).add(studyName);
                        }
                    }
                }
            }
        }
        return delegatedStaffStudiesMap;
    }

    /**
     * Checks for empty rows
     * @param row Row being checked
     * @return True if the row is empty. False if the row contains content
     */
    private boolean checkForEmptyRow(Row row) {
        if(row == null) {
            return true;
        }
        if(row.getLastCellNum() <= 0) {
            return true;
        }
        for(int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if(cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the cell is empty. Returns false if the cell contains content
     * @param cell Cell being analyzed
     * @return Boolean status of content within cell
     */
    private boolean checkForEmptyCell(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    /**
     * Import the study file to create an arraylist of studies
     * @return The arraylist of studies
     */
    private ArrayList<String> importStudies() {
        String studiesFileName = "studies.txt";
        File studiesFile = new File(studiesFileName);
        ArrayList<String> studiesList = new ArrayList<>();
        try {
            Scanner input = new Scanner(studiesFile);
            while(input.hasNextLine()) {
                studiesList.add(input.nextLine());
            }
        }
        catch(FileNotFoundException fnf) {
            System.out.println(fnf.getMessage());
        }
        return studiesList;
    }

    /**
     * Reads in the schedule sheet and the staff delegation map to cross-reference.
     * Verifies that scheduled staff is delegated for their assigned study
     * @param currentSheet Daily schedule sheet
     * @param studyMap Map of staff and their delegated studies
     */
    private void readScheduleSheet(Sheet currentSheet, HashMap<Color, String> studyMap) {
        //Iterates over all rows in current sheet
        Map<String, ArrayList<String>> staffShiftedStudies = new HashMap<>();
        ArrayList<String> studiesList = importStudies();
        for(Row row : currentSheet) {
            String currentStaffName = "";
            for(int i = 0; i < 8; i++) {
                if(i == 0) {                                //Staff column
                    Cell currentCell = row.getCell(i);
                    if(checkForEmptyCell(currentCell)) {    //Checks for blank cells
                        break;
                    }
                    currentStaffName = currentCell.getStringCellValue();

                    //Checks for non-staff cells
                    if(tabooWordsList.contains(currentStaffName) || (currentStaffName.contains("[") && currentStaffName.contains("]"))) {
                        break;
                    }
                }
                if(i > 2) {                                 //Procedure columns
                    Cell currentCell = row.getCell(i);
                    String procedureStudyName = "";

                    if(checkForEmptyCell(currentCell)) {    //Checks for blank cell
                        break;
                    }
                    //Gets the study name for the current procedure
                    if(!tabooWordsList.contains(currentCell.getStringCellValue().replaceFirst("\\s++$", ""))) {
                        Color procedureColor = currentCell.getCellStyle().getFillForegroundColorColor();
                        if (studyMap.containsKey(procedureColor)) {                 //Checks if the color represents a study
                            procedureStudyName = studyMap.get(procedureColor);      //Retrieves the study name from its color
                        }
                    }

                    if(!procedureStudyName.equals("")) {
                        //Iterates over the study list to find the base name of the study
                        Iterator<String> studyIterator = studiesList.iterator();
                        while (studyIterator.hasNext()) {
                            String study = studyIterator.next();
                            if (procedureStudyName.contains(study)) {                    //Finds the study matching from the study list and saves the column index
                                procedureStudyName = study;
                                break;
                            }
                            if (!studyIterator.hasNext()) {                              //Studies that aren't found in the list
                                if(!nonExistentStudies.contains(procedureStudyName)) {
                                    nonExistentStudies.add(procedureStudyName);
                                }
                            }
                        }

                        staffShiftedStudies.putIfAbsent(currentStaffName, new ArrayList<>());
                        if (!staffShiftedStudies.get(currentStaffName).contains(procedureStudyName)) {   //Checks if the study is already on the list
                            staffShiftedStudies.get(currentStaffName).add(procedureStudyName);
                        }
                    }
                }
            }
        }
        staffShiftedStudies = getSimplifiedStaffName(staffShiftedStudies);      //Simplifies and normalizes the names for comparison
        doaStaffStatusMap = getSimplifiedStaffName(doaStaffStatusMap);
        String[] staffArray = staffShiftedStudies.keySet().toArray(new String[0]);
        for(String name : doaStaffStatusMap.keySet()) {
            String matchingName = getMatchingSubstring(name, staffArray);       //Search the array for matching names
            if(!Objects.equals(matchingName, "")) {                          //Matching names found
                ArrayList<String> shiftedStudies = staffShiftedStudies.get(matchingName);
                shiftedStudies.removeAll(doaStaffStatusMap.get(name));

                if(!shiftedStudies.isEmpty()) {
                    doaConflictsMap.putIfAbsent(name, new DOAConflicts());
                    doaConflictsMap.get(name).addToConflictMap(currentSheet.getSheetName(), shiftedStudies);
                }
            }
        }
    }

    /**
     * Searches the input array for the input string and returns the matching string
     * @param inputString string being searched for in the array
     * @param stringArray array being searched
     * @return matching string. "" if no match is found
     */
    private String getMatchingSubstring(String inputString, String[] stringArray) {
        Optional<String> foundString = Arrays.stream(stringArray).parallel().filter(inputString::contains).findAny();
        return foundString.orElse("");
    }

    /**
     * Simplifies and normalizes the staff names for comparison. Reduces the names to either first name only or first name with last initial
     * @param inputMap The map with a first name key set that needs to reduced
     * @return new map with reduced names key set
     */
    private Map<String, ArrayList<String>> getSimplifiedStaffName(Map<String, ArrayList<String>> inputMap) {
        Map<String, ArrayList<String>> tempMap = new HashMap<>();
        for(String staffName : inputMap.keySet()) {
            ArrayList<String> names = new ArrayList<>(Arrays.asList(staffName.split(" ")));
            names = removeSpace(names);
            if (names.size() > 1) {
                String tempName;
                tempName = names.get(0) + " " + names.get(1).charAt(0);
                tempMap.putIfAbsent(tempName, inputMap.get(staffName));
            }
            else {
                tempMap.put(staffName, inputMap.get(staffName));
            }
        }
        return tempMap;
    }

    /**
     * Removes all spaces from arraylist
     * @param stringList arraylist with spaces
     * @return arraylist without spaces
     */
    private ArrayList<String> removeSpace(ArrayList<String> stringList) {
        while(stringList.contains("")) {
            stringList.remove("");
        }
        return stringList;
    }

    /**
     * Displays the studies that don't exist in the DOA
     */
    private void displayNonexistentStudies() {
        System.out.println("The following studies do not exist in the '" + doaFileName + "' list:");
        for (String nonExistentStudy : nonExistentStudies) {
            System.out.println(nonExistentStudy);
        }
    }

    /**
     * Saves the study conflicts with staff as an additional sheet at the end of the workbook
     * @param workbook Excel workbook being analyzed for study/DOA conflicts
     */
    private void saveDOAAnalysisAsSheet(XSSFWorkbook workbook) {
        System.out.println("Saving DOA Analysis as Excel sheet to current workbook");

        workbook = removeDOASheet(workbook);

        //Creates new DOA sheet
        XSSFSheet doaSheet = workbook.createSheet(DOA_SHEET_NAME);

        //Creates header for table
        Row headers = doaSheet.createRow(0);

        //Name header
        Cell staffNames = headers.createCell(0);
        staffNames.setCellValue("Name");

        //Conflict days header
        for(int day = 1; day < scheduleDays.size() + 1; day++) {
            Cell conflictDays = headers.createCell(day);
            conflictDays.setCellValue(scheduleDays.get(day - 1));
        }

        //Loops over the staff names
        Object[] doaStatusArray = doaConflictsMap.keySet().toArray();
        for(int i = 1; i < doaConflictsMap.size() + 1; i++) {
            Row newRow = doaSheet.createRow(i);                         //Creates new row per staff
            String staffName = doaStatusArray[i - 1].toString();        //Staff name

            Map<String, ArrayList<String>> staffConflictDays = doaConflictsMap.get(staffName).dayStudyMap;
            for(int j = 0; j < scheduleDays.size() + 1; j++) {
                Cell newColumnCell = newRow.createCell(j);
                //Staff names
                if(j == 0) {
                    newColumnCell.setCellValue(staffName);
                }
                //Conflict days
                else {
                    if(staffConflictDays.containsKey(scheduleDays.get(j - 1))) {
                        newColumnCell = newRow.createCell(j);
                        newColumnCell.setCellValue(staffConflictDays.get(scheduleDays.get(j - 1)).toString());
                    }
                }
            }
        }
        try (OutputStream fileOutput = new FileOutputStream(scheduleFileName)){
            workbook.write(fileOutput);
        }
        catch(FileNotFoundException fnf) {
            System.out.println(scheduleFileName + " is currently open. Please close the file to save hours sheet.");
            return;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Sheet saved as '" + DOA_SHEET_NAME + "'");
    }

    /**
     * Iterated through the sheet and saves the study and its cell color to a map.
     * Returns the study/color map
     * @param currentSheet Current sheet of the workbook with studies and colors
     * @return The mapping of study names to cell color
     */
    private HashMap<Color, String> saveStudies(Sheet currentSheet) {
        HashMap<Color, String> studyMap = new HashMap<>();

        //Iterates over all rows in current sheet
        for(Row row : currentSheet) {
            String studyName;       //Name of the study
            Color studyColor;       //Cell color of the study

            //In house studies
            Cell currentCell = row.getCell(12);
            if(currentCell != null && currentCell.getCellType() != CellType.BLANK) {
                studyName = currentCell.getStringCellValue();                               //Study name

                //Checks for header row
                if (!studyName.contains("In House")) {
                    studyColor = currentCell.getCellStyle().getFillForegroundColorColor();  //Saves study cell background color
                    if(!studyMap.containsKey(studyColor)) {
                        studyMap.put(studyColor, studyName);                                //Saves study name and color to map
                    }
                }
            }

            //OPV
            currentCell = row.getCell(17);
            if(currentCell != null && currentCell.getCellType() != CellType.BLANK) {
                studyName = currentCell.getStringCellValue();                               //Study name

                //Checks for header row
                if (!studyName.contains("OPV") || !studyName.contains("Protocol")) {
                    studyColor = currentCell.getCellStyle().getFillForegroundColorColor();  //Saves study cell background color
                    if(!studyMap.containsKey(studyColor)) {
                        studyMap.put(studyColor, studyName);                                //Saves study name and color to map
                    }
                }
            }
        }
        return studyMap;
    }
    public static void main(String[] args) {
        String schedule = "schedule.xlsx";
        String tracker = "tracker.xlsx";
        DOA test = new DOA(schedule, tracker);
        test.analyzeScheduleWorkbook();
    }
}
