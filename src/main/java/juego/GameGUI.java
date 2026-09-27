package juego;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Version GRAFICA (Swing) del juego, Humano (H) vs
 * IA (A).
 *
 * Estructura de la ventana (BorderLayout):
 *   - NORTE : etiqueta de estado (de quien es el turno,
 *             o el mensaje de fin de partida).
 *   - CENTRO: tablero de 8x8 JButton.
 *   - SUR   : boton "Volver a jugar".
 *
 * HILOS Y SwingWorker
 * --------------------
 * Minimax con profundidad 6 puede tardar una fraccion
 * de segundo perceptible. Si se calculara el movimiento
 * de la IA directamente dentro de un ActionListener, la
 * ventana se "congelaria" (no repintaria, no
 * respondería a clicks) mientras la IA piensa, porque
 * todo el codigo de los ActionListener corre en el
 * Event Dispatch Thread (EDT), el mismo hilo que dibuja
 * la interfaz.
 *
 * Por eso el calculo de MinimaxAI.getBestMove(...) se
 * hace dentro de un SwingWorker, en un hilo aparte:
 *   - doInBackground() corre en un hilo de fondo (aqui
 *     es seguro tardar).
 *   - done() vuelve a correr en el EDT, y es donde se
 *     aplica el resultado sobre la interfaz (que solo
 *     se debe tocar desde el EDT).
 *
 * Mientras la IA esta pensando, se deshabilita el
 * boton "Volver a jugar" para evitar una condicion de
 * carrera: si se reiniciara la partida a mitad de un
 * calculo, cuando el SwingWorker terminara aplicaria un
 * movimiento pensado para la partida VIEJA sobre el
 * estado NUEVO ya reiniciado, dejando el tablero en un
 * estado invalido.
 */
public class GameGUI extends JFrame {

    private static final int SIZE = GameState.SIZE;

    private static final Color COLOR_HUMANO = new Color(102, 187, 106);
    private static final Color COLOR_IA = new Color(239, 83, 80);
    private static final Color COLOR_USADA = new Color(66, 66, 66);
    private static final Color COLOR_DISPONIBLE = new Color(255, 241, 118);
    private static final Color COLOR_LIBRE = Color.WHITE;

    /** Un JButton por cada una de las 64 casillas. */
    private final JButton[][] buttons =
            new JButton[SIZE][SIZE];

    /** Muestra de quien es el turno o el resultado final. */
    private final JLabel statusLabel;

    /** Boton para reiniciar la partida en cualquier momento. */
    private final JButton restartButton;

    /** Estado actual del juego (inmutable; se reemplaza en cada jugada). */
    private GameState state;

    /** IA con profundidad de busqueda 6. */
    private final MinimaxAI ai;

    /** true una vez que la partida actual ya termino. */
    private boolean gameOver = false;

    public GameGUI() {

        super("Juego 8x8 - Humano (H) vs IA (A)");

        ai = new MinimaxAI(6);

        /*
         * Estado inicial estandar del juego
         * (patron Factory Method, ver GameState).
         */
        state = GameState.crearEstadoInicial();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Tu turno (H)", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.NORTH);

        add(crearTablero(), BorderLayout.CENTER);

        restartButton = new JButton("Volver a jugar");
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.addActionListener(e -> reiniciarJuego());

        JPanel panelInferior = new JPanel();
        panelInferior.add(restartButton);
        add(panelInferior, BorderLayout.SOUTH);

        refreshBoard();

        setSize(560, 660);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Crea el panel central con la grilla de 64
     * botones y les conecta su ActionListener.
     */
    private JPanel crearTablero() {

        JPanel board = new JPanel(new GridLayout(SIZE, SIZE));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {

                final int r = row;
                final int c = col;

                JButton button = new JButton();
                button.setFont(new Font("Arial", Font.BOLD, 18));
                button.setFocusPainted(false);
                button.setOpaque(true);
                button.setBorderPainted(false);
                button.addActionListener(e -> handleClick(r, c));

                buttons[row][col] = button;
                board.add(button);
            }
        }

        return board;
    }

    /**
     * Reinicia la partida desde cero, en cualquier
     * momento (la partida este en curso o ya haya
     * terminado). Usa el mismo Factory Method que la
     * version de consola para no duplicar la logica
     * de "como empieza una partida nueva".
     */
    private void reiniciarJuego() {

        state = GameState.crearEstadoInicial();
        gameOver = false;

        statusLabel.setText("Tu turno (H)");

        refreshBoard();
    }

