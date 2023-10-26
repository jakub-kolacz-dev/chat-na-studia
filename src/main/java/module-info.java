module com.example.demo12 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.demo12 to javafx.fxml;
    exports com.example.demo12;
    exports com.example.server;
    opens com.example.server to javafx.fxml;
}