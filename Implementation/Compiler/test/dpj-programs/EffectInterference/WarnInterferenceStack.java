/**
 * Straight-up warning for interfering stack effects
 * @author bocchino
 *
 */

class WarnInteferenceHeap {
    void m() {
	int x;
	pardo {
	    // Should warn about interference
	    x = 0;
	    x = 1;
	}
    }
}