/**
 * Test error condition:  insufficient permission for 'this'
 */
class ErrorSharedArgToThis<refgroup G> {
    void m1() unique(G) {}
    void m2() {
	ErrorSharedArgToThis esatt = new ErrorSharedArgToThis();
	esatt.m1(); // Not enough perm for this
    }
}