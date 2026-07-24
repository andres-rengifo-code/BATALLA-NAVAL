package proyect_batalla_naval.model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Estrategia de disparo de la máquina que combina un modo aleatorio con un
 * "modo caza": cuando un disparo impacta un barco sin hundirlo, encola las
 * celdas vecinas (arriba, abajo, izquierda, derecha) y las intenta antes de
 * volver a disparar completamente al azar.
 * <p>
 * Implementación concreta del patrón <b>Strategy</b> definido por
 * {@link ShootingStrategy}. Usa una {@link Deque} como <b>cola</b> de
 * candidatos, cumpliendo también el requisito de estructura de datos tipo
 * "Colas" del proyecto.
 * <p>
 * Implementa {@link Serializable} (heredado de {@link ShootingStrategy})
 * para que una partida en curso, con la máquina a mitad de un "modo caza",
 * pueda guardarse y recuperarse sin perder el estado de la IA.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class HuntTargetStrategy implements ShootingStrategy {

    /** Identificador de versión para la serialización de la clase. */
    private static final long serialVersionUID = 1L;

    /** Generador de números aleatorios para el modo de disparo al azar. */
    private final Random random = new Random();

    /** Cola de celdas candidatas pendientes por probar (modo caza). */
    private final Deque<int[]> huntQueue = new ArrayDeque<>();

    @Override
    public int[] chooseShot(boolean[][] shotsTaken) {
        // Modo caza: intenta primero las celdas vecinas a un impacto previo
        while (!huntQueue.isEmpty()) {
            int[] candidate = huntQueue.poll();
            if (isValidCandidate(candidate[0], candidate[1], shotsTaken)) {
                return candidate;
            }
        }

        // Modo aleatorio: si no hay candidatos válidos pendientes
        int row, col;
        do {
            row = random.nextInt(Board.SIZE);
            col = random.nextInt(Board.SIZE);
        } while (shotsTaken[row][col]);

        return new int[]{row, col};
    }

    @Override
    public void onShotResult(int row, int col, ShotResult result) {
        if (result == ShotResult.HIT) {
            enqueueNeighbors(row, col);
        } else if (result == ShotResult.SUNK) {
            // El barco fue eliminado por completo: se descartan candidatos pendientes
            huntQueue.clear();
        }
    }

    /**
     * Encola las celdas vecinas de una celda impactada, para intentarlas
     * antes de volver al modo completamente aleatorio.
     *
     * @param row fila del impacto
     * @param col columna del impacto
     */
    private void enqueueNeighbors(int row, int col) {
        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : deltas) {
            int r = row + d[0];
            int c = col + d[1];
            if (r >= 0 && r < Board.SIZE && c >= 0 && c < Board.SIZE) {
                huntQueue.offer(new int[]{r, c});
            }
        }
    }

    /**
     * Verifica que una celda candidata esté dentro del tablero y no haya
     * sido disparada previamente.
     *
     * @param row        fila candidata
     * @param col        columna candidata
     * @param shotsTaken matriz de celdas ya disparadas
     * @return {@code true} si la celda es un candidato válido
     */
    private boolean isValidCandidate(int row, int col, boolean[][] shotsTaken) {
        return row >= 0 && row < Board.SIZE
                && col >= 0 && col < Board.SIZE
                && !shotsTaken[row][col];
    }
}
