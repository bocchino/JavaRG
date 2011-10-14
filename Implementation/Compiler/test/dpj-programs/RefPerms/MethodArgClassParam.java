/**
 * Test binding to method argument with class ref group param
 */
class MethodArgClassParam<refgroup G> {
    static class Data {}
    void m1(unique(G) Data data) {}
    void m2() {
	refgroup g;
	MethodArgClassParam<g> macp = new MethodArgClassParam<g>();
	unique(g) Data data = new Data();
	macp.m1(data);
    }
}