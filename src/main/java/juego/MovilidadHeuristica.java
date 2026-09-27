package juego;

/**
 * Implementacion concreta (Strategy) de
 * HeuristicaEvaluacion, basada en "movilidad".
 *
 * La idea: cuantos mas movimientos futuros tenga
 * disponibles un jugador, menos probable es que quede
 * "encerrado" sin poder moverse (que es la condicion de
 * derrota en este juego). Por eso se compara la
 * cantidad de movimientos legales de la IA contra la
 * del humano.
 */
public class MovilidadHeuristica implements HeuristicaEvaluacion {

    @Override
    public int evaluar(GameState state) {

        int movimientosIA =
                contarMovimientos(state, 2);

        int movimientosHumano =
                contarMovimientos(state, 1);

        /*
         * Tener mas movimientos disponibles
         * es mejor para la IA.
         */
        return (movimientosIA * 10)
                - (movimientosHumano * 10);
    }

    /**
     * Cuenta cuantos movimientos legales tendria un
     * jugador especifico SI fuera su turno en este
     * estado, sin modificar el estado original (se
     * arma un estado temporal solo para la consulta,
     * ya que GameState es inmutable).
     *
     * @param state  estado del juego actual.
     * @param player jugador a evaluar (1 = humano, 2 = IA).
     * @return cantidad de movimientos legales disponibles
     *         para ese jugador.
     */
    private int contarMovimientos(GameState state, int player) {

        GameState estadoTemporal =
                new GameState(
                        state.getUsed(),
                        state.getHumanPosition(),
                        state.getAiPosition(),
                        player
                );

        return MoveGenerator
                .getLegalMoves(estadoTemporal)
                .size();
    }
}
