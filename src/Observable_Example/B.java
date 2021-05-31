package Observable_Example;

import java.util.Observable;
import java.util.Observer;

public class B implements Observer {
    private A a0, a1, a2;

    public B(A a0, A a1, A a2) {
        this.a0 = a0;
        this.a1 = a1;
        this.a2 = a2;
    }

    @Override
    public void update(Observable o, Object arg) {
        // this is invoked upon any change to object "a"
        // now we can actively get the state of object "a"
        if (o == a0) {
            System.out.println("a change has occurred");
            System.out.println("X = " + a0.getX() + ", Y = " + a0.getY());
        }
    }
}
