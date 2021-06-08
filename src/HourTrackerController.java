import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HourTrackerController implements Initializable {
    static TextArea staticOutputArea;
    private String filePath = "";
    private String rosterFilePath = "";

    @FXML
    private Button cancelButton;

    @FXML
    private Button selectFileButton;

    @FXML
    private TextArea outputTextField;

    @FXML
    private TextArea statusTextField;

    @FXML
    private TextArea rosterTextField;

    /**
     * Handles the generate button to create a sheet at the end of the workbook to track staff hours/work days
     */
    @FXML
    private void handleGenerateButton() {
        inputFile(filePath, 0);
    }

    /**
     * Handles the names only button
     */
    @FXML
    private void handleNamesButton() {
        inputFile(filePath, 1);
    }

    /**
     * Handles the lunch data button
     */
    @FXML
    private void handleGenerateLunchButton() {
        inputRosterFile(filePath, rosterFilePath);
    }

    /**
     * Closes the program
     */
    @FXML
    private void handleCancelButton() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the event of the select file button. Updates the outputTextField to let user know.
     */
    @FXML
    private void handleSelectFileButton() {
        Window owner = selectFileButton.getScene().getWindow();

        final FileChooser fileChooser = new FileChooser();
        File inputFile = fileChooser.showOpenDialog(owner);

        if(inputFile != null) {
            //Updates status field with current file
            statusTextField.setText("Current file: " + inputFile.getName());
            outputTextField.setText("Press 'Generate' to calculate hours based on current file selection.\n" +
                    "Press 'Select File' if you would like to update the selected file.\n" +
                    "Please be patient as the first generation takes a while.\n");
            filePath = inputFile.getAbsolutePath();
        }
    }

    /**
     * Handles the event of importing roster.
     */
    @FXML
    private void handleImportRosterButton() {
        Window owner = selectFileButton.getScene().getWindow();

        final FileChooser fileChooser = new FileChooser();
        File inputFile = fileChooser.showOpenDialog(owner);

        if(inputFile != null) {
            //Updates status field with current file
            rosterTextField.setText("Current file: " + inputFile.getName());
            outputTextField.setText(outputTextField.getText() + "Roster file selected.\n" +
                    "Press 'Select File' if a schedule has not already been selected.\n" +
                    "Press 'Generate Data' to create lunch data for the input schedule.\n");
            rosterFilePath = inputFile.getAbsolutePath();
        }
    }

    /**
     * Verifies the extension of the input file. Returns true if the file name contains .xlsx
     * @param fileName name of the file
     * @return true if file name contains .xlsx. False otherwise
     */
    private boolean verifyFileExtension(String fileName) {
        return fileName.contains(".xlsx");
    }

    /**
     * Links the staff hour analysis and staff only sheet with the generate button
     * @param fileName File extension for the Excel file we want to modify/analyze
     * @param option 0 = hour analysis. 1 = create staff only sheet
     */
    private void inputFile(String fileName, int option) {
        if(fileName == null || fileName.isEmpty()) {
            statusTextField.setText("No file selected\n" +
                    "Please select a file above...");
        }
        else if(!verifyFileExtension(fileName)) {
            statusTextField.setText("Wrong file type!");
        }
        else {
            HourTracker ht = new HourTracker(fileName);
            //Hour Tracker
            if(option == 0) {

                //ht.buildMapFromRoster();  //Used when building an initial roster map from a roster.txt file
                ht.inputExcelFile();
            }
            //Staff only sheet
            else if(option == 1) {
                ht.saveNamesOnlySheets();
            }
        }
    }

    /**
     * Links the lunch data creation with the generate lunch data button
     * @param filePath File path of the schedule file
     * @param rosterFilePath File path of the roster file
     */
    private void inputRosterFile(String filePath, String rosterFilePath) {
        if(filePath == null || filePath.isEmpty()) {
            statusTextField.setText("No schedule file selected\n" +
                    "Please select a file above...");
        }
        if(rosterFilePath == null || rosterFilePath.isEmpty()) {
            rosterTextField.setText("No roster file selected\n" +
                    "Please select a file below...");
        }
        else if(!verifyFileExtension(filePath) || !verifyFileExtension(rosterFilePath)) {
            statusTextField.setText("Wrong file type!");
        }
        else {
            HourTracker ht = new HourTracker(filePath);
            ht.setRosterFileName(rosterFilePath);
            ht.saveLunches();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        staticOutputArea = outputTextField;
    }
}
