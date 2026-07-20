package proyect_batalla_naval.exceptions;

/**
 * Excepción marcada (checked) que se lanza cuando se intenta colocar un
 * barco en una posición inválida del tablero (fuera de rango o superpuesta
 * con otro barco).
 *
 * Al ser una excepción marcada, obliga a quien invoca
 * {@code Board.placeShip(...)} a manejarla explícitamente con
 * {@code try/catch} o a propagarla con {@code throws}.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class InvalidPlacementException extends Exception {

    /**
     * Crea la excepción con un mensaje descriptivo del error.
     *
     * @param message detalle del motivo por el cual la colocación es inválida
     */
    public InvalidPlacementException(String message) {
        super(message);
    }
}
