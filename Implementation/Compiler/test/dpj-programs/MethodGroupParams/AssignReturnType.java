/**
 * Test assignment of return type with paramter args
 */
class AssignReturnType {
    class Data<refgroup G> {}
    <refgroup G>unique(G) Data<G> m1() { return new Data<G>(); }
    void m2() {
	refgroup g;
	unique(g) Data<g> data = this.<refgroup g>m1();
    }
}