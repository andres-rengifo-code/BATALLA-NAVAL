package proyect_batalla_naval;

import javafx.application.Application;

import javafx.stage.Stage;
import proyect_batalla_naval.utils.NavigationFacade;
import java.io.IOException;

public class StartApp extends Application {


    public static void main(String[] args){launch();} //Metodo main

    //Inicializador de la Ventana y la App
    @Override
    public void start(Stage stage) throws IOException {

        stage.setTitle("BATALLA NAVAL");
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        NavigationFacade.init(stage);
        NavigationFacade.goToWelcome();

    }
}
