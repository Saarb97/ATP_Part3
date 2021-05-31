package Observable_Example;

import java.util.Observable;

public class A extends Observable {
    private int x, y;

    public A() {
        x = 0;
        y = 0;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
        // actively notify all observers
        // and invoke their update method
        setChanged();
        notifyObservers();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
