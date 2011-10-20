/**
 * Error condition:  Superclass method preserves G, subclass
 * method doesn't.
 */
class A<refgroup GA> {
    void m() preserves GA {}
}

class B<refgroup GB> extends A<GB> {
    void m() {} // <-- Must preserve G
}