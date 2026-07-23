package proyect_batalla_naval.model;

import java.io.Serializable;

/**
 * Representa un disparo realizado durante la partida, ya sea por el
 * jugador humano o por la máquina. Se utiliza para construir el
 * historial de movimientos de la partida.
 *
 * @author Andres Felipe Rengifo Rodriguez
 * @author Juan Pablo Gomez
 * @author Álvaro Iván Ospina Capera
 */
public class ShotRecord implements Serializable {

    /** Fila donde se realizó el disparo. */
    private final int row;

    /** Columna donde se realizó el disparo. */
    private final int col;

    /** Resultado obtenido en el disparo. */
    private final ShotResult result;

    /** {@code true} si el disparo lo realizó el jugador humano, {@code false} si fue la máquina. */
    private final boolean byPlayer;

    /**Identificador de versión para la serialización de la clase.*/
    private static final long serialVersionUID = 1L;
    /**
     * Crea un nuevo registro de disparo.
     *
     * @param row      fila del disparo
     * @param col      columna del disparo
     * @param result   resultado del disparo
     * @param byPlayer {@code true} si el disparo fue del jugador humano
     */
    public ShotRecord(int row, int col, ShotResult result, boolean byPlayer) {
        this.row = row;
        this.col = col;
        this.result = result;
        this.byPlayer = byPlayer;
    }

    /** @return fila del disparo */
    public int getRow() { return row; }

    /** @return columna del disparo */
    public int getCol() { return col; }

    /** @return resultado del disparo */
    public ShotResult getResult() { return result; }

    /** @return {@code true} si el disparo fue realizado por el jugador humano */
    public boolean isByPlayer() { return byPlayer; }

    /**
     * Devuelve la coordenada del disparo en formato tipo "B7".
     *
     * @return coordenada en notación de letra-número
     */
    public String getCoordinate() {
        return "" + (char) ('A' + col) + (row + 1);
    }
}
