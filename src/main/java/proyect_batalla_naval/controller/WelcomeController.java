package proyect_batalla_naval.controller;

import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import proyect_batalla_naval.model.Session;
import proyect_batalla_naval.utils.InsertScene;
import proyect_batalla_naval.utils.Paths;

/**
 * Controlador de la pantalla de bienvenida.
 * Permite al jugador ingresar su nickname e iniciar una nueva partida.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class WelcomeController {

    /** Campo de texto para el nickname del jugador. */
    @FXML
    private TextField nicknameField;

    /** Label para mostrar mensajes de error. */
    @FXML
    private Label errorLabel;

    /**
     * Maneja el evento de clic en el botón "Jugar".
     * Valida que el nickname no esté vacío y carga la vista del juego.
     */
    @FXML
    protected void onPlayClicked() {
        String nickname = nicknameField.getText().trim();

        if (nickname.isEmpty()) {
            errorLabel.setText("Por favor ingresa un nickname");
            return;
        }
        Session.nickname = nickname;
        InsertScene.setScene(Paths.GAME);
    }
}
