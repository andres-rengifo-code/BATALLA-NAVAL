package proyect_batalla_naval.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Clase principal del modelo que gestiona la lógica del juego Batalla Naval.
 * Contiene los tableros del jugador y de la máquina, la flota de barcos,
 * el estado del juego y la lógica de turnos.
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class Game {

    /**
     * Estados posibles del juego.
     */
    public enum GameState {
        /** El jugador está colocando sus barcos. */
        PLACING_SHIPS,
        /** El juego está en curso (fase de disparos). */
        PLAYING,
        /** El juego ha terminado. */
        GAME_OVER
    }

    /** Tablero del jugador humano. */
    private Board playerBoard;

    /** Tablero de la máquina. */
    private Board machineBoard;

    /** Lista de barcos que el jugador debe colocar. */
    private List<Ship> playerShipsToPlace;

    /** Estado actual del juego. */
    private GameState state;

    /** Nickname del jugador humano. */
    private String playerNickname;

    /** Generador de números aleatorios. */
    private Random random;

    /** Indica si es el turno del jugador humano. */
    private boolean playerTurn;

    /** Nombre del ganador. */
    private String winner;

    /** Registro de casillas donde la máquina ya disparó. */
    private boolean[][] machineShotsTaken;

    /** Fila del último disparo de la máquina. */
    private int lastMachineShotRow = -1;

    /** Columna del último disparo de la máquina. */
    private int lastMachineShotCol = -1;

    /**
     * Crea una nueva partida de Batalla Naval.
     *
     * @param playerNickname nickname del jugador humano
     */
    public Game(String playerNickname) {
        this.playerNickname = playerNickname;
        this.playerBoard = new Board();
        this.machineBoard = new Board();
        this.random = new Random();
        this.state = GameState.PLACING_SHIPS;
        this.playerTurn = true;
        this.machineShotsTaken = new boolean[Board.SIZE][Board.SIZE];

        // Crear la flota del jugador para colocar
        this.playerShipsToPlace = createFleet();

        // Colocar barcos de la máquina aleatoriamente
        placeMachineShipsRandomly();
    }

    /**
     * Crea la flota estándar del juego: 1 portaaviones (4),
     * 2 submarinos (3), 3 destructores (2) y 4 fragatas (1).
     *
     * @return lista con los 10 barcos de la flota
     */
    private List<Ship> createFleet() {
        List<Ship> fleet = new ArrayList<>();
        fleet.add(new Ship("Portaaviones", 4));
        fleet.add(new Ship("Submarino 1", 3));
        fleet.add(new Ship("Submarino 2", 3));
        fleet.add(new Ship("Destructor 1", 2));
        fleet.add(new Ship("Destructor 2", 2));
        fleet.add(new Ship("Destructor 3", 2));
        fleet.add(new Ship("Fragata 1", 1));
        fleet.add(new Ship("Fragata 2", 1));
        fleet.add(new Ship("Fragata 3", 1));
        fleet.add(new Ship("Fragata 4", 1));
        return fleet;
    }

    /**
     * Coloca los barcos de la máquina de forma aleatoria en su tablero,
     * respetando las reglas de no superposición y límites del tablero.
     */
    private void placeMachineShipsRandomly() {
        List<Ship> machineShips = createFleet();
        for (Ship ship : machineShips) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(Board.SIZE);
                int col = random.nextInt(Board.SIZE);
                boolean horizontal = random.nextBoolean();
                if (machineBoard.canPlaceShip(row, col, ship.getSize(), horizontal)) {
                    machineBoard.placeShip(ship, row, col, horizontal);
                    placed = true;
                }
            }
        }
    }

    /**
     * Coloca un barco del jugador en su tablero.
     *
     * @param shipIndex  índice del barco en la lista de barcos por colocar
     * @param row        fila donde colocar el barco
     * @param col        columna donde colocar el barco
     * @param horizontal {@code true} para orientación horizontal
     * @return {@code true} si el barco fue colocado exitosamente
     */
    public boolean placePlayerShip(int shipIndex, int row, int col, boolean horizontal) {
        if (state != GameState.PLACING_SHIPS || shipIndex < 0 || shipIndex >= playerShipsToPlace.size()) {
            return false;
        }
        Ship ship = playerShipsToPlace.get(shipIndex);
        if (ship.isPlaced()) return false;
        if (!playerBoard.canPlaceShip(row, col, ship.getSize(), horizontal)) {
            return false;
        }
        playerBoard.placeShip(ship, row, col, horizontal);
        return true;
    }

    /**
     * Verifica si todos los barcos del jugador ya fueron colocados.
     *
     * @return {@code true} si todos los barcos están colocados
     */
    public boolean allPlayerShipsPlaced() {
        for (Ship ship : playerShipsToPlace) {
            if (!ship.isPlaced()) return false;
        }
        return true;
    }

    /**
     * Inicia el juego cambiando al estado {@link GameState#PLAYING}.
     * Solo se puede llamar cuando todos los barcos del jugador están colocados.
     */
    public void startGame() {
        if (allPlayerShipsPlaced()) {
            state = GameState.PLAYING;
            playerTurn = true;
        }
    }

    /**
     * Realiza un disparo del jugador humano al tablero de la máquina.
     *
     * @param row fila del disparo
     * @param col columna del disparo
     * @return resultado del disparo, o {@code null} si no es válido
     */
    public ShotResult playerShoot(int row, int col) {
        if (state != GameState.PLAYING || !playerTurn) return null;
        ShotResult result = machineBoard.receiveShot(row, col);
        if (result == ShotResult.ALREADY_SHOT) return result;

        if (result == ShotResult.WATER) {
            playerTurn = false;
        }
        // HIT o SUNK: el jugador vuelve a disparar

        if (machineBoard.areAllShipsSunk()) {
            state = GameState.GAME_OVER;
            winner = playerNickname;
        }

        return result;
    }

    /**
     * Realiza un disparo aleatorio de la máquina al tablero del jugador.
     *
     * @return resultado del disparo, o {@code null} si no es válido
     */
    public ShotResult machineShoot() {
        if (state != GameState.PLAYING || playerTurn) return null;

        int row, col;
        do {
            row = random.nextInt(Board.SIZE);
            col = random.nextInt(Board.SIZE);
        } while (machineShotsTaken[row][col]);

        machineShotsTaken[row][col] = true;
        lastMachineShotRow = row;
        lastMachineShotCol = col;
        ShotResult result = playerBoard.receiveShot(row, col);

        if (result == ShotResult.WATER) {
            playerTurn = true;
        }
        // HIT o SUNK: la máquina vuelve a disparar

        if (playerBoard.areAllShipsSunk()) {
            state = GameState.GAME_OVER;
            winner = "Máquina";
        }

        return result;
    }

    /** @return tablero del jugador humano */
    public Board getPlayerBoard() { return playerBoard; }

    /** @return tablero de la máquina */
    public Board getMachineBoard() { return machineBoard; }

    /** @return lista de barcos del jugador por colocar */
    public List<Ship> getPlayerShipsToPlace() { return playerShipsToPlace; }

    /** @return estado actual del juego */
    public GameState getState() { return state; }

    /** @return nickname del jugador humano */
    public String getPlayerNickname() { return playerNickname; }

    /** @return {@code true} si es el turno del jugador humano */
    public boolean isPlayerTurn() { return playerTurn; }

    /** @return nombre del ganador, o {@code null} si el juego no ha terminado */
    public String getWinner() { return winner; }

    /** @return fila del último disparo de la máquina */
    public int getLastMachineShotRow() { return lastMachineShotRow; }

    /** @return columna del último disparo de la máquina */
    public int getLastMachineShotCol() { return lastMachineShotCol; }

    /**
     * Cuenta los barcos de la máquina que el jugador ha hundido.
     *
     * @return cantidad de barcos hundidos por el jugador
     */
    public int getPlayerSunkCount() {
        int count = 0;
        for (Ship ship : machineBoard.getShips()) {
            if (ship.isSunk()) count++;
        }
        return count;
    }

    /**
     * Cuenta los barcos del jugador que la máquina ha hundido.
     *
     * @return cantidad de barcos hundidos por la máquina
     */
    public int getMachineSunkCount() {
        int count = 0;
        for (Ship ship : playerBoard.getShips()) {
            if (ship.isSunk()) count++;
        }
        return count;
    }
}
