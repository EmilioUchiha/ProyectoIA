package juego;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcula los movimientos legales del jugador que tiene
 * el turno en un GameState dado.
 *
 * Reglas del juego:
 *   1. Solo se puede mover una casilla, en una de las
 *      4 direcciones ortogonales (arriba, abajo,
 *      izquierda, derecha).
 *   2. El tablero es CIRCULAR (torico): si te sales
 *      por un borde, apareces del lado opuesto. Por
 *      eso las coordenadas se recalculan con modulo.
 *   3. No se puede pisar una casilla ya usada (rastro
 *      dejado por cualquiera de los dos jugadores).
 *   4. No se puede pisar la casilla donde esta parado
 *      el oponente EN ESTE MOMENTO.
 *
 * La regla 4 es importante y facil de pasar por alto:
 * la mascara de bits "used" solo marca las casillas que
 * un jugador ABANDONA al moverse. La casilla donde el
 * oponente esta parado ahora mismo NO queda marcada ahi
 * (salvo que coincida con alguna de las dos posiciones
 * iniciales). Si no se excluyera aparte, un jugador
 * podria moverse directo sobre el otro, lo cual no
 * deberia estar permitido.
 */
public class MoveGenerator {

    /*
     * Movimientos:
     *
     * Arriba
     * Abajo
     * Izquierda
     * Derecha
     */
    private static final int[][] DIRECTIONS = {

        {-1, 0},
        {1, 0},
        {0, -1},
        {0, 1}
    };

    /**
     * @param state estado actual del juego.
     * @return lista de posiciones a las que el jugador
     *         con el turno se puede mover legalmente
     *         (puede estar vacia si no tiene movimientos).
     */
    public static List<Position> getLegalMoves(
            GameState state) {

        List<Position> moves =
                new ArrayList<>();

        Position current =
                state.getCurrentPosition();

        /*
         * Posicion actual del oponente (ver el
         * javadoc de la clase: hay que excluirla
         * aparte, la mascara "used" no la cubre).
         */
        Position opponent =
                (state.getCurrentPlayer() == 1)
                        ? state.getAiPosition()
                        : state.getHumanPosition();

        for (int[] direction : DIRECTIONS) {

            int row =
                    current.getRow()
                    + direction[0];

            int col =
                    current.getCol()
                    + direction[1];

            /*
             * El tablero es circular.
             *
             * Si estamos en fila 0 y vamos arriba:
             *
             * 0 -> 7
             *
             * Si estamos en fila 7 y vamos abajo:
             *
             * 7 -> 0
             */
            row = (row + GameState.SIZE)
                    % GameState.SIZE;

            col = (col + GameState.SIZE)
                    % GameState.SIZE;

            Position candidate =
                    new Position(row, col);

            /*
             * Es un movimiento valido si la casilla
             * NO esta utilizada (bit apagado en la
             * mascara) Y NO es la casilla donde esta
             * parado el oponente ahora mismo.
             */
            if (!state.isUsed(row, col)
                    && !candidate.equals(opponent)) {

                moves.add(candidate);
            }
        }

        return moves;
    }
}
