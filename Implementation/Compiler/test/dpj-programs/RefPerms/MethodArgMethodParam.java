/**
 * Test binding to method argument
 */
class MethodArgument {
    class Data {}
    <refgroup G>void m1(unique(G) Data data) {}
    void m2() {
	refgroup g;
	unique(g) Data data = new Data();
	this.<refgroup g>m1(data);
    }
}