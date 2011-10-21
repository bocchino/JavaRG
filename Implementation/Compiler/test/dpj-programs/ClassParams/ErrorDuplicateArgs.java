/**
 * Test error case:  duplicate class group args
 */
class ErrorDuplicateArgs<refgroup G1,G2> {
    ErrorDuplicateArgs<G1,G1> field;
    void m() {
	refgroup g;
	ErrorDuplicateArgs<g,g> eda = null;    }
}