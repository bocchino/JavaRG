/**
 * Test that you can't pull unique(G) out of a field without !
 */
class ErrorFieldAccess<refgroup G> {

    class Data {}
    
    unique(G) Data data;
    
    void m() {
	// Should be ... = !this.data
	unique(G) data = this.data;
    }
}
