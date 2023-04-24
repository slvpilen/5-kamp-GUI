package nidelv;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class SettingsController {

    
    
    @FXML
    private TextField googleDocURL;

    @FXML
    private ChoiceBox puljer;

    @FXML
    private void saveURL() {
        // save url to settings, settings will save it to file, if its ok, if not sending a error, who need to be cathehed here
        // then puljer needs to be update to the possible puljer

    }

    @FXML
    private void switchToLeaderBoard() throws IOException {
        App.setRoot("leaderBoard");
    }
}