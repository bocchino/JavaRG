/**
 * Root of permission tree must be locally unique
 */
class CopyFromVar {
    class Data {}
    class Cell<refgroup G> { 
	unique(G) Data x;
    }
    void m() {
	refgroup g1, g2;
	// cell is not locally unique...
	Cell<g1> cell = new Cell<g1>();
	cell.x = new Data();
	// ...so this doesn't work
	unique(g2) Data y = cell.x;
    }
}