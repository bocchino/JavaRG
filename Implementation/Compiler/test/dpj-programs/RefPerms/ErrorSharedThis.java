/**
 * Test attempt to get unique(G) permission from shared 'this'
 */
class ErrorSharedThis {
    <refgroup G>void m() {
	// Not enough permission in 'this'
	unique(G) ErrorSharedThis est = this; 
    }
}