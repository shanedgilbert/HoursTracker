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
    private Button cancelButton;

    @FXML
    private Button selectFileButton;

    @FXML
    private TextArea outputTextField;

    @FXML
    private TextArea statusTextField;

    @FXML
    private void handleGenerateButton(ActionEvent event) {
        inputFile(filePath);
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

    private void inputFile(String fileName) {
        if(fileName == null || fileName.isEmpty()) {
            statusTextField.setText("No file selected\n" +
                    "Please select a file above...");
        }
        else if(!verifyFileExtension(fileName)) {
            statusTextField.setText("Wrong file type!");
        }
        else {
            HourTracker ht = new HourTracker(fileName);
            //ht.buildMapFromRoster();  //Used when building an initial roster map from a roster.txt file
            ht.inputExcelFile();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        staticOutputArea = outputTextField;
    }
}
