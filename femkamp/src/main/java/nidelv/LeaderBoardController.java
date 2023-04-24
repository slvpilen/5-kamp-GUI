package nidelv;

import java.io.IOException;
import javafx.fxml.FXML;

public class LeaderBoardController {

    @FXML
    private void switchToSettings() throws IOException {
        App.setRoot("settings");
    }
}
