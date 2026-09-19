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

}