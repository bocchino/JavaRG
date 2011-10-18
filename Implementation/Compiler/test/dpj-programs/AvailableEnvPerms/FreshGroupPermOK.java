/**
 * Test successful invocation of method that needs fresh group perm
 */
class FreshGroupPermOK {
    <refgroup G>void m1() fresh G {}
    void m2() {
	refgroup g;
	this.<refgroup g>m1();
    }
}