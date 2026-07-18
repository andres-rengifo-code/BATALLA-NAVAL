package proyect_batalla_naval.model;

/**
 * Representa el resultado de un disparo en el juego de Batalla Naval.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public enum ShotResult {

    /** Disparo al agua: no hay barco en esa casilla. */
    WATER,

    /** Tocado: se impactó una parte de un barco que aún no está hundido. */
    HIT,

    /** Hundido: se impactó la última parte de un barco, hundiéndolo por completo. */
    SUNK,

    /** Casilla ya disparada: el jugador ya había disparado en esta casilla. */
    ALREADY_SHOT
}
