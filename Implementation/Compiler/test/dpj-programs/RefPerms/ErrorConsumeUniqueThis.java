/**
 * Test of error consuming unique(G) permission to 'this'
 */
class ErrorConsumeUniqueThis {
    <refgroup G>void m() unique(G) {
	unique(G) ErrorConsumeUniqueThis ecut1 = this;
	// Permission is gone!
	unique(G) ErrorConsumeUniqueThis ecut2 = this;
    }
}