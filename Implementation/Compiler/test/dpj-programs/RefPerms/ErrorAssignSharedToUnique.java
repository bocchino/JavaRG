/**
 * Test that the compiler prohibits assigning shared to unique(G)
 */
class ErrorAssignSharedToUnique {
    class Data {}
    
    <refgroup G>void m() {
	Data x = new Data();
	unique(G) y = x; // Should cause error
    }
}