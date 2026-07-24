package proyect_batalla_naval.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Fachada que simplifica la navegación entre las pantallas de la aplicación.
 * <p>
 * Implementa el patrón de diseño <b>estructural Facade</b>: oculta detrás de
 * dos métodos con nombre de intención clara ({@link #goToWelcome()} y
 * {@link #goToGame()}) todos los detalles de bajo nivel de JavaFX
 * ({@link FXMLLoader}, {@link Scene}, manejo de errores de carga), que antes
 * quedaban expuestos directamente en {@code StartApp} y
 * {@code WelcomeController} a través de la clase {@code InsertScene}.
 * <p>
 * Reemplaza a la antigua clase {@code InsertScene}.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class NavigationFacade {

    /** Ventana principal de la aplicación. */
    private static Stage stage;

    /** Constructor privado: esta clase solo expone métodos estáticos de fachada. */
    private NavigationFacade() {
    }

    /**
     * Inicializa la fachada con la ventana principal de la aplicación.
     * Debe llamarse una única vez, típicamente desde {@code StartApp.start(...)}.
     *
     * @param primaryStage ventana principal de la aplicación
     */
    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /**
     * Navega a la pantalla de bienvenida (nickname del jugador).
     */
    public static void goToWelcome() {
        loadScene(Paths.INICIO);
    }

    /**
     * Navega a la pantalla principal del juego.
     */
    public static void goToGame() {
        loadScene(Paths.GAME);
    }

    /**
     * Carga un archivo FXML y lo muestra como la escena activa de la ventana.
     * Centraliza el manejo de errores de carga en un único lugar.
     *
     * @param fxmlPath ruta del recurso FXML a cargar
     * @throws IllegalStateException si el archivo FXML no pudo cargarse
     */
    private static void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationFacade.class.getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar la vista: " + fxmlPath, e);
        }
    }
}
