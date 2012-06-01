/**
 * Test error case:  duplicate method and type group args
 */
class ErrorDuplicateMethodAndTypeArgs<refgroup G1> {
    <refgroup G2>void m1() {}
    void m2() {
	// Inside m1, G1 would equal G2
	this.<refgroup G1>m1();
    }
}