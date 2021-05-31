package Observable_Example;

public class Main {
    public static void main(String[] args) {
        A a = new A();
        B b = new B(a, null, null);
        // inherited from Observable
        // add b to a's list of Observers
        a.addObserver(b);

        a.setXY(5, 5);
    }
}
