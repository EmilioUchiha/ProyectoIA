public class Mapa{
    private static final int tamano = 8;
    private int[][] mapa;

    public Mapa() {
        mapa = new int[tamano][tamano];
        inicializarMapa();
    }

    private void inicializarMapa() {
        for (int i = 0; i < tamano; i++) {
            for (int j = 0; j < tamano; j++) {
                mapa[i][j] = 0; // Inicializa todas las posiciones del mapa con 0
            }
        }
    }

    public void visitado(int x, int y) {
        if (x >= 0 && x < tamano && y >= 0 && y < tamano) {
            mapa[y][x] = -1; // Marca la posición como visitada con -1
        }
    }

    public int[][] getMapa() {
        return mapa;
    }

    public int getCasilla(int x, int y) {
        if (x >= 0 && x < tamano && y >= 0 && y < tamano) {
            return mapa[y][x];
        }
        return -1; // Retorna -1 si las coordenadas están fuera del mapa
    }
}