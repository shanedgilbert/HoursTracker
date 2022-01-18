import java.util.LinkedList;

public class DOAStatus {
    //Instance variables
    LinkedList<String> dayList;
    LinkedList<String> studyList;

    //Constructor to set empty study set
    public DOAStatus() {
        dayList = new LinkedList<>();
        studyList = new LinkedList<>();
    }

    /**
     * Records the conflicting study and its day
     * @param day Day of conflict
     * @param studyName Name of study with conflict
     */
    public void addStudy(String day, String studyName) {
        dayList.add(day);
        studyList.add(studyName);
    }

    /**
     * Returns the string representation of the studies with DOA conflicts
     * @return String representation of the studies with DOA conflicts
     */
    public String getDays() {
        StringBuilder output = new StringBuilder();
        for(String s : dayList) {
            output.append(s);
        }
        return output.toString();
    }

    /**
     * Returns the string representation of the days with DOA conflicts
     * @return String representation of the days with DOA conflicts
     */
    public String getStudies() {
        StringBuilder output = new StringBuilder();
        for(String s : studyList) {
            output.append(s);
        }
        return output.toString();
    }

}
