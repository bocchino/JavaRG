/**
 * Test mismatch in permission between method arg and formal param
 */
class ErrorArgumentMismatch {
    class Data{}
    <refgroup G>void m1(unique(G) Data data) {}
    <refgroup G1,G2>void m2() {
	unique(G1) Data data = new Data();
	this.<refgroup G2>m1(data); // Mismatch, G1 =/= G2
    }
}