import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HourTrackerController implements Initializable {
    static TextArea staticOutputArea;
    private String filePath = "";

    @FXML
    private Button generateButton;

    @FXML
    private Button generateNamesOnly;

    @FXML
    private Button cancelButton;

    @FXML
    private Button selectFileButton;

    @FXML
    private TextArea outputTextField;

    @FXML
    private TextArea statusTextField;

    @FXML
    private void handleGenerateButton(ActionEvent event) {
        inputFile(filePath, 0);
    }

    //TODO
    @FXML
    private void handleNamesButton(ActionEvent event) {
        inputFile(filePath, 1);
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSelectFileButton(ActionEvent event) {
        Window owner = selectFileButton.getScene().getWindow();

        final FileChooser fileChooser = new FileChooser();
        File inputFile = fileChooser.showOpenDialog(owner);

        if(inputFile != null) {
            //Updates status field with current file
            statusTextField.setText("Current file: " + inputFile.getName());
            outputTextField.setText("Press 'Generate' to calculate hours based on current file selection.\n" +
                    "Press 'Select File' if you would like to update the selected file.\n");
            filePath = inputFile.getAbsolutePath();
        }
    }

    private boolean verifyFileExtension(String fileName) {
        return fileName.contains(".xlsx");
    }

    /**
     *
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        staticOutputArea = outputTextField;
    }
}
