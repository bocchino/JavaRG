/**
 * Test error case:  needed updates perm not available
 */
class UpdatedGroupPermError {
    <refgroup G>void m1() updates G {}
    <refgroup G>void m2() preserves G{
	// G should be locked
	this.<refgroup G>m1();
    }
}