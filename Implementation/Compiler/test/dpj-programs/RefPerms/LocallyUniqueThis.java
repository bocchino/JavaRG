/**
 * Test of locally unique permission to 'this'
 */
class LocallyUniqueThis {
    <refgroup G>void m() unique(G) {
	unique(G) LocallyUniqueThis lut = this;
    }
}