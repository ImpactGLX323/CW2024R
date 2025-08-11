module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

        opens com.example.demo to javafx.graphics;
        opens com.example.demo.view to javafx.fxml;
}