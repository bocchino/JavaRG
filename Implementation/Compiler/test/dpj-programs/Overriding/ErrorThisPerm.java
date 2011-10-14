/**
 * Check overriding of this permission
 */
class A<refgroup G> {
    void m() {}
}

class B<refgroup G> extends A<G> {
    void m() unique(G) {} // Error:  Bad this permission
}
