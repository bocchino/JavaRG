/**
 * Test error case:  needed preserved perm not available
 */
class PreservedGroupPermError {
    <refgroup G>void m1() preserves G {}
    <refgroup G>void m2() {
	// G should be locked
	this.<refgroup G>m1();
    }
}