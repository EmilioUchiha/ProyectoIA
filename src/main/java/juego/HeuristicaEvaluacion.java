package juego;

/**
 * PATRON DE DISEÑO: STRATEGY.
 *
 * Representa el "criterio" que usa MinimaxAI para
 * asignarle un puntaje a un estado del juego cuando la
 * busqueda llega al limite de profundidad y hay que
 * "adivinar" que tan buena es esa posicion sin seguir
 * explorando.
 *
 * ¿Por que una interfaz y no un metodo fijo dentro de
 * MinimaxAI? Porque asi el algoritmo de Minimax (que es
 * codigo delicado: recursion + poda alfa-beta) queda
 * totalmente separado de la heuristica (que es la parte
 * que uno querria poder cambiar o mejorar sin tocar el
 * algoritmo). Se le puede pasar a MinimaxAI cualquier
 * implementacion de esta interfaz -por ejemplo, una
 * heuristica de movilidad (MovilidadHeuristica), una
 * mas agresiva, o una que combine varios factores- sin
 * modificar una sola linea de MinimaxAI.
 */
public interface HeuristicaEvaluacion {

    /**
     * Calcula el puntaje de un estado del juego.
     *
     * Por convencion: un valor MAS ALTO siempre debe
     * representar una situacion mejor para la IA
     * (jugador 2), y un valor MAS BAJO una situacion
     * mejor para el humano (jugador 1). MinimaxAI
     * depende de esta convencion para maximizar en los
     * turnos de la IA y minimizar en los del humano.
     *
     * @param state estado del juego a evaluar.
     * @return puntaje numerico del estado.
     */
    int evaluar(GameState state);
}
