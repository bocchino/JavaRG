/**
 * Test that permission is available in each of if branches
 */

abstract class IfSeparateBranches {
    class Data{}
    abstract boolean test();
    <refgroup G>void m() {
	unique(G) Data data = new Data();
	if (test()) {
	    unique(G) Data data1 = data; // OK
	}
	else {
	    unique(G) Data data2 = data; // Also OK
	}
    }
}