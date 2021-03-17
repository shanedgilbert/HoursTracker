import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
    Button calcHoursButton;

    //Main driver
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Hour Tracker");

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

        //Creates grid container for input field
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        //File name input field
        final TextField fileName = new TextField();
        fileName.setPromptText("Enter name of schedule.");
        fileName.setPrefColumnCount(10);
        fileName.getText();
        GridPane.setConstraints(fileName, 0, 0);
        grid.getChildren().add(fileName);

        //Calculate button
        Button generate = new Button("Generate");
        GridPane.setConstraints(generate, 1, 0);
        grid.getChildren().add(generate);

        //Clear button
        Button clear = new Button("Clear");
        GridPane.setConstraints(clear, 1, 1);
        grid.getChildren().add(clear);

        //Text area for output
        TextArea output = new TextArea();
        GridPane.setConstraints(output, 2, 0);

        //Setting action for generate button
        //TODO
        generate.setOnAction(e -> {
            if(fileName.getText() == null || fileName.getText().isEmpty()) {
                output.setText("Please enter a valid file name\n" +
                        "ie: FINAL SCHEDULE.xlsx\n");
            }
            else {
                HourTracker ht = new HourTracker(fileName.getText());
                ht.buildMapFromRoster();
                ht.inputExcelFile();
            }
        });

        //Setting action for clear button
        //TODO

        StackPane layout = new StackPane();
        layout.getChildren().addAll(grid);

        Scene scene = new Scene(layout, 600, 480);
        stage.setScene(scene);
        stage.show();
    }
}
