/**
 * Test error case:  needed fresh perm not available
 */
class FreshGroupPermError {
    class Data {}

    class Cell<refgroup G> {
	unique(G) Data data;
    }

    <refgroup G>void m1() fresh G {}

    void m2() {
	refgroup g;
	unique(g) Cell<g> cell = new Cell<g>();
	cell.data = new Data(); // this needs 'updates g'
	this.<refgroup g>m1();  // g is not fresh!
    }
}