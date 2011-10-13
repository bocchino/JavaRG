/**
 * Test that the compiler consumes unique(G) on initialization
 */
class ErrorInit {
    class Data {}
    
    <refgroup G>void m() {
	unique(G) Data x = new Data();
	unique(G) Data y = x;
	unique(G) Data z = x; // Should cause error
    }
}