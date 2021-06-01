import java.util.ArrayList;

/**
 * Manages the shifted hours, number of shifted days, and currently shifted days for one instance of a staff member
 */
public class StaffData {
    //Instance variables
    private final ArrayList<String> shiftDates = new ArrayList<>();
    private double staffHours;
    private int dayCount = 0;

    /**
     * Default constructor. Sets staffHours to 0
     */
    public StaffData() {
        staffHours = 0;
    }

    /**
     * Adds hours to the current staffHours count
     * @param hours Amount of hours to add
     */
    public void updateShiftHours(double hours) {
        this.staffHours += hours;
    }

    /**
     * Adds a String day to the current ArrayList of shifted days
     * @param day String day ie: "MO" for Monday or "TU" for Tuesday
     */
    public void addShiftDay(String day) {
        shiftDates.add(day);
        dayCount++;
    }

    /**
     * Returns the count of hours
     * @return The count of hours
     */
    public double getStaffHours() {
        return staffHours;
    }

    /**
     * Returns the number of shifted days
     * @return The number of shifted days
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Returns the contents of the shiftDates ArrayList as a string
     * @return The contents of the shiftDates ArrayList as a string
     */
    public String getShiftDates() {
        StringBuilder output = new StringBuilder();
        for(String s : shiftDates) {
            output.append(" ").append(s);
        }
        return output.toString();
    }
}
