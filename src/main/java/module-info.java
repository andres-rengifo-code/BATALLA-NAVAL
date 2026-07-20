module proyect_batalla_naval {
    requires javafx.controls;
    requires javafx.fxml;

    opens proyect_batalla_naval to javafx.fxml;
    opens proyect_batalla_naval.controller to javafx.fxml;
    opens proyect_batalla_naval.model to javafx.fxml;
    opens proyect_batalla_naval.utils to javafx.fxml;
    opens proyect_batalla_naval.exceptions to javafx.fxml;
    exports proyect_batalla_naval;
    exports proyect_batalla_naval.controller;
    exports proyect_batalla_naval.model;
    exports proyect_batalla_naval.utils;
    exports proyect_batalla_naval.exceptions;
}
