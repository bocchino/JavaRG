/**
 * Test destructive read
 */
class DestructiveRead<refgroup G> {
    class Data {}
    unique(G) Data data;
    
    void m() {
	unique(G) Data data = !this.data;
    }
}