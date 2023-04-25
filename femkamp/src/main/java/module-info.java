module nidelv {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.opencsv;
    requires javafx.graphics;
    requires java.base;


    opens nidelv to javafx.fxml;
    exports nidelv;
}
