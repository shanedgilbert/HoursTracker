import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.WindowEvent;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Objects;
//TODO: Comments
public class Main extends Application {

    private final PipedInputStream pipeIn = new PipedInputStream();
    private final PipedInputStream pipeIn2 = new PipedInputStream();
    Thread errorThrower;
    private Thread reader;
    private Thread reader2;
    private boolean quit;
    private TextArea outputTextArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //Load FXML layout
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("HourTrackerFXML.fxml")));

        outputTextArea = HourTrackerController.staticOutputArea;

        stage.setTitle("Hour Tracker");
        Scene scene = new Scene(root, 640, 400);
        stage.setScene(scene);
        stage.show();

        //Thread for output stream
        executeReaderThreads();

        //Closing thread on close
        stage.setOnCloseRequest(windowEvent -> {
            closeThread();
            Platform.exit();
            System.exit(0);
        });
    }

    public void executeReaderThreads() {
        try {
            PipedOutputStream pout = new PipedOutputStream(this.pipeIn);
            System.setOut(new PrintStream(pout, true));
        }
        catch(IOException | SecurityException ignored) {}
        try {
            PipedOutputStream pout2  = new PipedOutputStream(this.pipeIn2);
            System.setErr(new PrintStream(pout2, true));
        }
        catch(IOException | SecurityException ignored) {}
        ReaderThread obj = new ReaderThread(pipeIn, pipeIn2, errorThrower, reader, reader2, quit, outputTextArea);
    }

    synchronized void closeThread() {
        System.out.println("Closing...");
        this.quit = true;
        notifyAll();
        try {
            this.reader.join(1000L);
            this.pipeIn.close();
        }
        catch(Exception ignored) {}
        try {
            this.reader2.join(1000L);
            this.pipeIn2.close();
        }
        catch(Exception ignored) {}
        System.exit(0);
    }
}
