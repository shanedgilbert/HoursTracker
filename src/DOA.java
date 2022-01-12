import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private final String fileName;
    private String doaFileName;
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
        fileName = "";
    }

    /**
     * Overloaded constructor to set fileName
     * @param fileName name of the xlsx file
     */
    public DOA(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Reads in the Excel file and returns it as a workbook object
     * @return The workbook object representing the xlsx workbook
     */
    private XSSFWorkbook inputFile() {
        System.out.println("Searching for file...");
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            File inputFile = new File(fileName);
            FileInputStream fis = new FileInputStream(inputFile);
            workbook = new XSSFWorkbook(fis);

            System.out.println("File found");
            System.out.println("Importing workbook...");
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found: " + fileName);
        }
        catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        System.out.println("Finished DOA Analysis!");
        return workbook;
    }

    /**
     * Analyzes the workbook object by iterating through each sheet and checking if each staff is delegated to their assigned study
     * @param workbook The xlsx workbook object being analyzed
     */
    private void analyzeWorkbook(XSSFWorkbook workbook) {
        try {
            int sheetCount = workbook.getNumberOfSheets();

            //Loops through each sheet in workbook
            for (int i = 0; i < sheetCount; i++) {
                Sheet currentSheet = workbook.getSheetAt(i);                    //Current sheet

                //Checks for hours sheet at end of workbook
                if (currentSheet.getSheetName().equals(doaSheetName)) {
                    break;
                }
                HashMap<Short, String> studyMap = saveStudies(currentSheet);    //Map of studies
                readSheet(currentSheet, studyMap);                              //Analyzes each sheet for staff delegation
            }
            saveDOAAnalysisAsSheet(workbook);                                   //Saves a report at the end of the workbook
            workbook.close();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //TODO
    private void readSheet(Sheet currentSheet, HashMap<Short, String> studyMap) {

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
