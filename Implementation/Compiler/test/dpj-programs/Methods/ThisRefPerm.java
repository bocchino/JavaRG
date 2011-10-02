/**
 * Test of reference permission for 'this'
 */
class ThisRefPerm<refgroup G> {
    void m() unique(G) {}
}