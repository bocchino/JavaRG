/**
 * Test successful invocation of method that needs updated group perm
 */
class UpdatedGroupPermOK {
    <refgroup G>void m1() updates G {}
    void m2() {
	refgroup g;
	this.<refgroup g>m1();
    }
}