/**
 * Test mismatch in permission between method arg and formal param
 */
class ErrorArgumentMismatch {
    class Data{}
    <refgroup G>void m1(unique(G) Data data) {}
    void m2() {
	refgroup g1, g2;
	unique(g1) Data data = new Data();
	this.<refgroup g2>m1(data); // Mismatch, g1 =/= g2
    }
}