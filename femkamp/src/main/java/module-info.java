module nidelv {
    requires javafx.controls;
    requires javafx.fxml;

    opens nidelv to javafx.fxml;
    exports nidelv;
}
