/**
 * Test conflict:  Preserving in one branch of pardo, updating
 * in another.
 */
class ErrorPreservesUpdates {
    <refgroup G>void m1() preserves G {}
    <refgroup G>void m2() {} // updates G
    void conflict() {
	refgroup g;
	pardo {
	    this.<refgroup g>m1(); // preserves g
	    this.<refgroup g>m2(); // updates g
	}
    }
}