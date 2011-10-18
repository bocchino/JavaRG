/**
 * Error condition:  wrong return type after substitution
 */
class ErrorAssignReturnType {
    class Data<refgroup G> {}
    <refgroup G>unique(G) Data<G> m1() { return new Data<G>(); }
    void m2() {
	refgroup g1, g2;
	unique(g1) Data<g1> data = this.<refgroup g2>m1();
    }
}