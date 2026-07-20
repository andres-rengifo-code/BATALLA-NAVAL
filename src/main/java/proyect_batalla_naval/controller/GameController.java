package proyect_batalla_naval.controller;

import proyect_batalla_naval.model.*;
import proyect_batalla_naval.exceptions.GameStateException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import proyect_batalla_naval.model.Board;
import proyect_batalla_naval.model.Game;
import proyect_batalla_naval.model.Ship;
import proyect_batalla_naval.model.ShotResult;
import proyect_batalla_naval.persistence.GameSaveManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controlador principal del juego Batalla Naval.
 * Gestiona la interacción del usuario con los tableros,
 * la colocación de barcos (HU-1), la realización de disparos (HU-2),
 * la visualización de verificación del tablero del oponente (HU-3)
 * y la IA de la máquina (HU-4).
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class GameController {

    /** Tamaño en píxeles de cada celda del tablero. */
    private static final int CELL_SIZE = 38;

    // ---- Elementos FXML ----

    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label playerBoardLabel;
    @FXML private Label enemyBoardLabel;
    @FXML private GridPane playerGrid;
    @FXML private GridPane enemyGrid;
    @FXML private VBox shipPanel;
    @FXML private VBox shipListBox;
    @FXML private Label orientationLabel;
    @FXML private Button rotateButton;
    @FXML private Button startGameButton;
    @FXML private Label playerSunkLabel;
    @FXML private Label machineSunkLabel;
    @FXML private Label infoLabel;

    /** Botón de verificación docente (HU-3): solo disponible antes de iniciar la partida. */
    @FXML private Button revealButton;

    /** Etiqueta de precisión de disparos del jugador. */
    @FXML private Label accuracyLabel;

    /** Panel con el historial reciente de disparos (pila de movimientos). */
    @FXML private VBox historyBox;

    // ---- Estado interno ----

    /** Instancia del modelo del juego. */
    private Game game;

    /** Celdas visuales del tablero del jugador. */
    private StackPane[][] playerCells = new StackPane[10][10];

    /** Celdas visuales del tablero enemigo. */
    private StackPane[][] enemyCells = new StackPane[10][10];

    /** Índice del barco seleccionado para colocar. */
    private int selectedShipIndex = -1;

    /** Orientación actual del barco a colocar. */
    private boolean horizontal = true;

    /**
     * HU-3: indica si el tablero de la máquina se está mostrando en modo
     * verificación (solo disponible durante la colocación de barcos).
     */
    private boolean revealingEnemyBoard = false;


    @FXML
    public void initialize(){
        initGame(Session.nickname);
    }

      /**
     * Inicializa el juego con el nickname del jugador.
     * Crea el modelo, construye los tableros visuales y muestra la lista de barcos.
     *
     * @param nickname nombre del jugador
     */
    public void initGame(String nickname) {
        if (GameSaveManager.existsSave()) {

            try {
                game = GameSaveManager.loadGame();

                // Si la partida cargada ya terminó, eliminar el guardado
                // e iniciar una nueva partida.
                if (game.getState() == Game.GameState.GAME_OVER) {
                    GameSaveManager.deleteSave();
                    game = new Game(nickname);
                }

            } catch (Exception e) {
                game = new Game(nickname);
            }

        } else {

            game = new Game(nickname);

        }
        playerBoardLabel.setText("Tablero de " + nickname);
        buildGrid(playerGrid, playerCells, true);
        buildGrid(enemyGrid, enemyCells, false);
        populateShipList();
        updateStatus();
        updateSunkCounts();
        updateHistoryPanel();

        if (revealButton != null) {
            revealButton.setVisible(true);
            revealButton.setManaged(true);
            revealButton.setText("🔍 Ver tablero del oponente (verificación)");
        }
    }

    // =========================================================================
    //  CONSTRUCCIÓN DE LOS TABLEROS
    // =========================================================================

    /**
     * Construye visualmente un tablero de 10×10 dentro de un GridPane.
     *
     * @param grid          el GridPane donde se agregan las celdas
     * @param cells         matriz donde se almacenan las referencias a las celdas
     * @param isPlayerBoard {@code true} si es el tablero del jugador
     */
    private void buildGrid(GridPane grid, StackPane[][] cells, boolean isPlayerBoard) {
        grid.getChildren().clear();

        // Encabezados de columna (A-J)
        for (int col = 0; col < 10; col++) {
            Label label = new Label(String.valueOf((char) ('A' + col)));
            label.setStyle("-fx-text-fill: #8888aa; -fx-font-size: 12; -fx-font-weight: bold;");
            label.setPrefSize(CELL_SIZE, 20);
            label.setAlignment(Pos.CENTER);
            grid.add(label, col + 1, 0);
        }

        // Encabezados de fila (1-10)
        for (int row = 0; row < 10; row++) {
            Label label = new Label(String.valueOf(row + 1));
            label.setStyle("-fx-text-fill: #8888aa; -fx-font-size: 12; -fx-font-weight: bold;");
            label.setPrefSize(25, CELL_SIZE);
            label.setAlignment(Pos.CENTER);
            grid.add(label, 0, row + 1);
        }

        // Crear celdas
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                StackPane cell = createCell(row, col, isPlayerBoard);
                cells[row][col] = cell;
                grid.add(cell, col + 1, row + 1);
            }
        }
    }

    /**
     * Crea una celda visual individual del tablero.
     *
     * @param row           fila de la celda
     * @param col           columna de la celda
     * @param isPlayerBoard {@code true} si pertenece al tablero del jugador
     * @return un StackPane con el rectángulo de la celda
     */
    private StackPane createCell(int row, int col, boolean isPlayerBoard) {
        StackPane cell = new StackPane();
        Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
        rect.setFill(Color.web("#2a4a7f"));
        rect.setStroke(Color.web("#1a1a2e"));
        rect.setStrokeWidth(1);
        rect.setArcWidth(3);
        rect.setArcHeight(3);
        cell.getChildren().add(rect);

        if (isPlayerBoard) {
            // Eventos para colocación de barcos
            cell.setOnMouseEntered(e -> onPlayerCellHover(row, col, true));
            cell.setOnMouseExited(e -> onPlayerCellHover(row, col, false));
            cell.setOnMouseClicked(e -> onPlayerCellClicked(row, col));
        } else {
            // Eventos para disparos
            cell.setOnMouseClicked(e -> onEnemyCellClicked(row, col));
            cell.setOnMouseEntered(e -> {
                if (game != null && game.getState() == Game.GameState.PLAYING && game.isPlayerTurn()) {
                    Rectangle r = (Rectangle) cell.getChildren().get(0);
                    int cellState = game.getMachineBoard().getCell(row, col);
                    if (cellState == Board.EMPTY || cellState == Board.SHIP) {
                        r.setFill(Color.web("#4a6a9f"));
                    }
                }
            });
            cell.setOnMouseExited(e -> {
                if (game != null) {
                    refreshEnemyCell(row, col);
                }
            });
        }

        return cell;
    }

    // =========================================================================
    //  HU-1: COLOCACIÓN DE BARCOS
    // =========================================================================

    /**
     * Maneja el hover sobre una celda del tablero del jugador.
     * Muestra una vista previa de la colocación del barco seleccionado.
     *
     * @param row      fila de la celda
     * @param col      columna de la celda
     * @param entering {@code true} si el mouse está entrando, {@code false} si sale
     */
    private void onPlayerCellHover(int row, int col, boolean entering) {
        if (game == null || game.getState() != Game.GameState.PLACING_SHIPS || selectedShipIndex < 0) {
            return;
        }

        Ship ship = game.getPlayerShipsToPlace().get(selectedShipIndex);
        if (ship.isPlaced()) return;

        int size = ship.getSize();
        boolean canPlace = game.getPlayerBoard().canPlaceShip(row, col, size, horizontal);

        for (int i = 0; i < size; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (r >= 10 || c >= 10) break;

            Rectangle rect = (Rectangle) playerCells[r][c].getChildren().get(0);
            if (entering) {
                rect.setFill(canPlace ? Color.web("#2d6a4f") : Color.web("#8b0000"));
            } else {
                refreshPlayerCell(r, c);
            }
        }
    }

    /**
     * Maneja el clic sobre una celda del tablero del jugador para colocar un barco.
     *
     * @param row fila de la celda
     * @param col columna de la celda
     */
    private void onPlayerCellClicked(int row, int col) {
        if (game == null || game.getState() != Game.GameState.PLACING_SHIPS || selectedShipIndex < 0) {
            return;
        }

        Ship ship = game.getPlayerShipsToPlace().get(selectedShipIndex);
        if (ship.isPlaced()) return;

        if (game.placePlayerShip(selectedShipIndex, row, col, horizontal)) {
            // Actualizar tablero visual
            refreshPlayerBoard();

            // Auto-seleccionar el siguiente barco no colocado
            selectedShipIndex = -1;
            List<Ship> ships = game.getPlayerShipsToPlace();
            for (int i = 0; i < ships.size(); i++) {
                if (!ships.get(i).isPlaced()) {
                    selectedShipIndex = i;
                    break;
                }
            }
            updateShipList();

            // Verificar si todos los barcos están colocados
            if (game.allPlayerShipsPlaced()) {
                startGameButton.setDisable(false);
                statusLabel.setText("¡Todos los barcos colocados! Presiona 'Iniciar Juego'");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #2d6a4f; -fx-font-weight: bold;");
            }

            updateStatus();
        } else {
            infoLabel.setText("⚠ No se puede colocar el barco ahí. Intenta otra posición.");
        }
    }

    /**
     * Maneja el clic en el botón "Rotar" para cambiar la orientación del barco.
     */
    @FXML
    protected void onRotateClicked() {
        horizontal = !horizontal;
        orientationLabel.setText("Orientación: " + (horizontal ? "Horizontal" : "Vertical"));
    }

    /**
     * Maneja el clic en el botón "Iniciar Juego".
     * Cambia el estado del juego a PLAYING y desactiva los controles de colocación.
     * A partir de este punto, la opción de verificación del tablero enemigo (HU-3)
     * deja de estar disponible.
     */
    @FXML
    protected void onStartGameClicked() {
        if (game.allPlayerShipsPlaced()) {
            game.startGame();
            startGameButton.setDisable(true);
            startGameButton.setText("Juego en curso");
            rotateButton.setDisable(true);
            statusLabel.setText("¡Tu turno! Dispara en el tablero enemigo");
            statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #6ec6ff; -fx-font-weight: bold;");
            infoLabel.setText("Haz clic en una celda del tablero enemigo para disparar");

            // Desactivar botones de barcos
            for (Node node : shipListBox.getChildren()) {
                node.setDisable(true);
            }

            // HU-3: la verificación del tablero del oponente solo aplica antes de jugar
            revealingEnemyBoard = false;
            if (revealButton != null) {
                revealButton.setVisible(false);
                revealButton.setManaged(false);
            }
            refreshEnemyBoard();
        }
    }

    // =========================================================================
    //  HU-3: VISUALIZACIÓN DEL TABLERO DE POSICIÓN DEL OPONENTE (VERIFICACIÓN)
    // =========================================================================

    /**
     * Maneja el clic en el botón de verificación docente. Alterna entre
     * mostrar y ocultar la flota de la máquina en el tablero enemigo.
     * Solo tiene efecto mientras el juego está en la fase de colocación de
     * barcos ({@link Game.GameState#PLACING_SHIPS}); una vez iniciada la
     * partida el botón se oculta y esta acción no tiene efecto alguno.
     */
    @FXML
    protected void onToggleRevealClicked() {
        if (game == null || game.getState() != Game.GameState.PLACING_SHIPS) {
            return;
        }
        revealingEnemyBoard = !revealingEnemyBoard;
        revealButton.setText(revealingEnemyBoard
                ? "🙈 Ocultar tablero (verificación)"
                : "🔍 Ver tablero del oponente (verificación)");
        refreshEnemyBoard();
    }

    // =========================================================================
    //  HU-2 y HU-4: DISPAROS DEL JUGADOR E IA DE LA MÁQUINA
    // =========================================================================

    /**
     * Maneja el clic en una celda del tablero enemigo para disparar.
     *
     * @param row fila del disparo
     * @param col columna del disparo
     */
    private void onEnemyCellClicked(int row, int col) {
        if (game == null) return;

        ShotResult result;
        try {
            result = game.playerShoot(row, col);
        } catch (GameStateException e) {
            // No es el turno del jugador o el juego no está en curso: se ignora el clic.
            return;
        }

        if (result == ShotResult.ALREADY_SHOT) {
            infoLabel.setText("⚠ Ya disparaste en esa casilla. Elige otra.");
            return;
        }

        try {
            GameSaveManager.saveGame(game);
        } catch (IOException e) {
            e.printStackTrace();
        }

        refreshEnemyCell(row, col);
        updateSunkCounts();
        updateHistoryPanel();

        // Verificar fin del juego primero
        if (game.getState() == Game.GameState.GAME_OVER) {
            // Refrescar por si se hundió el último barco
            refreshEnemyBoard();
            showGameOver();
            return;
        }

        switch (result) {
            case WATER:
                String coordW = "" + (char) ('A' + col) + (row + 1);
                infoLabel.setText("Disparo en " + coordW + ": ¡Agua! Turno de la máquina...");
                statusLabel.setText("Turno de la máquina");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #ff6e6e;");
                // Turno de la máquina con un breve retardo
                PauseTransition pause = new PauseTransition(Duration.millis(800));
                pause.setOnFinished(e -> doMachineTurn());
                pause.play();
                break;
            case HIT:
                String coordH = "" + (char) ('A' + col) + (row + 1);
                infoLabel.setText("Disparo en " + coordH + ": ¡Tocado! Dispara de nuevo.");
                statusLabel.setText("¡Tocado! Tu turno de nuevo");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #ff8c00; -fx-font-weight: bold;");
                break;
            case SUNK:
                String coordS = "" + (char) ('A' + col) + (row + 1);
                infoLabel.setText("Disparo en " + coordS + ": ¡¡HUNDIDO!! Dispara de nuevo.");
                statusLabel.setText("¡¡HUNDIDO!! Tu turno de nuevo");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #dc143c; -fx-font-weight: bold;");
                refreshEnemyBoard();
                break;
            default:
                break;
        }
    }

    /**
     * Ejecuta el turno de la máquina (HU-4): dispara de forma autónoma y
     * procesa el resultado. Si la máquina acierta (tocado o hundido), vuelve
     * a disparar con un retardo.
     */
    private void doMachineTurn() {
        ShotResult result;
        try {
            result = game.machineShoot();
        } catch (GameStateException e) {
            return;
        }

        try {
            GameSaveManager.saveGame(game);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        refreshPlayerBoard();
        updateSunkCounts();
        updateHistoryPanel();

        int mr = game.getLastMachineShotRow();
        int mc = game.getLastMachineShotCol();
        String coord = "" + (char) ('A' + mc) + (mr + 1);

        if (game.getState() == Game.GameState.GAME_OVER) {
            showGameOver();
            return;
        }

        switch (result) {
            case WATER:
                infoLabel.setText("Máquina disparó en " + coord + ": Agua. ¡Tu turno!");
                statusLabel.setText("Tu turno");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #6ec6ff; -fx-font-weight: bold;");
                break;
            case HIT:
                infoLabel.setText("Máquina disparó en " + coord + ": ¡Tocó uno de tus barcos!");
                statusLabel.setText("La máquina dispara de nuevo...");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #ff6e6e;");
                PauseTransition pause = new PauseTransition(Duration.millis(800));
                pause.setOnFinished(e -> doMachineTurn());
                pause.play();
                break;
            case SUNK:
                infoLabel.setText("Máquina disparó en " + coord + ": ¡¡Hundió uno de tus barcos!!");
                statusLabel.setText("La máquina dispara de nuevo...");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #ff6e6e;");
                PauseTransition pause2 = new PauseTransition(Duration.millis(800));
                pause2.setOnFinished(e -> doMachineTurn());
                pause2.play();
                break;
            default:
                break;
        }
    }

    /**
     * Muestra un diálogo de fin de juego con el resultado.
     */
    private void showGameOver() {
        String winner = game.getWinner();
        statusLabel.setText("¡¡JUEGO TERMINADO!!");
        statusLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #ffd700; -fx-font-weight: bold;");
        infoLabel.setText("Ganador: " + winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Juego Terminado!");
        alert.setHeaderText("Ganador: " + winner);
        alert.setContentText(
                "Barcos hundidos por ti: " + game.getPlayerSunkCount() + "/10\n"
                + "Barcos hundidos por la máquina: " + game.getMachineSunkCount() + "/10");
        alert.showAndWait();
    }

    // =========================================================================
    //  ACTUALIZACIÓN VISUAL DE LOS TABLEROS
    // =========================================================================

    /**
     * actualiza visualmente todas las celdas del tablero del jugador.
     */
    private void refreshPlayerBoard() {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                refreshPlayerCell(r, c);
            }
        }
    }

    /**
     * actualiza visualmente una celda individual del tablero del jugador.
     *
     * @param row fila de la celda
     * @param col columna de la celda
     */
    private void refreshPlayerCell(int row, int col) {
        StackPane cell = playerCells[row][col];
        // Conservar solo el rectángulo base
        while (cell.getChildren().size() > 1) {
            cell.getChildren().remove(cell.getChildren().size() - 1);
        }
        Rectangle rect = (Rectangle) cell.getChildren().get(0);

        int state = game.getPlayerBoard().getCell(row, col);
        switch (state) {
            case Board.EMPTY:
                rect.setFill(Color.web("#2a4a7f"));
                break;
            case Board.SHIP:
                rect.setFill(Color.web("#607d8b"));
                break;
            case Board.WATER:
                rect.setFill(Color.web("#2a4a7f"));
                Text xText = new Text("✕");
                xText.setFill(Color.web("#ffffff80"));
                xText.setFont(Font.font(14));
                cell.getChildren().add(xText);
                break;
            case Board.HIT:
                rect.setFill(Color.web("#ff8c00"));
                Text hitText = new Text("🔥");
                hitText.setFont(Font.font(16));
                cell.getChildren().add(hitText);
                break;
            case Board.SUNK:
                rect.setFill(Color.web("#dc143c"));
                Text sunkText = new Text("💀");
                sunkText.setFont(Font.font(16));
                cell.getChildren().add(sunkText);
                break;
        }
    }

    /**
     * actualiza visualmente todas las celdas del tablero enemigo.
     */
    private void refreshEnemyBoard() {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                refreshEnemyCell(r, c);
            }
        }
    }

    /**
     * actualiza visualmente una celda individual del tablero enemigo.
     * No revela barcos no impactados del enemigo, salvo que el modo de
     * verificación (HU-3) esté activo y el juego aún esté en la fase de
     * colocación de barcos.
     *
     * @param row fila de la celda
     * @param col columna de la celda
     */
    private void refreshEnemyCell(int row, int col) {
        StackPane cell = enemyCells[row][col];
        while (cell.getChildren().size() > 1) {
            cell.getChildren().remove(cell.getChildren().size() - 1);
        }
        Rectangle rect = (Rectangle) cell.getChildren().get(0);

        boolean verifying = revealingEnemyBoard && game.getState() == Game.GameState.PLACING_SHIPS;

        int state = game.getMachineBoard().getCell(row, col);
        switch (state) {
            case Board.EMPTY:
                rect.setFill(Color.web("#2a4a7f"));
                break;
            case Board.SHIP:
                // Solo se revela con fines de verificación docente, antes de iniciar la partida
                rect.setFill(verifying ? Color.web("#607d8b") : Color.web("#2a4a7f"));
                break;
            case Board.WATER:
                rect.setFill(Color.web("#2a4a7f"));
                Text xText = new Text("✕");
                xText.setFill(Color.web("#ffffff80"));
                xText.setFont(Font.font(14));
                cell.getChildren().add(xText);
                break;
            case Board.HIT:
                rect.setFill(Color.web("#ff8c00"));
                Text hitText = new Text("🔥");
                hitText.setFont(Font.font(16));
                cell.getChildren().add(hitText);
                break;
            case Board.SUNK:
                rect.setFill(Color.web("#dc143c"));
                Text sunkText = new Text("💀");
                sunkText.setFont(Font.font(16));
                cell.getChildren().add(sunkText);
                break;
        }
    }

    // =========================================================================
    //  PANEL DE SELECCIÓN DE BARCOS
    // =========================================================================

    /**
     * Inicializa la lista de barcos en el panel lateral.
     */
    private void populateShipList() {
        updateShipList();
    }

    /**
     * Actualiza visualmente la lista de barcos en el panel lateral.
     * Muestra el estado de cada barco (por colocar, seleccionado, colocado).
     */
    private void updateShipList() {
        shipListBox.getChildren().clear();
        List<Ship> ships = game.getPlayerShipsToPlace();

        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            Button btn = new Button(ship.getName() + " (" + ship.getSize() + ")");
            btn.setMaxWidth(Double.MAX_VALUE);

            final int index = i;
            if (ship.isPlaced()) {
                btn.setStyle("-fx-background-color: #2d6a4f; -fx-text-fill: #aaa; -fx-padding: 6;");
                btn.setDisable(true);
                btn.setText("✓ " + ship.getName());
            } else if (index == selectedShipIndex) {
                btn.setStyle("-fx-background-color: #e6a817; -fx-text-fill: black; -fx-padding: 6; "
                        + "-fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    selectedShipIndex = -1;
                    updateShipList();
                    updateStatus();
                });
            } else {
                btn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-padding: 6; "
                        + "-fx-cursor: hand;");
                btn.setOnAction(e -> {
                    selectedShipIndex = index;
                    updateShipList();
                    updateStatus();
                });
            }

            shipListBox.getChildren().add(btn);
        }
    }


    /**
     * Actualiza los contadores de barcos hundidos y la precisión de disparo
     * del jugador (calculada a partir de la tabla de estadísticas del modelo).
     */
    private void updateSunkCounts() {
        playerSunkLabel.setText("Tus hundidos: " + game.getPlayerSunkCount() + "/10");
        machineSunkLabel.setText("Enemigo hundidos: " + game.getMachineSunkCount() + "/10");

        if (accuracyLabel != null) {
            Map<String, Integer> stats = game.getStats();
            int shots = stats.getOrDefault("playerShots", 0);
            int hits = stats.getOrDefault("playerHits", 0);
            int accuracy = shots == 0 ? 0 : (hits * 100 / shots);
            accuracyLabel.setText("Precisión: " + accuracy + "%");
        }
    }

    /**
     * Actualiza el panel de historial reciente de disparos, obtenido desde
     * la pila de movimientos del modelo ({@code Game.getRecentMoves}).
     */
    private void updateHistoryPanel() {
        if (historyBox == null) return;
        historyBox.getChildren().clear();

        List<ShotRecord> recent = game.getRecentMoves(6);
        for (ShotRecord record : recent) {
            String who = record.isByPlayer() ? "Tú" : "Máquina";
            String resultText;
            switch (record.getResult()) {
                case WATER: resultText = "Agua"; break;
                case HIT: resultText = "Tocado"; break;
                case SUNK: resultText = "Hundido"; break;
                default: resultText = "Repetido"; break;
            }
            Label entry = new Label(who + " → " + record.getCoordinate() + ": " + resultText);
            entry.setStyle("-fx-text-fill: #c0c0c0; -fx-font-size: 11;");
            historyBox.getChildren().add(entry);
        }
    }

    /**
     * Actualiza los mensajes de estado según la fase actual del juego.
     */
    private void updateStatus() {
        if (game.getState() == Game.GameState.PLACING_SHIPS) {
            int placed = 0;
            for (Ship s : game.getPlayerShipsToPlace()) {
                if (s.isPlaced()) placed++;
            }
            if (!game.allPlayerShipsPlaced()) {
                statusLabel.setText("Coloca tus barcos (" + placed + "/10)");
                statusLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #8888aa;");
            }
            if (selectedShipIndex >= 0) {
                Ship selected = game.getPlayerShipsToPlace().get(selectedShipIndex);
                infoLabel.setText("Seleccionado: " + selected.getName()
                        + " (tamaño " + selected.getSize() + ") — "
                        + (horizontal ? "Horizontal" : "Vertical"));
            } else {
                infoLabel.setText("Selecciona un barco de la lista");
            }
        }
    }
}
