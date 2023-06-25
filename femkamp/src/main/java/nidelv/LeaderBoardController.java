package nidelv;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javafx.fxml.FXML;
import nidelv.backend.GoogleDockReader;
import nidelv.backend.SaveAndReadSettings;
import nidelv.backend.Validation;

public class LeaderBoardController {

    private String googleDockURL;

    @FXML
    private void switchToSettings() throws IOException {
        App.setRoot("settings");
    }



    
}
