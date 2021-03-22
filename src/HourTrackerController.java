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
    private TextField inputFileName;

    @FXML
    private void handleGenerateButton(ActionEvent event) {
        String fileName = inputFileName.getText();
        inputFile(fileName);
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
        File input = fileChooser.showOpenDialog(owner);

        //Updates status field with current file
        statusTextField.setText("Current file: " + input.getName());
        inputFileName.setText(input.getName());
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
            ht.buildMapFromRoster();
            ht.inputExcelFile();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        staticOutputArea = outputTextField;
    }
}
