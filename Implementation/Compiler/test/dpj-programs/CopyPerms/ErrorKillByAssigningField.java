/**
 * Test that assigning field in group G kills copy perms
 * with G as source group
 */
abstract class ErrorKillByAssigningField {
    class Data{}
    class Cell<refgroup G> { unique(G) Data data; }
    abstract <refgroup G1,G2>void m1(Cell<G1> x)
	copies x to G2 preserves G1,G2;
    abstract <refgroup G1,G2>void m2(Cell<G1> x) 
    	copies x.data...G1 to G2;
    void OK() {
	refgroup g1, g2;
	unique(g1) Cell<g1> cell = new Cell<g1>();
	// Should create 'copies cell.?...g1 to g2'
	this.<refgroup g1,g2>m1(cell);
	// Should be OK
	this.<refgroup g1,g2>m2(cell);
    }
    void Error() {
	refgroup g1, g2;
	unique(g1) Cell<g1> cell = new Cell<g1>();
	// Should create 'copies cell.?...g1 to g2'
	this.<refgroup g1,g2>m1(cell);
	// Assignment into g1 should kill it
	cell.data = new Data();
	// Should fail
	this.<refgroup g1,g2>m2(cell);	
    }
}