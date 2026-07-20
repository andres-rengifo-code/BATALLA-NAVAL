package proyect_batalla_naval.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un barco en el juego de Batalla Naval.
 * Cada barco tiene un nombre, tamaño, posición en el tablero,
 * orientación (horizontal o vertical) y un registro de impactos.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class Ship {

    /** Nombre del barco (ej: "Portaaviones", "Submarino 1"). */
    private String name;

    /** Cantidad de casillas que ocupa el barco. */
    private int size;

    /** Fila de la casilla superior-izquierda del barco. */
    private int row;

    /** Columna de la casilla superior-izquierda del barco. */
    private int col;

    /** {@code true} si el barco está orientado horizontalmente. */
    private boolean horizontal;

    /** Arreglo que indica cuáles partes del barco han sido impactadas. */
    private boolean[] hits;

    /** Indica si el barco ya fue colocado en el tablero. */
    private boolean placed;

    /**
     * Crea un nuevo barco con el nombre y tamaño indicados.
     * ademas de inicializar los impactos de un barco y su "Placed"
     *
     * @param name nombre del barco
     * @param size cantidad de casillas que ocupa
     */
    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.hits = new boolean[size];
        this.placed = false;
    }

    /**
     * Establece la posición del barco en el tablero.
     *
     * @param row        fila inicial
     * @param col        columna inicial
     * @param horizontal {@code true} para orientación horizontal
     */
    public void setPosition(int row, int col, boolean horizontal) {
        this.row = row;
        this.col = col;
        this.horizontal = horizontal;
        this.placed = true;
    }

    /**
     * Registra un impacto en la celda indicada.
     *
     * @param targetRow fila del impacto
     * @param targetCol columna del impacto
     */
    public void hit(int targetRow, int targetCol) {
        List<int[]> cells = getOccupiedCells();
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i)[0] == targetRow && cells.get(i)[1] == targetCol) {
                hits[i] = true;
                break;
            }
        }
    }

    /**
     * Verifica si el barco está completamente hundido.
     *
     * @return {@code true} si todas las partes han sido impactadas
     */
    public boolean isSunk() {
        for (boolean h : hits) {
            if (!h) return false;
        }
        return true;
    }

    /**
     * Devuelve la lista de celdas que ocupa este barco.
     *
     * @return lista de arreglos {@code [fila, columna]}
     */
    public List<int[]> getOccupiedCells() {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (horizontal) {
                cells.add(new int[]{row, col + i});
            } else {
                cells.add(new int[]{row + i, col});
            }
        }
        return cells;
    }

    /** @return nombre del barco */
    public String getName() { return name; }

    /** @return tamaño del barco */
    public int getSize() { return size; }

    /** @return fila inicial del barco */
    public int getRow() { return row; }

    /** @return columna inicial del barco */
    public int getCol() { return col; }

    /** @return {@code true} si es horizontal */
    public boolean isHorizontal() { return horizontal; }

    /** @return {@code true} si el barco ya fue colocado */
    public boolean isPlaced() { return placed; }
}
