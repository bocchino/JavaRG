/**
 * Test that compiler correctly consumes unique(G) on assignment
 */
class ErrorConsumeUnique {
    class Data {}
    
    <refgroup G>void m() {
	unique(G) Data x = new Data();
	unique(G) Data y = null, z = null;
	y = x;
	z = x; // Should cause error
    }
}