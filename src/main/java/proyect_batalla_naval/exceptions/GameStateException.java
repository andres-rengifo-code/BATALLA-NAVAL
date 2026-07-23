package proyect_batalla_naval.exceptions;

/**
 * Excepción no marcada (unchecked) que se lanza cuando se intenta ejecutar
 * una acción del juego (disparar, iniciar partida, etc.) que no es válida
 * para el {@code GameState} actual, por ejemplo disparar cuando no es el
 * turno correspondiente o cuando el juego ya terminó.
 *
 * @author Andres Felipe Rengifo Rodriguez
 * @author Juan Pablo Gomez
 * @author Álvaro Iván Ospina Capera
 */
public class GameStateException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo del error.
     *
     * @param message detalle del motivo por el cual la acción no es válida
     */
    public GameStateException(String message) {
        super(message);
    }
}
