package proyect_batalla_naval.model;

import proyect_batalla_naval.exceptions.GameStateException;
import proyect_batalla_naval.exceptions.InvalidPlacementException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Cola (FIFO) de casillas candidatas para el "modo caza" de la IA: cuando
     * la máquina toca un barco sin hundirlo, se encolan las celdas vecinas
     * para intentarlas antes de volver a disparar completamente al azar.
     */
    private Deque<int[]> huntQueue;

    /**
     * Pila (LIFO) con el historial de disparos de toda la partida (tanto del
     * jugador como de la máquina). Se implementa con {@link ArrayDeque}
     * usada como pila mediante {@code push}/{@code peek}, de forma que el
     * disparo más reciente siempre queda primero.
     */
    private Deque<ShotRecord> moveHistory;

    /**
     * Tabla (mapa) con estadísticas de la partida: cantidad de disparos y
     * aciertos de cada bando, usada para calcular precisión (%).
     */
    private Map<String, Integer> stats;

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
        this.huntQueue = new ArrayDeque<>();
        this.moveHistory = new ArrayDeque<>();
        this.stats = new HashMap<>();
        stats.put("playerShots", 0);
        stats.put("playerHits", 0);
        stats.put("machineShots", 0);
        stats.put("machineHits", 0);

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
                    try {
                        machineBoard.placeShip(ship, row, col, horizontal);
                        placed = true;
                    } catch (InvalidPlacementException e) {
                        // No debería ocurrir porque ya validamos con canPlaceShip,
                        // pero se maneja de forma defensiva para no romper la partida.
                        System.err.println("Error al colocar barco de la máquina: " + e.getMessage());
                    }
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
        try {
            playerBoard.placeShip(ship, row, col, horizontal);
            return true;
        } catch (InvalidPlacementException e) {
            // No debería ocurrir ya que se validó previamente con canPlaceShip.
            return false;
        }
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
     * @return resultado del disparo
     * @throws GameStateException si el juego no está en curso o no es el turno del jugador
     */
    public ShotResult playerShoot(int row, int col) {
        if (state != GameState.PLAYING || !playerTurn) {
            throw new GameStateException("No se puede disparar: el juego no está en curso o no es el turno del jugador.");
        }
        ShotResult result = machineBoard.receiveShot(row, col);
        if (result == ShotResult.ALREADY_SHOT) return result;

        recordMove(row, col, result, true);
        updateStats(true, result);

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
     * Realiza un disparo de la máquina al tablero del jugador.
     * <p>
     * La máquina dispara de manera aleatoria (HU-4), pero además implementa
     * un sencillo "modo caza": cuando un disparo toca un barco sin hundirlo,
     * las celdas vecinas se encolan como candidatas prioritarias para el
     * siguiente disparo, antes de volver al modo completamente aleatorio.
     *
     * @return resultado del disparo
     * @throws GameStateException si el juego no está en curso o no es el turno de la máquina
     */
    public ShotResult machineShoot() {
        if (state != GameState.PLAYING || playerTurn) {
            throw new GameStateException("No se puede disparar: el juego no está en curso o no es el turno de la máquina.");
        }

        int row = -1, col = -1;
        boolean found = false;

        // Modo caza: intenta primero las celdas vecinas a un impacto previo
        while (!huntQueue.isEmpty() && !found) {
            int[] candidate = huntQueue.poll();
            int r = candidate[0], c = candidate[1];
            if (r >= 0 && r < Board.SIZE && c >= 0 && c < Board.SIZE && !machineShotsTaken[r][c]) {
                row = r;
                col = c;
                found = true;
            }
        }

        // Modo aleatorio: si no hay candidatos válidos pendientes
        if (!found) {
            do {
                row = random.nextInt(Board.SIZE);
                col = random.nextInt(Board.SIZE);
            } while (machineShotsTaken[row][col]);
        }

        machineShotsTaken[row][col] = true;
        lastMachineShotRow = row;
        lastMachineShotCol = col;
        ShotResult result = playerBoard.receiveShot(row, col);

        recordMove(row, col, result, false);
        updateStats(false, result);

        if (result == ShotResult.WATER) {
            playerTurn = true;
        } else if (result == ShotResult.HIT) {
            enqueueNeighbors(row, col);
            // la máquina vuelve a disparar
        } else if (result == ShotResult.SUNK) {
            // El barco fue eliminado por completo: se descartan candidatos pendientes
            huntQueue.clear();
            // la máquina vuelve a disparar
        }

        if (playerBoard.areAllShipsSunk()) {
            state = GameState.GAME_OVER;
            winner = "Máquina";
        }

        return result;
    }

    /**
     * Encola las celdas vecinas (arriba, abajo, izquierda, derecha) de una
     * celda impactada, para que la IA las intente antes de volver al modo
     * completamente aleatorio.
     *
     * @param row fila del impacto
     * @param col columna del impacto
     */
    private void enqueueNeighbors(int row, int col) {
        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : deltas) {
            int r = row + d[0];
            int c = col + d[1];
            if (r >= 0 && r < Board.SIZE && c >= 0 && c < Board.SIZE && !machineShotsTaken[r][c]) {
                huntQueue.offer(new int[]{r, c});
            }
        }
    }

    /**
     * Registra un disparo en el historial de movimientos de la partida (pila).
     *
     * @param row      fila del disparo
     * @param col      columna del disparo
     * @param result   resultado del disparo
     * @param byPlayer {@code true} si el disparo lo hizo el jugador humano
     */
    private void recordMove(int row, int col, ShotResult result, boolean byPlayer) {
        moveHistory.push(new ShotRecord(row, col, result, byPlayer));
    }

    /**
     * Actualiza la tabla de estadísticas de disparos/aciertos.
     *
     * @param byPlayer {@code true} si el disparo lo hizo el jugador humano
     * @param result   resultado del disparo
     */
    private void updateStats(boolean byPlayer, ShotResult result) {
        String shotsKey = byPlayer ? "playerShots" : "machineShots";
        String hitsKey = byPlayer ? "playerHits" : "machineHits";
        stats.merge(shotsKey, 1, Integer::sum);
        if (result == ShotResult.HIT || result == ShotResult.SUNK) {
            stats.merge(hitsKey, 1, Integer::sum);
        }
    }

    /**
     * Devuelve los últimos {@code count} movimientos de la partida, del más
     * reciente al más antiguo.
     *
     * @param count cantidad máxima de movimientos a devolver
     * @return lista con los últimos movimientos
     */
    public List<ShotRecord> getRecentMoves(int count) {
        List<ShotRecord> recent = new ArrayList<>();
        int i = 0;
        for (ShotRecord record : moveHistory) {
            if (i >= count) break;
            recent.add(record);
            i++;
        }
        return recent;
    }

    /**
     * Devuelve la tabla de estadísticas de la partida (disparos y aciertos
     * de cada bando).
     *
     * @return mapa con las claves {@code playerShots}, {@code playerHits},
     *         {@code machineShots} y {@code machineHits}
     */
    public Map<String, Integer> getStats() {
        return stats;
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
