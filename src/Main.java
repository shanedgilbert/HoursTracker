import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.poi.ss.formula.functions.T;

//public class Main {
//    public static void main(String[] args) {
//        HourTracker ht = new HourTracker("FINAL SCHEDULE.xlsx");
//        ht.buildMapFromRoster();
//        ht.inputExcelFile();
//    }
//}

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Hour Tracker");

        Parent root = FXMLLoader.load(getClass().getResource("HourTrackerFXML.fxml"));

//        TextField fileNameField = new TextField("Enter schedule file name.");
//        HBox fileNameInputBox = new HBox();
//        fileNameInputBox.getChildren().addAll(fileNameField);
//
//        calcHoursButton = new Button("Click me");
//        calcHoursButton.setOnAction(e -> {
//            HourTracker ht = new HourTracker("FINAL SCHEDULE.xlsx");
//            ht.buildMapFromRoster();
//            ht.inputExcelFile();
//        });

        //Setting action for generate button
        //TODO
//        generate.setOnAction(e -> {
//            if(fileName.getText() == null || fileName.getText().isEmpty()) {
//                output.setText("Please enter a valid file name\n" +
//                        "ie: FINAL SCHEDULE.xlsx\n");
//            }
//            else {
//                HourTracker ht = new HourTracker(fileName.getText());
//                ht.buildMapFromRoster();
//                ht.inputExcelFile();
//            }
//        });

        Scene scene = new Scene(root, 640, 400);
        stage.setScene(scene);
        stage.show();
    }
}
