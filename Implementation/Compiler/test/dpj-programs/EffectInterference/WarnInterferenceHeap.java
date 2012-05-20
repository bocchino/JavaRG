/**
 * Straight-up warning for interfering heap effects
 * @author bocchino
 *
 */

class WarnInteferenceHeap {
    region r;
    int x in r;
    void m() {
	pardo {
	    // Should warn about interference
	    x = 0;
	    x = 1;
	}
    }
}