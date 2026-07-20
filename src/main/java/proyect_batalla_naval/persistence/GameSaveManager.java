package proyect_batalla_naval.persistence;

import proyect_batalla_naval.model.Game;
import java.io.*;

/**
 * Gestiona el guardado y carga automática de partidas.
 * Guarda:
 * - Estado completo del juego mediante serialización.
 * - Nickname del jugador en un archivo plano.
 *
 * @author Juan Pablo Gómez
 */
public class GameSaveManager {

    /**
     * Archivo serializado del juego.
     */
    private static final String GAME_FILE = "save/game.dat";

    /**
     * Archivo plano con información del jugador.
     */
    private static final String PLAYER_FILE = "save/player.txt";

    /**
     * Guarda automáticamente la partida.
     *
     * @param game partida actual.
     * @throws IOException si ocurre un error al guardar.
     */
    public static void saveGame(Game game) throws IOException {

        File folder = new File("save");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (ObjectOutputStream out =
                     new ObjectOutputStream(
                             new FileOutputStream(GAME_FILE))) {

            out.writeObject(game);

        }

        savePlayerInfo(game);
    }

    /**
     * Guarda el nickname en un archivo plano.
     *
     * @param game partida actual.
     * @throws IOException si ocurre un error.
     */
    private static void savePlayerInfo(Game game) throws IOException {

        BufferedWriter writer = new BufferedWriter(
                new FileWriter(PLAYER_FILE));

        writer.write(game.getPlayerNickname());
        writer.newLine();
        writer.write(String.valueOf(game.getPlayerSunkCount()));

        writer.close();
    }

    /**
     * Carga una partida guardada.
     *
     * @return partida recuperada.
     * @throws IOException error de lectura.
     * @throws ClassNotFoundException error de deserialización.
     */
    public static Game loadGame()
            throws IOException, ClassNotFoundException {

        ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(GAME_FILE));

        Game game = (Game) in.readObject();

        in.close();

        return game;
    }

    /**
     * Indica si existe una partida guardada.
     *
     * @return true si existe.
     */
    public static boolean existsSave() {

        return new File(GAME_FILE).exists();
    }

    /**
     * Elimina la partida guardada.
     */
    public static void deleteSave() {

        File game = new File(GAME_FILE);

        if (game.exists()) {
            game.delete();
        }

        File player = new File(PLAYER_FILE);

        if (player.exists()) {
            player.delete();
        }

    }

    /**
     * Recupera el nickname almacenado.
     *
     * @return nickname o cadena vacía.
     */
    public static String loadNickname() {

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(PLAYER_FILE))) {

            return reader.readLine();

        } catch (Exception e) {

            return null;
        }
    }
}