package juego;

/**
 * Representa una "foto" completa del juego en un
 * momento dado: que casillas estan usadas, donde esta
 * el humano, donde esta la IA, y de quien es el turno.
 *
 * Es INMUTABLE: makeMove(...) nunca modifica el objeto
 * actual, siempre devuelve un GameState nuevo. Esto es
 * clave para Minimax, que necesita poder "probar" un
 * movimiento, explorar lo que pasaria despues, y luego
 * volver atras sin haber roto el estado original: al no
 * modificar nada in-place, no hace falta "deshacer"
 * movimientos.
 *
 * TABLERO COMO MASCARA DE BITS
 * ----------------------------
 * En vez de un boolean[8][8] (64 casillas -> 64
 * variables boolean, cada una ocupando en la practica
 * varios bytes en memoria y habiendo que clonar el
 * arreglo entero en cada movimiento), el tablero se
 * representa con un solo "long" de 64 bits.
 *
 *   - Cada bit representa una casilla.
 *   - bit en 1 -> casilla utilizada / bloqueada.
 *   - bit en 0 -> casilla libre.
 *   - indice del bit = fila * 8 + columna (0..63).
 *
 * Ventajas:
 *   - Copiar el estado es copiar un numero (rapidisimo),
 *     en vez de clonar una matriz.
 *   - Marcar una casilla como usada es un simple OR de
 *     bits: used | (1L << indice).
 *   - Preguntar si una casilla esta usada es un simple
 *     AND: (used >> indice) & 1L.
 *
 * Minimax con profundidad 6 explora miles de estados,
 * asi que esta representacion hace que el algoritmo sea
 * bastante mas eficiente que con una matriz booleana.
 */
public class GameState {

    /** Tamaño del tablero (8x8). */
    public static final int SIZE = 8;

    /**
     * Mascara de bits del tablero (ver documentacion
     * de la clase). Bit en 1 = casilla utilizada.
     */
    private long used;

    /** Posicion actual del jugador humano. */
    private Position humanPosition;

    /** Posicion actual de la IA. */
    private Position aiPosition;

    /**
     * Jugador que tiene el turno.
     * 1 = Humano, 2 = IA.
     */
    private int currentPlayer;

    /**
     * Crea un estado del juego a partir de sus datos.
     * Normalmente no se llama directamente desde fuera
     * salvo para construir el siguiente estado dentro
     * de makeMove(), o el estado inicial dentro de
     * crearEstadoInicial().
     *
     * @param used           mascara de bits del tablero.
     * @param humanPosition  posicion del humano.
     * @param aiPosition     posicion de la IA.
     * @param currentPlayer  1 = humano, 2 = IA.
     */
    public GameState(
            long used,
            Position humanPosition,
            Position aiPosition,
            int currentPlayer) {

        this.used = used;
        this.humanPosition = humanPosition;
        this.aiPosition = aiPosition;
        this.currentPlayer = currentPlayer;
    }

    /**
     * PATRON DE DISEÑO: FACTORY METHOD.
     *
     * Centraliza la creacion del estado inicial del
     * juego: tablero vacio salvo por las dos posiciones
     * de partida, Humano en (3,3), IA en (3,4), y el
     * turno inicial para el Humano.
     *
     * Tanto la version de consola (Game) como la
     * version grafica (GameGUI) usan este mismo metodo
     * para arrancar la partida y tambien para
     * REINICIARLA (boton "Volver a jugar"), evitando
     * duplicar esta logica en dos lugares distintos y
     * que se puedan desincronizar con el tiempo.
     *
     * @return un nuevo GameState con la configuracion
     *         inicial estandar del juego.
     */
    public static GameState crearEstadoInicial() {

        long used = 0L;

        Position human = new Position(3, 3);
        Position aiPosition = new Position(3, 4);

        /*
         * Las posiciones iniciales ya estan
         * ocupadas: encendemos sus bits.
         */
        used |= (1L << toIndex(3, 3));
        used |= (1L << toIndex(3, 4));

        return new GameState(used, human, aiPosition, 1);
    }

    public long getUsed() {
        return used;
    }

    /**
     * Convierte una coordenada (fila, columna) al
     * indice del bit correspondiente dentro de la
     * mascara "used".
     *
     * @param row fila (0..7).
     * @param col columna (0..7).
     * @return indice de bit (0..63).
     */
    public static int toIndex(int row, int col) {
        return row * SIZE + col;
    }

    /**
     * Indica si la casilla (row, col) esta utilizada,
     * revisando unicamente el bit correspondiente en
     * la mascara.
     */
    public boolean isUsed(int row, int col) {

        int index = toIndex(row, col);

        return ((used >> index) & 1L) != 0L;
    }

    public Position getHumanPosition() {
        return humanPosition;
    }

    public Position getAiPosition() {
        return aiPosition;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * @return la posicion del jugador que tiene
     *         el turno actualmente.
     */
    public Position getCurrentPosition() {

        if (currentPlayer == 1) {
            return humanPosition;
        }

        return aiPosition;
    }

    /**
     * Crea un NUEVO estado que resulta de mover al
     * jugador actual a newPosition. No modifica this;
     * GameState es inmutable.
     *
     * La casilla que el jugador abandona se marca como
     * usada (se enciende su bit), la posicion de ese
     * jugador cambia a newPosition, y el turno pasa al
     * otro jugador.
     *
     * @param newPosition casilla destino, ya validada
     *                    previamente por MoveGenerator.
     * @return el estado resultante despues del movimiento.
     */
    public GameState makeMove(Position newPosition) {

        Position currentPosition =
                getCurrentPosition();

        long newUsed =
                used | (1L << toIndex(
                        currentPosition.getRow(),
                        currentPosition.getCol()
                ));

        if (currentPlayer == 1) {

            return new GameState(
                    newUsed,
                    newPosition,
                    aiPosition,
                    2
            );

        } else {

            return new GameState(
                    newUsed,
                    humanPosition,
                    newPosition,
                    1
            );
        }
    }
}
