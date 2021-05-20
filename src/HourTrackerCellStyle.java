import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class HourTrackerCellStyle {
    XSSFWorkbook workbook;
    String shift;

    /**
     * Constructor that sets shift name and workbook
     * @param workbook workbook to create CellStyle
     * @param shift String name of the shift
     */
    public HourTrackerCellStyle(XSSFWorkbook workbook, String shift) {
        this.shift = shift;
        this.workbook = workbook;
    }

    /**
     * Returns the CellStyle corresponding to the shift. Colors differ per shift
     * @return CellStyle based on shift
     */
    public CellStyle getCellStyle() {
        CellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setFont(getFont(true));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //Switch case for different shifts (since they use different formatting)
        switch(shift) {
            case ("day"):
                cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case ("mid"):
                cellStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case ("night"):
                cellStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                cellStyle.setFont(getFont(false));
                break;
            case("header"):
                cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case("times"):
                cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case("names"):
                cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                cellStyle.setAlignment(HorizontalAlignment.LEFT);
                break;
            default:
                break;
        }

        //Sets the cell border as thick
        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);

        return cellStyle;
    }

    /**
     * Returns the custom default font used for the sheet
     * @return custom font
     */
    private Font getFont(boolean blackText) {
        XSSFFont cellFont = workbook.createFont();
        cellFont.setBold(true);
        cellFont.setItalic(false);
        cellFont.setFontName("Arial");
        cellFont.setFontHeightInPoints((short)12);

        if(blackText) {
            cellFont.setColor(IndexedColors.BLACK.getIndex());
        }
        else {
            cellFont.setColor(IndexedColors.WHITE.getIndex());
        }

        return cellFont;
    }
}
