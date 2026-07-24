package proyect_batalla_naval.controller;

import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import proyect_batalla_naval.model.Session;
import proyect_batalla_naval.persistence.GameSaveManager;
import proyect_batalla_naval.utils.NavigationFacade;

import java.util.Optional;

/**
 * Controlador de la pantalla de bienvenida.
 * Permite al jugador ingresar su nickname e iniciar una nueva partida,
 * o continuar una partida previamente guardada.
 *
 * @author Andres Felipe Rengifo Rodriguez
 * @author Juan Pablo Gomez
 * @author Álvaro Iván Ospina Capera
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
     * Valida que el nickname no esté vacío, verifica si existe una partida
     * guardada y carga la vista del juego.
     */
    @FXML
    protected void onPlayClicked() {
        String nickname = nicknameField.getText().trim();

        if (nickname.isEmpty()) {
            errorLabel.setText("Por favor ingresa un nickname");
            return;
        }
        if (GameSaveManager.existsSave()) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Partida encontrada");

            alert.setHeaderText("Existe una partida guardada.");

            alert.setContentText("¿Desea continuar la partida anterior?");

            ButtonType continuar = new ButtonType("Continuar");

            ButtonType nueva = new ButtonType("Nueva partida");

            alert.getButtonTypes().setAll(continuar, nueva);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == continuar) {

                Session.nickname = GameSaveManager.loadNickname();

            } else {

                GameSaveManager.deleteSave();

                Session.nickname = nickname;

            }

        } else {

            Session.nickname = nickname;

        }

        NavigationFacade.goToGame();
    }
}
