module com.example.proyect_batalla_naval {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.proyect_batalla_naval to javafx.fxml;
    exports com.example.proyect_batalla_naval;
}