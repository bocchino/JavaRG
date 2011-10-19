/**
 * Test of error trying to destroy field without update perm
 */
class Data{}

class ErrorCantUpdate<refgroup G> {
    unique(G) Data data;
    void m() preserves G {
	unique(G) Data data = !this.data; // Should fail
    }
}