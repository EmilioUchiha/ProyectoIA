package juego;

import java.util.Objects;

/**
 * Representa una coordenada (fila, columna) dentro del
 * tablero de 8x8.
 *
 * Es un objeto inmutable y muy simple ("value object"):
 * una vez creado, su fila y columna no cambian. Por eso
 * se puede reutilizar libremente sin miedo a que alguien
 * lo modifique por accidente, y se puede comparar con
 * equals()/hashCode() para saber si dos posiciones son
 * "la misma casilla" (por ejemplo, para ver si el
 * movimiento elegido por el humano esta en la lista de
 * movimientos legales).
 */
public class Position {

    private int row;
    private int col;

    /**
     * @param row fila (0..7).
     * @param col columna (0..7).
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Dos posiciones son iguales si tienen la misma
     * fila y la misma columna. Esto es lo que permite,
     * por ejemplo, usar moves.contains(selected) para
     * saber si una casilla elegida por el jugador es
     * un movimiento valido.
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Position position = (Position) obj;

        return row == position.row
                && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Representacion legible, util para depurar o para
     * mostrar los movimientos disponibles en la consola.
     */
    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
