package model.cells;

public class CellGameOfLife extends Cell {
    public static class State {
        public static byte DEAD = 0;
        public static byte ALIVE = 1;
    }

    public CellGameOfLife() {
        state = State.DEAD;
    }
}
