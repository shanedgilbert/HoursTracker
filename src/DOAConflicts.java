import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DOAConflicts {
    //Instance variables
    Map<String, ArrayList<String>> dayStudyMap;

    //Constructor to set empty study map
    public DOAConflicts() {
        dayStudyMap = new HashMap<>();
    }

    /**
     * Adds a new day and its corresponding conflicts to the map
     * @param day day of study conflict
     * @param conflicts list of conflicting studies
     */
    public void addToConflictMap(String day, ArrayList<String> conflicts) {
        dayStudyMap.put(day, conflicts);
    }

    /**
     * Returns the map with days and its corresponding conflicting studies
     * @return The map of days and its studies
     */
    public Map<String, ArrayList<String>> getDayStudyMap() {
        return dayStudyMap;
    }
}
