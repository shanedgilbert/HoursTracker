public class TesterMain {
    public static void main(String[] args) {
        HourTracker ht = new HourTracker("FINAL SCHEDULE - Names.xlsx");
        ht.saveNamesOnlySheets();
    }
}