    /**
     * Se ejecuta cuando el humano hace click en una
     * casilla del tablero.
     */
    private void handleClick(int row, int col) {

        if (gameOver) {
            return;
        }

        if (state.getCurrentPlayer() != 1) {
            return;
        }

        List<Position> moves =
                MoveGenerator.getLegalMoves(state);

        Position selected = new Position(row, col);

        if (!moves.contains(selected)) {

            statusLabel.setText(
                    "Movimiento invalido. Elige una casilla resaltada."
            );

            return;
        }

        state = state.makeMove(selected);

        refreshBoard();

        continueGame();
    }

    /**
     * Revisa si el juego termino o si le toca jugar a
     * la IA, y actua en consecuencia. Si es turno de la
     * IA, lanza el calculo en un SwingWorker (ver
     * javadoc de la clase).
     */
    private void continueGame() {

        List<Position> moves =
                MoveGenerator.getLegalMoves(state);

        if (moves.isEmpty()) {

            endGame();

            return;
        }

        if (state.getCurrentPlayer() == 2) {

            statusLabel.setText("La IA esta pensando...");

            setBoardEnabled(false);

            /*
             * Se deshabilita el boton de reinicio
             * mientras la IA calcula, para evitar la
             * condicion de carrera descrita en el
             * javadoc de la clase.
             */
            restartButton.setEnabled(false);

            SwingWorker<Position, Void> worker =
                    new SwingWorker<Position, Void>() {

                @Override
                protected Position doInBackground() {
                    return ai.getBestMove(state);
                }

                @Override
                protected void done() {

                    try {

                        Position bestMove = get();

                        if (bestMove == null) {
                            endGame();
                            return;
                        }

                        state = state.makeMove(bestMove);

                        refreshBoard();

                        continueGame();

                    } catch (Exception ex) {

                        ex.printStackTrace();

                        statusLabel.setText(
                                "Ocurrio un error en la IA."
                        );

                        restartButton.setEnabled(true);
                    }
                }
            };

            worker.execute();

        } else {

            statusLabel.setText("Tu turno (H)");
        }
    }

    /**
     * Muestra quien gano y bloquea el tablero (pero
     * deja habilitado "Volver a jugar").
     */
    private void endGame() {

        gameOver = true;

        setBoardEnabled(false);
        restartButton.setEnabled(true);

        String message;

        if (state.getCurrentPlayer() == 1) {
            message = "No tienes movimientos disponibles. ¡LA IA GANA!";
        } else {
            message = "La IA no tiene movimientos disponibles. ¡TU GANAS!";
        }

        statusLabel.setText(message);

        JOptionPane.showMessageDialog(this, message);
    }

    private void setBoardEnabled(boolean enabled) {

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                buttons[row][col].setEnabled(enabled);
            }
        }
    }

    /**
     * Repinta las 64 casillas segun el estado actual:
     * quien esta donde, que casillas estan usadas, y
     * (si es turno del humano) cuales son sus
     * movimientos validos. Tambien vuelve a habilitar
     * el boton de reinicio, que solo se deshabilita
     * transitoriamente mientras la IA piensa.
     */
    private void refreshBoard() {

        List<Position> moves =
                (state.getCurrentPlayer() == 1 && !gameOver)
                        ? MoveGenerator.getLegalMoves(state)
                        : Collections.<Position>emptyList();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {

                Position position = new Position(row, col);
                JButton button = buttons[row][col];

                if (position.equals(state.getHumanPosition())) {

                    button.setText("H");
                    button.setBackground(COLOR_HUMANO);

                } else if (position.equals(state.getAiPosition())) {

                    button.setText("A");
                    button.setBackground(COLOR_IA);

                } else if (state.isUsed(row, col)) {

                    button.setText("");
                    button.setBackground(COLOR_USADA);

                } else if (moves.contains(position)) {

                    button.setText("");
                    button.setBackground(COLOR_DISPONIBLE);

                } else {

                    button.setText("");
                    button.setBackground(COLOR_LIBRE);
                }
            }
        }

        setBoardEnabled(!gameOver && state.getCurrentPlayer() == 1);
        restartButton.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameGUI::new);
    }
}
