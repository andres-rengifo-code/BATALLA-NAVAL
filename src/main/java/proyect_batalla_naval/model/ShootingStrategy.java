package proyect_batalla_naval.model;

import java.io.Serializable;

/**
 * Define el contrato para las distintas estrategias de disparo que puede
 * usar la máquina durante su turno.
 * <p>
 * Implementa el patrón de diseño de <b>comportamiento Strategy</b>: separa
 * el algoritmo de selección de casillas (qué celda elegir y cómo reaccionar
 * al resultado) de la clase {@link Game}, que solo se encarga de orquestar
 * el turno. Esto permite intercambiar la forma de jugar de la máquina
 * (por ejemplo, una IA puramente aleatoria contra una IA con modo "caza")
 * sin modificar {@code Game}.
 * <p>
 * Extiende {@link Serializable} porque {@link Game} implementa
 * {@code Serializable} para poder guardarse en disco (ver
 * {@code GameSaveManager}), y todo campo de una clase serializable debe
 * ser a su vez serializable — incluida la estrategia activa de la máquina.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public interface ShootingStrategy extends Serializable {

    /**
     * Elige la próxima celda donde la máquina va a disparar.
     *
     * @param shotsTaken matriz de 10×10 que indica qué celdas del tablero
     *                    del jugador ya fueron disparadas por la máquina
     * @return arreglo {@code [fila, columna]} de la celda elegida
     */
    int[] chooseShot(boolean[][] shotsTaken);

    /**
     * Notifica a la estrategia el resultado del último disparo, para que
     * pueda ajustar su comportamiento futuro (por ejemplo, encolar celdas
     * vecinas tras un impacto).
     *
     * @param row    fila del disparo realizado
     * @param col    columna del disparo realizado
     * @param result resultado obtenido en ese disparo
     */
    void onShotResult(int row, int col, ShotResult result);
}
