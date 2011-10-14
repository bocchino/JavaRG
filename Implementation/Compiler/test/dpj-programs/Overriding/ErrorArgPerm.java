/**
 * Check overriding of argument permission
 */
class Data {}

class A {
    void m(Data data) {}
}

class B<refgroup G> extends A {
    void m(unique(G) Data data) {} // Error:  Bad argument permission
}
