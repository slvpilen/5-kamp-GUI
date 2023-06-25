package nidelv;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import nidelv.backend.GoogleDockReader;
import nidelv.backend.SaveAndReadSettings;

public class SettingsController implements Initializable {

    
    
    @FXML
    private TextField googleDocURL;

    //@FXML
    //private ChoiceBox<String> puljer;

    @FXML
    private Label errorLabel;

    @FXML
    private void saveURL() throws IOException {
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/nidelv/leaderBoard.fxml"));
        //loader.load();
        //LeaderBoardController leaderBoardController = loader.getController();
        try {
            GoogleDockReader.setSpreadsheetID(googleDocURL.getText());
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("connection to google doc is OK");
        }
        catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText(e.getMessage());
        }
        // save url to settings, settings will save it to file, if its ok, if not sending a error, who need to be cathehed here
        // then puljer needs to be update to the possible puljer

    }

    @FXML
    private void switchToLeaderBoard() throws IOException {
        App.setRoot("leaderBoard");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            googleDocURL.setText(SaveAndReadSettings.getGoogleDocURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }  
}