/**
 * Test that copy perm is consumed on use
 */
class ErrorPermConsumed {
    class Data{}
    <refgroup G>void m(Data x) copies x to G {
	unique(G) Data y = x; // OK
	unique(G) Data z = x; // Should fail; we can only do it once
    }
}