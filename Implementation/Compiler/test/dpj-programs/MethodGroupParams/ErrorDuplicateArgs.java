/**
 * Test error case:  duplicate method group args
 */
class ErrorDuplicateArgs {
    <refgroup G1,G2>void m1() {}
    void m2() {
	refgroup g;
	this.<refgroup g,g>m1();
    }
}