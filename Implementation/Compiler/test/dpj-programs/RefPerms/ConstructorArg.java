/**
 * Test checking of permissions at constructor arg binding
 */
class ConstructorArg<refgroup G> {
    static class Data{}
    ConstructorArg(unique(G) Data data) {}
    void m() {
	refgroup g;
	unique(g) Data data = new Data();
	ConstructorArg<g> ca = new ConstructorArg<g>(data);
    }
}
