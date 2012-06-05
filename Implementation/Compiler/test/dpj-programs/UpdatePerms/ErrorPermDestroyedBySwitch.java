/**
 * Test of error: Perm destroyed by switch in method call
 */
abstract class ErrorPermDestroyedBySwitch {
    class Cell<refgroup G> {
	unique(G) Object d;
    }
    abstract <refgroup G>void m1() preserves G switches G;
    void m2() {
	refgroup g1, g2;
	unique(g1) Cell<g1> c1 = new Cell<g1>();
	unique(g2) Cell<g2> c2 = new Cell<g2>();
	// fresh g1 exists here
	this.<refgroup g1>m1();
	// now it doesn't
	c1.d = c2.d;	
    }
}