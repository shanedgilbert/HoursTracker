import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TesterMain {
    public static void main(String[] args) {
//        HourTracker ht = new HourTracker("FINAL SCHEDULE - Names.xlsx");
//        ht.saveNamesOnlySheets();
        XSSFWorkbook workbook = new XSSFWorkbook();
        HourTracker ht = new HourTracker("FINAL SCHEDULE - WiP.xlsx");
        ht.saveLunches();
    }
}
