/**
 * Test insufficient permission with explicit this
 */
class ErrorExplicitThis<refgroup G> {
    void m1() unique(G) {}
    void m2() {
	this.m1(); // Not enough permission for this
    }
}