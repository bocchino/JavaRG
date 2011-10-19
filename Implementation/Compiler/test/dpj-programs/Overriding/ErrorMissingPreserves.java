/**
 * Error condition:  Superclass method preserves G, subclass
 * method doesn't.
 */
class A<refgroup G> {
    void m() preserves G {}
}

class B<refgroup G> extends A<G> {
    void m() {} // <-- Must preserve G
}