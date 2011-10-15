/**
 * Test of permissions with implicit 'this' in method call
 */
class ExplicitThis<refgroup G> {
    void m1() unique(G) {}
    void m2() unique(G) {
	m1();
    }
}