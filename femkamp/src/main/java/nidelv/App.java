package nidelv;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nidelv.backend.GoogleDockReader;
import nidelv.backend.SaveAndReadSettings;
import nidelv.backend.Validation;

import java.io.IOException;


/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    
    @Override
    public void start(Stage stage) throws IOException {
        String firstScrene = "";

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nidelv/leaderBoard.fxml"));
        loader.load();
        //LeaderBoardController leaderBoardController = loader.getController();
        try {
            String URL = SaveAndReadSettings.getGoogleDocURL();
            GoogleDockReader.setSpreadsheetID(URL);
            firstScrene = "leaderBoard";
        }
        catch (Exception e) {
            System.out.println(e);
            firstScrene = "settings";
        }
        scene = new Scene(loadFXML(firstScrene), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}