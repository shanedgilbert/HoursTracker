public class LunchData {
    private final int shiftedHours;     //Hours shifted for the day
    private final String shiftStart;    //Start of shift in military time
    private final String shiftEnd;      //End of shift in military time
    private final String lunchStart;    //Start of lunch in military time
    private final String lunchEnd;      //End of lunch in military time

    /**
     * Default constructor that sets everything as blank/0
     */
    public LunchData() {
        this.shiftStart = "";
        this.shiftEnd = "";
        this.lunchStart = "";
        this.lunchEnd = "";
        this.shiftedHours = 0;
    }

    /**
     * Overloaded constructor that sets shift times, lunch times, and shifted hours
     * @param shiftStart Start of shift in military time, as a String
     * @param shiftEnd End of shift in military time, as a String
     * @param lunchStart Start of lunch in military time, as a String
     * @param lunchEnd End of lunch in military time, as a String
     * @param shiftedHours Total hours shifted for the day
     */
    public LunchData(String shiftStart, String shiftEnd, String lunchStart, String lunchEnd, int shiftedHours) {
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.lunchStart = lunchEnd;
        this.lunchEnd = lunchEnd;
        this.shiftedHours = shiftedHours;
    }

    /**
     * Retruns the shifted hours, as an int
     * @return The shifted hors for the day
     */
    public int getShiftedHours() {
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
     * Returns the String representation of the shift start, shift end,
     * lunch start, lunch end, and shifted hours
     * @return String representation of shift start, shift end, lunch start, lunch end, and shifted hours
     */
    public String toString() {
        return  "Shift start: " + shiftStart +
                " Shift end: " + shiftEnd +
                " Lunch start: " + lunchStart +
                " Lunch end: " + lunchEnd +
                " Shifted hours: " + shiftedHours;
    }
}

