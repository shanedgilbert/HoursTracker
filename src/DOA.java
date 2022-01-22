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
    private final String doaSheetName = "DOA Analysis";
    final static int STUDY_COLUMN = 13;
    Map<String, DOAConflicts> doaConflictsMap = new HashMap<>();
    Map<String, List<String>> doaStaffStatusMap = new HashMap<>();

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
        System.out.println("Searching for file...");
        XSSFWorkbook Workbook = new XSSFWorkbook();
        try {
            File inputFile = new File(fileName);
            FileInputStream fis = new FileInputStream(inputFile);
            Workbook = new XSSFWorkbook(fis);

            System.out.println("File found");
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
            int sheetCount = scheduleWorkbook.getNumberOfSheets();

            //Loops through each sheet in workbook
            for (int i = 0; i < sheetCount; i++) {
                Sheet currentSheet = scheduleWorkbook.getSheetAt(i);            //Current sheet

                //Checks for DOA sheet at end of workbook
                if (currentSheet.getSheetName().equals(doaSheetName)) {
                    break;
                }
                HashMap<Color, String> studyMap = saveStudies(currentSheet);    //Map of studies
                readScheduleSheet(currentSheet, studyMap);                      //Analyzes each sheet for staff delegation
            }
            saveDOAAnalysisAsSheet(scheduleWorkbook);                           //Saves a report at the end of the workbook
            scheduleWorkbook.close();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads the DOA Tracker and saves the staff to and their studies to a map
     * @return Returns the map with staff and their delegated studies
     */
    private Map<String, List<String>> analyzeDOAWorkbook() {
        Map<String, List<String>> delegatedStaffStudiesMap = new HashMap<>();
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
                            System.out.println(studyName + " does not exist in the 'studies.txt' list!");
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

    //TODO: check if there are conflicting studies by comparing DOA to staffed procedures
    private void readScheduleSheet(Sheet currentSheet, HashMap<Color, String> studyMap) {
        //Iterates over all rows in current sheet
        Map<String, List<String>> staffShiftedStudies = new HashMap<>();
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
                                System.out.println(procedureStudyName + " does not exist in the 'studies.txt' list!");
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
        //TODO: Compare arraylists
        for (Map.Entry<String, List<String>> entry : staffShiftedStudies.entrySet())
            System.out.println("Key = " + entry.getKey() +
                    ", Value = " + entry.getValue());
    }

    /**
     * Saves the study conflicts with staff as an additional sheet at the end of the workbook
     * @param workbook Excel workbook being analyzed for study/DOA conflicts
     */
    private void saveDOAAnalysisAsSheet(XSSFWorkbook workbook) {
        System.out.println("Saving DOA Analysis as Excel sheet to current workbook");

        //Remove the DOA analysis sheet if it already exists
        if(workbook.getSheet(doaSheetName) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(doaSheetName));
        }

        //Creates new DOA sheet
        XSSFSheet doaSheet = workbook.createSheet(doaSheetName);

        //Creates header for table
        Row headers = doaSheet.createRow(0);
        Cell staffNames = headers.createCell(0);    //Name header (1)
        staffNames.setCellValue("Name");

        Cell conflictDays = headers.createCell(1);  //Conflict days header (2)
        conflictDays.setCellValue("Conflict Days");

        Cell studyNames = headers.createCell(2);    //Study names header (3)
        studyNames.setCellValue("Study Names");

        //Updates each cell of the row with the staff data. ie: staff name, conflict days, study names
        Object[] doaStatusArray = doaConflictsMap.keySet().toArray();
        for(int i = 1; i < doaConflictsMap.size() + 1; i++) {
            Row newRow = doaSheet.createRow(i);
            for(int j = 0; j < 3; j++) {
                Cell newColumnCell = newRow.createCell(j);
                //Staff names
                if(j == 0) {
                    newColumnCell.setCellValue(doaStatusArray[i - 1].toString());
                }
                //Conflict days
                else if(j == 1) {
                    newColumnCell.setCellValue(doaConflictsMap.get(doaStatusArray[i - 1].toString()).getDays());
                }
                //Study names
                else {
                    newColumnCell.setCellValue(doaConflictsMap.get(doaStatusArray[i - 1].toString()).getStudies());
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
        System.out.println("Sheet saved as '" + scheduleFileName + "'");
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
