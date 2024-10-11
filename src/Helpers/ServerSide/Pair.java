package Helpers.ServerSide;

public class Pair<F, S> {
    private F A;
    private S B;
    public Pair(F A, S B) {

        this.A = A;
        this.B = B;
    }
    public F getA() {
        return A;
    }
    public S getB() {
        return B;
    }
    public void setA(F A) {
        this.A = A;
    }
    public void setB(S B) {
        this.B = B;
    }
}
