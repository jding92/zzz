package ca.ubc.cs.cpsc210.translink.parsers;

/**
 * Created by norm on 2016-09-05.
 * A simple pair of two identical types
 */
public class Pair<T1> {
    public T1 first;
    public T1 second;

    public Pair(T1 first, T1 second) {
        this.first = first;
        this.second = second;
    }

    public void swap() {
        T1 tmp = first;
        first = second;
        second = tmp;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
