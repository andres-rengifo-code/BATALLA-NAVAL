package proyect_batalla_naval.exceptions;

/**
 * Excepción no marcada (unchecked) que se lanza cuando se intenta disparar
 * o consultar una celda con coordenadas fuera de los límites del tablero
 * (0-9 en fila y columna).
 *
 * Se modela como {@link RuntimeException} porque representa un error de
 * programación (coordenadas mal calculadas) y no una condición que el
 * código que la invoca deba estar obligado a manejar en cada llamada.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class InvalidShotException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo del error.
     *
     * @param message detalle del motivo por el cual las coordenadas son inválidas
     */
    public InvalidShotException(String message) {
        super(message);
    }
}
