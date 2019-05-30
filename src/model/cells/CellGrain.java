package model.cells;
import static model.cells.CellGrain.Availability.*;

public class CellGrain extends Cell {
    public static class State {
        public static byte EMPTY = 0;
        public static byte GRAIN = 1;
    }

    public enum Availability {
        AVAILABLE, UNAVAILABLE
    }

    private int id;
    private Availability availability;

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    private int energy;

    public CellGrain() {
        state = State.EMPTY;
        availability = AVAILABLE;
        id = 0;
        energy = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }
}
