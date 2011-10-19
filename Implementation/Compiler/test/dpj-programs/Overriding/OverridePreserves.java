/**
 * Successful case of overriding preserves permission
 */
class A<refgroup G> {
    void m() preserves G {}
}

class B<refgroup G> extends A<G> {
    void m() preserves G {}
}