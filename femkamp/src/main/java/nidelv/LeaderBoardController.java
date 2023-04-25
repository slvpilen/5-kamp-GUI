package nidelv;

import java.io.IOException;
import javafx.fxml.FXML;
import nidelv.backend.SaveAndReadSettings;
import nidelv.backend.Validation;

public class LeaderBoardController {

    private String googleDockURL;

    @FXML
    private void switchToSettings() throws IOException {
        App.setRoot("settings");
    }

    public void setURL(String googleDockURL) throws IOException {
        Validation.googleDockURL(googleDockURL);
        this.googleDockURL = googleDockURL;
        SaveAndReadSettings.saveURL(this.googleDockURL);
    }

    
}
