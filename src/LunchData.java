public class LunchData {
    private final String staffName;     //Name of staff
    private final double shiftedHours;     //Hours shifted for the day
    private final String shiftStart;    //Start of shift in military time
    private final String shiftEnd;      //End of shift in military time
    private final String lunchStart;    //Start of lunch in military time
    private final String lunchEnd;      //End of lunch in military time
    private final String date;          //Date of shift

    /**
     * Default constructor that sets everything as blank/0
     */
    public LunchData() {
        staffName = "";
        shiftedHours = 0;
        shiftStart = "";
        shiftEnd = "";
        lunchStart = "";
        lunchEnd = "";
        date = "";
    }

    /**
     * Overloaded constructor that sets shift times, lunch times, and shifted hours
     * @param staffName Staff name, as a String
     * @param shiftStart Start of shift in military time, as a String
     * @param shiftEnd End of shift in military time, as a String
     * @param lunchStart Start of lunch in military time, as a String
     * @param lunchEnd End of lunch in military time, as a String
     * @param shiftedHours Total hours shifted for the day, as a double
     * @param date Date of shift
     */
    public LunchData(String staffName, String shiftStart, String shiftEnd, String lunchStart, String lunchEnd, double shiftedHours, String date) {
        this.staffName = staffName;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.lunchStart = lunchStart;
        this.lunchEnd = lunchEnd;
        this.shiftedHours = shiftedHours;
        this.date = date;
    }

    /**
     * Returns the name of the staff, as a String
     * @return The name of the staff
     */
    public String getStaffName() {
        return staffName;
    }

    /**
     * Retruns the shifted hours, as a double
     * @return The shifted hors for the day
     */
    public double getShiftedHours() {
        return shiftedHours;
    }

    /**
     * Returns the start of the shift, as a String
     * @return The start of the shift
     */
    public String getShiftStart() {
        return shiftStart;
    }

    /**
     * Returns the end of the shift, as a String
     * @return The end of the shift
     */
    public String getShiftEnd() {
        return shiftEnd;
    }

    /**
     * Returns the start of the lunch, as a String
     * @return The start of the lunch
     */
    public String getLunchStart() {
        return lunchStart;
    }

    /**
     * Returns the end of the lunch, as a String
     * @return The end of the lunch
     */
    public String getLunchEnd() {
        return lunchEnd;
    }

    /**
     * Returns the date of the shift, as a String
     * @return The date of the shift
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns the String representation of the staff name, date, shift start, shift end,
     * lunch start, lunch end, and shifted hours
     * @return String representation of the staff name, date, shift start, shift end, lunch start, lunch end, and shifted hours
     */
    public String toString() {
        return  "Staff Name: " + staffName +
                " Date: " + date +
                " Shift start: " + shiftStart +
                " Shift end: " + shiftEnd +
                " Lunch start: " + lunchStart +
                " Lunch end: " + lunchEnd +
                " Shifted hours: " + shiftedHours;
    }
}

