/**
 * Test splitting of a copy perm from a fresh group perm
 */
class Data {}

class A<refgroup GA1,GA2> {
    void m(unique(GA1) Data xA) fresh GA2 {}
}

class B<refgroup GB1,GB2> extends A<GB1,GB2> {
    void m(unique(GB1) Data xB) copies xB...GB1 to GB2 {}
}