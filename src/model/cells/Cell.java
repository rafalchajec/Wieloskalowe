package model.cells;

public abstract class Cell {

    public byte state;

    public byte getState(){
        return state;
    }

    public void setState(byte state){
        this.state = state;
    }
}
