# Juego 8x8 - Humano vs IA

Juego de estrategia tipo "Tron": dos jugadores (Humano e IA) se mueven por un
tablero de 8x8, dejando un rastro de casillas bloqueadas detrás de ellos.
Pierde el primero que se queda sin movimientos posibles.

Incluye dos formas de jugar (consola y ventana grafica con Swing) y una IA
que usa el algoritmo **Minimax con poda Alfa-Beta**.

---

## Tabla de contenidos

1. [Reglas del juego](#reglas-del-juego)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Como compilar y ejecutar](#como-compilar-y-ejecutar)
4. [Descripcion de cada clase](#descripcion-de-cada-clase)
5. [El tablero como mascara de bits](#el-tablero-como-mascara-de-bits)
6. [Como piensa la IA (Minimax + poda Alfa-Beta)](#como-piensa-la-ia-minimax--poda-alfa-beta)
7. [Patrones de diseño usados](#patrones-de-diseño-usados)
8. [Interfaz grafica (Swing)](#interfaz-grafica-swing)
9. [Requisitos](#requisitos)

---

## Reglas del juego

- El tablero es de **8x8** casillas.
- El Humano arranca en la casilla `(3,3)` y la IA en `(3,4)`.
- En cada turno, el jugador se mueve **una casilla** en una de 4 direcciones:
  arriba, abajo, izquierda o derecha (no hay movimientos en diagonal).
- El tablero es **circular (torico)**: si te sales por un borde, aparecés
  del lado opuesto (por ejemplo, salir por la fila 0 hacia arriba te lleva
  a la fila 7).
- Cada casilla que un jugador abandona queda **bloqueada** para siempre
  (el "rastro").
- **No se puede pisar** una casilla ya bloqueada, ni la casilla donde está
  parado el oponente en ese momento.
- **Pierde** el jugador que, en su turno, no tiene ningún movimiento legal
  disponible.

---

## Estructura del proyecto

```
outputs/
├── README.md                      <- este archivo
└── juego/                         <- carpeta del paquete "juego" (obligatoria por el package)
    ├── Main.java                  <- punto de entrada (lanza la GUI)
    ├── Position.java              <- coordenada (fila, columna)
    ├── GameState.java             <- estado inmutable del juego (mascara de bits)
    ├── MoveGenerator.java         <- calcula movimientos legales
    ├── HeuristicaEvaluacion.java  <- interfaz Strategy para la heuristica
    ├── MovilidadHeuristica.java   <- heuristica concreta (movilidad)
    ├── MinimaxAI.java             <- IA: Minimax con poda Alfa-Beta
    ├── Game.java                  <- version de CONSOLA del juego
    └── GameGUI.java               <- version GRAFICA (Swing) del juego
```

Todas las clases declaran `package juego;`, por lo que **deben** estar
físicamente dentro de una carpeta llamada `juego/` para que Java las
reconozca como parte del mismo paquete.

---

## Como compilar y ejecutar

Desde la carpeta que **contiene** a `juego/` (es decir, un nivel arriba):

```bash
# Compilar todo el paquete
javac juego/*.java

# Ejecutar la version grafica (por defecto)
java juego.Main
```

Si preferís la **version de consola**, hay dos opciones:

- Ejecutarla directamente: `java juego.Game` no funciona porque `Game` no
  tiene `main`. En su lugar, editá `Main.java` y reemplazá su contenido por:

  ```java
  package juego;

  public class Main {
      public static void main(String[] args) {
          Game game = new Game();
          game.start();
      }
  }
  ```

  y volvé a compilar/ejecutar.

> **Nota sobre versiones de Java:** el codigo es compatible con Java 8 en
> adelante (se evitó `List.of()` y el diamante `<>` en clases anonimas,
> que requieren Java 9+). Si tu entorno usa `javac -source 8`, no deberia
> haber problemas.

---

## Descripcion de cada clase

### `Position`
Representa una coordenada `(fila, columna)`. Es **inmutable** y redefine
`equals()`/`hashCode()` para poder comparar casillas por valor (por ejemplo,
para saber si una casilla elegida está en la lista de movimientos legales).

### `GameState`
La "foto" completa de una partida en un momento dado: qué casillas están
usadas, dónde está cada jugador, y de quién es el turno. Es **inmutable**:
`makeMove(...)` nunca modifica el objeto actual, siempre devuelve uno nuevo.
Esto es lo que le permite a Minimax "probar" jugadas sin romper el estado
real y sin tener que "deshacer" nada. También expone el método de fábrica
`crearEstadoInicial()` (ver [Patrones de diseño](#patrones-de-diseño-usados)).

### `MoveGenerator`
Clase de utilidad (métodos `static`) que calcula los movimientos legales
del jugador en turno: prueba las 4 direcciones, aplica el envoltorio
circular (módulo), y descarta las casillas ya usadas o la casilla donde
está parado el oponente ahora mismo.

### `HeuristicaEvaluacion`
Interfaz que define el contrato `evaluar(GameState) -> int`. Es la pieza
central del patrón **Strategy**: cualquier forma de "puntuar" un tablero
puede implementarse acá sin tocar el algoritmo de Minimax.

### `MovilidadHeuristica`
Implementación concreta de `HeuristicaEvaluacion`. Compara cuántos
movimientos tiene disponibles cada jugador (más opciones = mejor posición,
porque reduce el riesgo de quedar encerrado).

### `MinimaxAI`
El "cerebro" de la IA. Implementa Minimax con poda Alfa-Beta hasta una
profundidad configurable (por defecto, 6). Recibe una `HeuristicaEvaluacion`
por constructor para evaluar los estados cuando llega al límite de
profundidad.

### `Game`
Versión de **consola**: imprime el tablero como texto, lee los movimientos
del humano por teclado, y al terminar una partida pregunta si se quiere
jugar de nuevo.

### `GameGUI`
Versión **gráfica (Swing)**: tablero de 64 botones, colores por estado
(humano, IA, usada, movimiento disponible), botón **"Volver a jugar"**, y
cálculo de la IA en un hilo aparte (`SwingWorker`) para no congelar la
ventana mientras piensa.

### `Main`
Punto de entrada de la aplicación: lanza `GameGUI` dentro del hilo de
interfaz de Swing (Event Dispatch Thread).

---

## El tablero como mascara de bits

En vez de un `boolean[8][8]`, el tablero se representa con un solo `long`
de 64 bits: cada bit es una casilla.

```
índice del bit = fila * 8 + columna      (0 a 63)

marcar (3,3) como usada:   used |= (1L << indice)     // OR: prende el bit
consultar si está usada:   (used >> indice) & 1L       // AND: lee el bit
```

Ventaja principal: copiar el estado (algo que Minimax hace miles de veces)
es copiar **un número**, en vez de clonar una matriz de 64 posiciones.

---

## Como piensa la IA (Minimax + poda Alfa-Beta)

- La IA (jugador 2) quiere **maximizar** el resultado; el Humano (jugador 1)
  quiere **minimizar**lo. El algoritmo alterna entre ambos criterios a
  medida que baja de nivel en el árbol de jugadas posibles.
- Si un jugador se queda sin movimientos, esa rama tiene un valor extremo:
  `+100000` (gana la IA) o `-100000` (pierde la IA).
- Si se llega a la profundidad máxima sin que termine el juego, se usa la
  `HeuristicaEvaluacion` para estimar qué tan buena es esa posición.
- **Poda Alfa-Beta**: mientras se explora el árbol, se llevan dos límites
  (`alpha` y `beta`). Si en algún punto `beta <= alpha`, esa rama ya no
  puede cambiar la decisión final, así que se deja de explorar — el
  resultado final es el mismo, pero mucho más rápido.

---

## Patrones de diseño usados

### Factory Method — `GameState.crearEstadoInicial()`
Centraliza cómo arranca (o se reinicia) una partida: tablero vacío salvo
las dos posiciones iniciales, Humano en `(3,3)`, IA en `(3,4)`, turno del
Humano. Tanto `Game` como `GameGUI` lo usan, evitando duplicar esa lógica
en dos lugares que con el tiempo podrían desincronizarse.

### Strategy — `HeuristicaEvaluacion` / `MovilidadHeuristica`
Separa el algoritmo de Minimax (código delicado: recursión + poda) de la
heurística de evaluación (la parte que uno querría poder cambiar o
mejorar). `MinimaxAI` recibe la estrategia por constructor:

```java
new MinimaxAI(6);                          // usa MovilidadHeuristica por defecto
new MinimaxAI(6, new MovilidadHeuristica()); // explicito
new MinimaxAI(6, new OtraHeuristicaTuya()); // cualquier otra implementacion
```

---

## Interfaz grafica (Swing)

- **Tablero**: 64 `JButton` en un `GridLayout(8,8)`.
    - Verde = Humano, Rojo = IA, Gris oscuro = casilla usada, Amarillo =
      movimiento disponible, Blanco = libre.
- **Boton "Volver a jugar"**: reinicia la partida en cualquier momento
  (incluso a mitad de juego). Se deshabilita transitoriamente mientras la
  IA está pensando, para evitar aplicar un movimiento calculado sobre una
  partida que ya fue reiniciada.
- **Calculo de la IA en segundo plano**: `MinimaxAI.getBestMove(...)` se
  ejecuta dentro de un `SwingWorker`, en un hilo separado del hilo de la
  interfaz (EDT). Así la ventana no se congela mientras la IA piensa; el
  resultado se aplica de vuelta sobre la interfaz una vez que termina el
  cálculo.

---

## Requisitos

- **JDK 8 o superior** (el código es compatible con Java 8; no usa
  `List.of()` ni el diamante `<>` en clases anónimas).
- No requiere librerías externas: solo usa `javax.swing`, `java.util` y
  `java.util.Scanner`, que vienen con el JDK estándar.