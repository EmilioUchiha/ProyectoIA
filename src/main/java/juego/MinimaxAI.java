package juego;

import java.util.List;

/**
 * Inteligencia artificial del juego (jugador 2).
 *
 * Implementa el algoritmo MINIMAX con PODA ALFA-BETA
 * para elegir, entre los movimientos posibles, el que
 * lleva a la mejor situacion posible para la IA
 * asumiendo que el humano tambien juega de forma
 * optima (intentando minimizar el resultado de la IA).
 *
 * Minimax explora el arbol de posibles jugadas hasta
 * una profundidad maxima (maxDepth). Al llegar a esa
 * profundidad, en vez de seguir explorando (lo cual
 * seria carisimo), se le pide a una heuristica que
 * "adivine" un puntaje aproximado del estado (ver
 * HeuristicaEvaluacion).
 *
 * PODA ALFA-BETA: mientras se explora el arbol, se
 * llevan dos limites (alpha y beta). Si en algun punto
 * se detecta que una rama no puede mejorar el resultado
 * que ya se tiene garantizado por otro lado (beta menor
 * o igual a alpha), se deja de explorar esa rama: da el
 * mismo resultado final, pero mucho mas rapido.
 *
 * PATRON DE DISEÑO: STRATEGY.
 * La heuristica de evaluacion NO esta fija dentro de
 * esta clase: se recibe en el constructor como una
 * HeuristicaEvaluacion. Esto separa el algoritmo
 * (Minimax + poda) de la heuristica (que tan buena es
 * una posicion), permitiendo cambiar la heuristica sin
 * tocar el algoritmo.
 */
public class MinimaxAI {
    /**
     * Profundidad maxima que analizara Minimax antes
     * de recurrir a la heuristica de evaluacion.
     */
    private final int maxDepth;
    /**
     * Estrategia usada para evaluar un estado cuando
     * se alcanza la profundidad maxima de busqueda.
     */
    private final HeuristicaEvaluacion heuristica;
    /**
     * Crea la IA usando la heuristica de movilidad
     * por defecto (MovilidadHeuristica).
     *
     * @param maxDepth profundidad maxima de busqueda.
     */
    public MinimaxAI(int maxDepth) {
        this(maxDepth, new MovilidadHeuristica());
    }
    /**
     * Crea la IA indicando explicitamente que
     * heuristica de evaluacion usar (patron Strategy).
     * Util si en el futuro se quiere experimentar con
     * otra forma de evaluar el tablero sin tocar esta
     * clase.
     *
     * @param maxDepth   profundidad maxima de busqueda.
     * @param heuristica estrategia de evaluacion a usar.
     */
    public MinimaxAI(int maxDepth, HeuristicaEvaluacion heuristica) {
        this.maxDepth = maxDepth;
        this.heuristica = heuristica;
    }
    /**
     * Busca el mejor movimiento para la IA desde el
     * estado actual, probando cada movimiento legal y
     * quedandose con el de mayor valor segun Minimax.
     *
     * @param state estado actual del juego (debe ser
     *              turno de la IA).
     * @return el mejor movimiento encontrado, o null si
     *         la IA no tiene movimientos disponibles.
     */
    public Position getBestMove(GameState state) {
        List<Position> moves =
                MoveGenerator.getLegalMoves(state);
        /*
         * Si la IA no tiene movimientos,
         * no puede realizar ningún movimiento.
         */
        if (moves.isEmpty()) {
            return null;
        }
        int bestValue =
                Integer.MIN_VALUE;

        Position bestMove =
                null;
        int alpha =
                Integer.MIN_VALUE;
        int beta =
                Integer.MAX_VALUE;
        /*
         * Probamos cada movimiento posible.
         */
        for (Position move : moves) {
            GameState nextState =
                    state.makeMove(move);
            int value =
                    minimax(
                            nextState,
                            maxDepth - 1,
                            alpha,
                            beta
                    );

            /*
             * Buscamos el movimiento
             * con mayor valor.
             */
            if (value > bestValue) {

                bestValue = value;

                bestMove = move;
            }

            /*
             * Actualizamos Alpha.
             */
            if (bestValue > alpha) {
                alpha = bestValue;
            }
        }

        return bestMove;
    }

    /**
     * Nucleo recursivo de Minimax con poda Alfa-Beta.
     *
     * @param state estado a evaluar/explorar.
     * @param depth cuantos niveles mas se puede bajar
     *              antes de tener que usar la
     *              heuristica en vez de seguir explorando.
     * @param alpha mejor valor que el maximizador (IA)
     *              tiene garantizado hasta ahora.
     * @param beta  mejor valor que el minimizador
     *              (humano) tiene garantizado hasta ahora.
     * @return el valor calculado para este estado.
     */
    private int minimax(
            GameState state,
            int depth,
            int alpha,
            int beta) {

        List<Position> moves =
                MoveGenerator.getLegalMoves(state);

        /*
         * CASO TERMINAL
         *
         * Si el jugador que tiene el turno
         * no tiene movimientos, pierde.
         */
        if (moves.isEmpty()) {

            /*
             * Si no puede moverse la IA,
             * la IA pierde.
             */
            if (state.getCurrentPlayer() == 2) {

                return -100000;
            }

            /*
             * Si no puede moverse el humano,
             * la IA gana.
             */
            return 100000;
        }

        /*
         * Si llegamos a la profundidad máxima,
         * evaluamos el estado con la heuristica
         * (patron Strategy).
         */
        if (depth == 0) {

            return heuristica.evaluar(state);
        }

        /*
         * TURNO DE LA IA
         *
         * La IA quiere MAXIMIZAR.
         */
        if (state.getCurrentPlayer() == 2) {

            int maxEvaluation =
                    Integer.MIN_VALUE;

            for (Position move : moves) {

                GameState nextState =
                        state.makeMove(move);

                int evaluation =
                        minimax(
                                nextState,
                                depth - 1,
                                alpha,
                                beta
                        );

                if (evaluation > maxEvaluation) {

                    maxEvaluation =
                            evaluation;
                }

                if (maxEvaluation > alpha) {

                    alpha =
                            maxEvaluation;
                }

                /*
                 * PODA ALFA-BETA:
                 * el humano (mas arriba en el arbol)
                 * ya tiene garantizado un valor <= beta;
                 * si esta rama ya supero eso, el humano
                 * jamas la elegiria, asi que no hace
                 * falta seguir explorandola.
                 */
                if (beta <= alpha) {

                    break;
                }
            }

            return maxEvaluation;
        }

        /*
         * TURNO DEL HUMANO
         *
         * El humano quiere MINIMIZAR
         * el resultado de la IA.
         */
        else {

            int minEvaluation =
                    Integer.MAX_VALUE;

            for (Position move : moves) {

                GameState nextState =
                        state.makeMove(move);

                int evaluation =
                        minimax(
                                nextState,
                                depth - 1,
                                alpha,
                                beta
                        );

                if (evaluation < minEvaluation) {

                    minEvaluation =
                            evaluation;
                }

                if (minEvaluation < beta) {

                    beta =
                            minEvaluation;
                }

                /*
                 * PODA ALFA-BETA (simetrica a la de
                 * arriba, ahora desde el punto de vista
                 * del humano).
                 */
                if (beta <= alpha) {

                    break;
                }
            }
            return minEvaluation;
        }
    }
}
