/**
 * Test insufficient permission with implicit this
 */
class ErrorExplicitThis<refgroup G> {
    void m1() unique(G) {}
    void m2() {
	m1(); // Not enough permission for this
    }
}