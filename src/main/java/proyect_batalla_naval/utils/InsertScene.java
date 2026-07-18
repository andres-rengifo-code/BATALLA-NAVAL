package proyect_batalla_naval.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class InsertScene {

    public static Stage interfaz;

    public static void setStage(Stage stage) {
        interfaz = stage;
    }

    public static void setScene(String paths){
        FXMLLoader cargarInterfaz = new FXMLLoader(InsertScene.class.getResource(paths));
        try {
            Parent pane = cargarInterfaz.load();
            Scene scene = new Scene(pane);
            interfaz.setScene(scene);
            interfaz.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}