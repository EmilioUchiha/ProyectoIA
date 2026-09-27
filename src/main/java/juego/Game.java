package juego;

import java.util.List;
import java.util.Scanner;

/**
 * Version de CONSOLA del juego (Humano vs IA).
 *
 * Controla el flujo general de una partida: imprime el
 * tablero, pide movimientos al humano, le pide a la IA
 * (MinimaxAI) que calcule el suyo, y detecta cuando un
 * jugador se queda sin movimientos (pierde). Al terminar
 * una partida, pregunta si se quiere jugar de nuevo.
 */
public class Game {

    private Scanner scanner;

    private GameState state;

    private MinimaxAI ai;

    public Game() {

        scanner = new Scanner(System.in);

        /*
         * Profundidad 6.
         */
        ai = new MinimaxAI(6);

        /*
         * Estado inicial estandar del juego
         * (patron Factory Method, ver GameState).
         */
        state = GameState.crearEstadoInicial();
    }

    /**
     * Punto de entrada del modo consola. Juega
     * partidas en un ciclo: al terminar cada una,
     * pregunta si se quiere jugar de nuevo.
     */
    public void start() {

        boolean jugarDeNuevo = true;

        while (jugarDeNuevo) {

            imprimirEncabezado();

            /*
             * Reiniciamos el estado por si esta
             * es una nueva partida (no la primera).
             */
            state = GameState.crearEstadoInicial();

            jugarPartida();

            jugarDeNuevo = preguntarJugarDeNuevo();
        }

        scanner.close();
    }

    /**
     * Juega una partida completa, turno por turno,
     * hasta que alguien gana.
     */
    private void jugarPartida() {

        while (true) {

            printBoard();

            /*
             * Obtenemos movimientos disponibles para
             * el jugador que tiene el turno.
             */
            List<Position> moves =
                    MoveGenerator.getLegalMoves(state);

            /*
             * Si no hay movimientos, pierde el
             * jugador actual.
             */
            if (moves.isEmpty()) {

                finishGame();

                return;
            }

            /*
             * Turno del humano.
             */
            if (state.getCurrentPlayer() == 1) {

                boolean movimientoValido = humanTurn();

                if (!movimientoValido) {

                    System.out.println();
                    System.out.println("LA IA GANA.");

                    return;
                }
            }

            /*
             * Turno de la IA.
             */
            else {

                boolean iaPudoMoverse = aiTurn();

                if (!iaPudoMoverse) {

                    System.out.println();
                    System.out.println("TU GANAS.");

                    return;
                }
            }
        }
    }

    private void imprimirEncabezado() {

        System.out.println();
        System.out.println(
                "=============================="
        );

        System.out.println(
                "       JUEGO 8 x 8"
        );

        System.out.println(
                "      HUMANO VS IA"
        );

        System.out.println(
                "=============================="
        );

        System.out.println();
    }

    /**
     * Pregunta al humano si quiere jugar otra partida.
     *
     * @return true si respondio "s" (si), false en
     *         cualquier otro caso.
     */
    private boolean preguntarJugarDeNuevo() {

        System.out.println();
        System.out.print(
                "¿Quieres jugar de nuevo? (s/n): "
        );

        String respuesta = scanner.next();

        return respuesta.trim().equalsIgnoreCase("s");
    }

    /**
     * Turno del humano: muestra los movimientos
     * disponibles, lee fila/columna, y valida la
     * eleccion.
     *
     * @return true si el movimiento fue valido y se
     *         aplico, false si el humano eligio una
     *         casilla invalida (con lo cual pierde).
     */
    private boolean humanTurn() {

        System.out.println();
        System.out.println(
                "========== TU TURNO =========="
        );

        List<Position> moves =
                MoveGenerator.getLegalMoves(state);

        System.out.println(
                "Movimientos disponibles:"
        );

        for (Position move : moves) {

            System.out.println(
                    " -> " + move
            );
        }

        System.out.println();

        System.out.print(
                "Ingresa fila: "
        );

        int row = scanner.nextInt();

        System.out.print(
                "Ingresa columna: "
        );

        int col = scanner.nextInt();

        Position selected = new Position(row, col);

        /*
         * Comprobamos que el movimiento
         * sea válido.
         */
        if (!moves.contains(selected)) {

            System.out.println();

            System.out.println(
                    "Movimiento invalido."
            );

            System.out.println(
                    "Debes elegir una de las"
                    + " casillas listadas."
            );

            return false;
        }

        /*
         * Realizamos el movimiento.
         */
        state = state.makeMove(selected);

        return true;
    }

    /**
     * Turno de la IA: le pide a MinimaxAI el mejor
     * movimiento y lo aplica.
     *
     * @return true si la IA pudo moverse, false si no
     *         tenia movimientos disponibles (con lo
     *         cual pierde).
     */
    private boolean aiTurn() {

        System.out.println();
        System.out.println(
                "========= TURNO DE LA IA ========="
        );

        System.out.println(
                "La IA esta pensando..."
        );

        Position bestMove = ai.getBestMove(state);

        /*
         * Por seguridad.
         */
        if (bestMove == null) {

            System.out.println(
                    "La IA no puede moverse."
            );

            return false;
        }

        System.out.println(
                "La IA se mueve a: " + bestMove
        );

        /*
         * Realizamos el movimiento
         * de la IA.
         */
        state = state.makeMove(bestMove);

        return true;
    }

    /**
     * Se llama cuando, al empezar un turno, el jugador
     * actual ya no tiene ningun movimiento disponible
     * (quedo "atrapado"). Informa quien gano.
     */
    private void finishGame() {

        System.out.println();

        if (state.getCurrentPlayer() == 1) {

            System.out.println(
                    "No tienes movimientos disponibles."
            );

            System.out.println(
                    "LA IA GANA."
            );

        } else {

            System.out.println(
                    "La IA no tiene movimientos disponibles."
            );

            System.out.println(
                    "TU GANAS."
            );
        }
    }

    /**
     * Imprime el tablero actual en la consola, usando
     * el bit correspondiente de la mascara para saber
     * si una casilla esta usada.
     */
    private void printBoard() {

        System.out.println();

        System.out.println(
                "       0 1 2 3 4 5 6 7"
        );

        System.out.println(
                "      -----------------"
        );

        for (int row = 0; row < 8; row++) {

            System.out.print(
                    "   " + row + " | "
            );

            for (int col = 0; col < 8; col++) {

                Position position =
                        new Position(row, col);

                /*
                 * Humano.
                 */
                if (position.equals(
                        state.getHumanPosition())) {

                    System.out.print(
                            "H "
                    );
                }

                /*
                 * IA.
                 */
                else if (position.equals(
                        state.getAiPosition())) {

                    System.out.print(
                            "A "
                    );
                }

                /*
                 * Casilla utilizada
                 * (bit encendido en la mascara).
                 */
                else if (
                        state.isUsed(row, col)) {

                    System.out.print(
                            "X "
                    );
                }

                /*
                 * Casilla disponible.
                 */
                else {

                    System.out.print(
                            ". "
                    );
                }
            }

            System.out.println();
        }

        System.out.println();

        System.out.println(
                "H = Humano"
        );

        System.out.println(
                "A = IA"
        );

        System.out.println(
                "X = Casilla utilizada"
        );

        System.out.println(
                ". = Casilla disponible"
        );
    }
}
