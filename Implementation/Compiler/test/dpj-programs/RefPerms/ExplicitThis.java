/**
 * Test of permissions with explicit 'this' in method call
 */
class ExplicitThis<refgroup G> {
    void m1() unique(G) {}
    void m2() unique(G) {
	this.m1();
    }
}