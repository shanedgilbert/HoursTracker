import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class HourTracker {
    String fileName;
    int maxCol;
    ArrayList<String> staffRoster = new ArrayList<>();
    ArrayList<String> staffHours = new ArrayList<>();

    public HourTracker() {
        maxCol = 0;
        fileName = "";
    }

    //Builds the staff roster
    //CAN BE DONE DYNAMICALLY DURING inputExcelFile()
    public void rosterBuilder(String name) {
        if(!staffRoster.contains(name)) {
            staffRoster.add(name);
        }
    }

    //Gets hours from schedule
    public void scheduleHours() {
        fileName = "FINAL SCHEDULE.xlsx";
        maxCol = 2;
        inputExcelFile(fileName);
    }

    private void removeTabooWords(ArrayList<String> list) {
        String[] tabooWords = {"Staff Name", "Shift", "Day Shift 0700-1530", "Mid Shift 1500-2330", "Night Shift 2300-0730"};
        for (String tabooWord : tabooWords) {
            list.remove(tabooWord);     //TODO: REMOVE ALL ITERATIONS OF THE WORD
        }
    }

    private void inputExcelFile(String fileName) {
        try {
            File inputFile = new File(fileName);
            FileInputStream fis = new FileInputStream(inputFile);

            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            //Returns the first sheet
            XSSFSheet sheet = workbook.getSheetAt(0);   //Loop for other sheets?

            //Traverses each row
            for (Row row : sheet) {
                //For each row, traverses each column
                Iterator<Cell> cellIterator = row.cellIterator();
                int columnCount = 0;
                //Reads in the maxCol columns (col1: name, col2: shift)
                while (cellIterator.hasNext() && columnCount < maxCol) {
                    Cell cell = cellIterator.next();

                    switch (cell.getCellType()) {
                        case STRING:    //String
//                            System.out.println(cell.getStringCellValue() + "\t\t\t");
                            if(columnCount == 0) {
                                rosterBuilder(cell.getStringCellValue());
                            }
                            else if(columnCount == 1) {
                                staffHours.add(cell.getStringCellValue());
                            }
                            break;
                        case NUMERIC:   //Double
//                            if(columnCount == 1) {
//                                staffHours.add(cell.getStringCellValue());
//                            }
//                            System.out.println("Column: " + columnCount + " Cell type: " + cell.getCellType());
//                            System.out.println(cell.getNumericCellValue() + "\t\t\t");
                            break;
                        default:
                            break;
                    }
                    columnCount++;
                }
                System.out.println("");
            }
        }
        catch(FileNotFoundException fnf) {
            System.out.println("File not found: " + fileName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        staffRoster.remove(0);      //Removes the date (First line)
        removeTabooWords(staffRoster);
        removeTabooWords(staffHours);
//        for (String s : staffRoster) {
//            System.out.println(s);
//        }
        for(int i = 0; i < staffRoster.size(); i++) {
            System.out.println(staffRoster.get(i));
            System.out.println(staffHours.get(i));
        }
    }
}
