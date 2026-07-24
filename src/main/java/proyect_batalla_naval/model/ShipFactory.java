package proyect_batalla_naval.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Fábrica encargada de construir la flota estándar de barcos del juego
 * Batalla Naval: 1 portaaviones (4), 2 submarinos (3), 3 destructores (2)
 * y 4 fragatas (1).
 * <p>
 * Implementa el patrón de diseño <b>creacional Factory Method</b>: centraliza
 * la creación de los objetos {@link Ship} en un único punto, evitando que la
 * lógica de construcción de la flota esté duplicada (antes se repetía tanto
 * para el jugador como para la máquina dentro de {@link Game}).
 *
 * @author Andres Felipe Rengifo Rodriguez
 */
public class ShipFactory {

    /** Constructor privado: esta clase solo expone métodos estáticos de fábrica. */
    private ShipFactory() {
    }

    /**
     * Crea una nueva flota estándar de 10 barcos, lista para ser colocada
     * en un {@link Board}. Cada llamada devuelve instancias nuevas de
     * {@link Ship}, para que el jugador y la máquina tengan sus propios
     * objetos independientes.
     *
     * @return lista con los 10 barcos de la flota estándar
     */
    public static List<Ship> createStandardFleet() {
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
}
