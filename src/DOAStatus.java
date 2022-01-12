import java.util.HashMap;
import java.util.Map;

public class DOAStatus {
    //Instance variables
    private final Map<String, String> studies;

    //Constructor to set empty study set
    public DOAStatus() {
        studies = new HashMap<>();
    }

    public void addStudy(String day, String studyName) {
        studies.put(day, studyName);
    }
}
