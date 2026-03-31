module com.example.devtracker {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.devtracker to javafx.fxml;
    exports com.example.devtracker;
}