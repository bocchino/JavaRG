/**
 * Test of method copy permissions
 */
class CopyPerms<type T, refgroup G1,G2> {
    void m1(final T x) copies x to G2 {}
    void m2(final T x) copies x...G1 to G2 {}
}