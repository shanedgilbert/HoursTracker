import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
    Map<String, DOAStatus> doaStatusMap = new HashMap<>();

    //Words to skip on schedule
    String[] tabooWords = {"Staff Name", "Staff", "Shift", "Day Shift 0700-1530", "Mid Shift 1500-2330", "Night Shift 2300-0730", "", " "};
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

                //Checks for hours sheet at end of workbook
                if (currentSheet.getSheetName().equals(doaSheetName)) {
                    break;
                }
                HashMap<Short, String> studyMap = saveStudies(currentSheet);    //Map of studies
                readScheduleSheet(currentSheet, studyMap);                              //Analyzes each sheet for staff delegation
            }
            saveDOAAnalysisAsSheet(scheduleWorkbook);                           //Saves a report at the end of the workbook
            scheduleWorkbook.close();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //TODO
    private void analyzeDOAWorkbook() {
        XSSFWorkbook doaWorkbook = inputFile(doaFileName);
        Sheet currentSheet = doaWorkbook.getSheetAt(0);
    }

    //TODO
    private void readScheduleSheet(Sheet currentSheet, HashMap<Short, String> studyMap) {

        //Iterates over all rows in current sheet
        for(Row row : currentSheet) {
            Iterator<Cell> cellIterator = row.cellIterator();
            String currentStaffName;
            int currentCol = 0;
            while (cellIterator.hasNext()) {
                Cell currentCell = cellIterator.next();
                if(currentCol == 0) {                   //Staff column
                    currentStaffName = currentCell.getStringCellValue();

                    //Checks for non-staff cells
                    if(tabooWordsList.contains(currentStaffName) || (currentStaffName.contains("[") && currentStaffName.contains("]"))) {
                        break;
                    }
                }
                if(currentCol > 2) {                    //Procedure columns
                    //Checks for blank cell
                    if(currentCell == null || currentCell.getCellType() == CellType.BLANK) {
                        break;
                    }
                    //Gets the study name for the current procedure
                    short procedureColor = currentCell.getCellStyle().getFillBackgroundColor();
                    String procedureStudyName = "";
                    if(studyMap.containsKey(procedureColor)) {
                        procedureStudyName = studyMap.get(procedureColor);
                    }
                    //TODO: Check if staff is delegated

                }
                currentCol++;
            }
        }
    }

    //TODO
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

        Cell conflictDays = headers.createCell(1);   //Conflict days header (2)
        conflictDays.setCellValue("Conflict Days");

        Cell studyNames = headers.createCell(2);    //Study names header (3)
        studyNames.setCellValue("Study Names");

        //Updates each cell of the row with the staff data. ie: staff name, conflict days, study names
        //TODO: get data from map
        Object[] doaStatusArray = doaStatusMap.keySet().toArray();
        for(int i = 1; i < doaStatusMap.size() + 1; i++) {
            Row newRow = doaSheet.createRow(i);
            for(int j = 0; j < 3; j++) {
                Cell newColumnCell = newRow.createCell(j);
                //Staff names
                if(j == 0) {
                    newColumnCell.setCellValue();
                }
                //Staff hours
                else if(j == 1) {
                    newColumnCell.setCellValue();
                }
                //Days staff are shifted
                else if(j == 2) {
                    newColumnCell.setCellValue();
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
    private HashMap<Short, String> saveStudies(Sheet currentSheet) {
        HashMap<Short, String> studyMap = new HashMap<>();

        //Iterates over all rows in current sheet
        for(Row row : currentSheet) {
            Iterator<Cell> cellIterator = row.cellIterator();
            String studyName;       //Name of the study
            short studyColor;       //Cell color of the study
            int currentCol = 0;     //Column containing list of studies

            while (cellIterator.hasNext()) {
                Cell currentCell = cellIterator.next();
                if(currentCol == 13 && currentCell != null && currentCell.getCellType() != CellType.BLANK) {
                    studyName = currentCell.getStringCellValue();

                    //Checks for header row
                    if (studyName.contains("In House")) {
                        break;
                    }
                    studyColor = currentCell.getCellStyle().getFillBackgroundColor();   //Saves study cell background color
                    studyMap.put(studyColor, studyName);                                //Saves study name and color to map
                }
                currentCol++;
            }
        }
        return studyMap;
    }

}
