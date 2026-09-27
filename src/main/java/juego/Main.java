package juego;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicacion.
 *
 * Por defecto se abre la interfaz grafica (Swing),
 * dentro de SwingUtilities.invokeLater(...) porque
 * toda la creacion y manipulacion de componentes Swing
 * debe ocurrir en el Event Dispatch Thread (EDT).
 *
 * Si en cambio se prefiere la version de consola, se
 * puede reemplazar el contenido de main(...) por:
 *
 *     Game game = new Game();
 *     game.start();
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameGUI::new);
    }
}
