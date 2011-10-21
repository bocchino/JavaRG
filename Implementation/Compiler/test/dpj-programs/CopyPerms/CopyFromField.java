/**
 * Use a copy permission to copy from a field
 */
class CopyFromField {
    class Data {}
    class Cell<refgroup G> { 
	unique(G) Data x;
    }
    void m() {
	refgroup g1, g2;
	unique(g1) Cell<g1> cell = new Cell<g1>();
	cell.x = new Data();
	// Permission is copied to g2
	unique(g2) Data y = cell.x;
	// Permission in g1 remains
	unique(g1) Data z = !cell.x;
    }
}