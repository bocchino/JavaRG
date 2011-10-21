/**
 * Use a copy permission to copy from a variable
 */
class CopyFromVar {
    class Data {}
    void m() {
	refgroup g1, g2;
	unique(g1) Data x = new Data();
	// Permission is copied to g2
	unique(g2) Data y = x;
	// Permission in g1 remains
	unique(g1) Data z = x;
    }
}