/**
 * Test successful invocation of method that needs preserved group perm
 */
class PreservedGroupPermOK {
    <refgroup G>void m1() preserves G {}
    void m2() {
	refgroup g;
	this.<refgroup g>m1();
    }
}