/**
 * Test error case:  needed preserved perm not available
 */
class PreservedGroupPermError {
    <refgroup G>void m1() {}
    <refgroup G>void m2() preserves G{
	// G should be locked
	this.<refgroup G>m1();
    }
}