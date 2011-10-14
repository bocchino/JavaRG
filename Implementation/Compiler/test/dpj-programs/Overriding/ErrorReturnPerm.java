/**
 * Check overriding of return permission
 */
class Data {}

class A<refgroup G> {
    unique(G) Data m() {}
}

class B<refgroup G> extends A<G> {
    Data m(); // Error:  Bad return permission
}
