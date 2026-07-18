package proyect_batalla_naval.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un tablero de 10×10 del juego Batalla Naval.
 * Cada celda almacena un estado: vacío, barco, agua (fallo), tocado o hundido.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class Board {

    /** Tamaño del tablero (10×10). */
    public static final int SIZE = 10;

    /** Celda vacía. */
    public static final int EMPTY = 0;

    /** Celda con barco (sin impactar). */
    public static final int SHIP = 1;

    /** Celda donde se disparó y no había barco. */
    public static final int WATER = 2;

    /** Celda donde se impactó parte de un barco. */
    public static final int HIT = 3;

    /** Celda donde el barco fue hundido. */
    public static final int SUNK = 4;

    /** Matriz que representa el estado de cada celda. */
    private int[][] grid;

    /** Lista de barcos colocados en este tablero. */
    private List<Ship> ships;

    /**
     * Crea un nuevo tablero vacío de 10×10.
     * y una lista de barcos vacia
     */
    public Board() {
        grid = new int[SIZE][SIZE];
        ships = new ArrayList<>();
    }

    /**
     * Verifica si un barco puede ser colocado en la posición indicada.
     *
     * @param row        fila inicial
     * @param col        columna inicial
     * @param size       tamaño del barco
     * @param horizontal {@code true} para orientación horizontal
     * @return {@code true} si la colocación es válida
     */
    public boolean canPlaceShip(int row, int col, int size, boolean horizontal) {
        if (horizontal) {
            if (col + size > SIZE) return false;
            for (int i = 0; i < size; i++) {
                if (grid[row][col + i] != EMPTY) return false;
            }
        } else {
            if (row + size > SIZE) return false;
            for (int i = 0; i < size; i++) {
                if (grid[row + i][col] != EMPTY) return false;
            }
        }
        return true;
    }

    /**
     * Coloca un barco en la posición indicada. Se asume que ya se validó
     * con {canPlaceShip(int, int, int, boolean)}.
     *
     * @param ship       el barco a colocar
     * @param row        fila inicial
     * @param col        columna inicial
     * @param horizontal {@code true} para orientación horizontal
     */
    public void placeShip(Ship ship, int row, int col, boolean horizontal) {
        ship.setPosition(row, col, horizontal);
        if (horizontal) {
            for (int i = 0; i < ship.getSize(); i++) {
                grid[row][col + i] = SHIP;
            }
        } else {
            for (int i = 0; i < ship.getSize(); i++) {
                grid[row + i][col] = SHIP;
            }
        }
        ships.add(ship);
    }

    /**
     * Recibe un disparo en la celda indicada y devuelve el resultado.
     *
     * @param row fila del disparo
     * @param col columna del disparo
     * @return resultado del disparo
     */
    public ShotResult receiveShot(int row, int col) {
        if (grid[row][col] == WATER || grid[row][col] == HIT || grid[row][col] == SUNK) {
            return ShotResult.ALREADY_SHOT;
        }
        if (grid[row][col] == EMPTY) {
            grid[row][col] = WATER;
            return ShotResult.WATER;
        }
        // Es un barco
        Ship hitShip = findShipAt(row, col);
        if (hitShip != null) {
            hitShip.hit(row, col);
            if (hitShip.isSunk()) {
                markAsSunk(hitShip);
                return ShotResult.SUNK;
            }
            grid[row][col] = HIT;
            return ShotResult.HIT;
        }
        return ShotResult.WATER;
    }

    /**
     * Busca el barco que ocupa la celda indicada.
     *
     * @param row fila
     * @param col columna
     * @return el barco encontrado, o {@code null} si no hay barco
     */
    private Ship findShipAt(int row, int col) {
        for (Ship ship : ships) {
            for (int[] cell : ship.getOccupiedCells()) {
                if (cell[0] == row && cell[1] == col) {
                    return ship;
                }
            }
        }
        return null;
    }

    /**
     * Marca todas las celdas de un barco hundido como {@link #SUNK}.
     *
     * @param ship el barco hundido
     */
    private void markAsSunk(Ship ship) {
        for (int[] cell : ship.getOccupiedCells()) {
            grid[cell[0]][cell[1]] = SUNK;
        }
    }

    /**
     * Verifica si todos los barcos del tablero han sido hundidos.
     *
     * @return {@code true} si toda la flota fue hundida
     */
    public boolean areAllShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) return false;
        }
        return !ships.isEmpty();
    }

    /**
     * Obtiene el estado de una celda.
     *
     * @param row fila
     * @param col columna
     * @return valor del estado de la celda
     */
    public int getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Devuelve la lista de barcos colocados en este tablero.
     *
     * @return lista de barcos
     */
    public List<Ship> getShips() {
        return ships;
    }
}
