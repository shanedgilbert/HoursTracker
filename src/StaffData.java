import java.util.ArrayList;

public class StaffData {
    private ArrayList<String> shiftDates = new ArrayList<>();
    private double staffHours;
    private int dayCount = 0;

    public StaffData() {
        staffHours = 0;
    }

    public void updateShiftHours(double hours) {
        this.staffHours += hours;
    }

    public void addShiftDay(String day) {
        shiftDates.add(day);
        dayCount++;
    }

    public double getStaffHours() {
        return staffHours;
    }

    public int getDayCount() {
        return dayCount;
    }

    public String getShiftDates() {
        StringBuilder output = new StringBuilder();
        for(String s : shiftDates) {
            output.append(" ").append(s);
        }
        return output.toString();
    }
}
