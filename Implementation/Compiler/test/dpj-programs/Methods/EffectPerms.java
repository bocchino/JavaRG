/**
 * Test of method effect permissions
 */
class EffectPerms<type T, refgroup G> {
    region r;
    void m1(final T x) reads r, r via x writes r, r via x {}
    void m2(final T x) reads r, r via x...G writes r, r via x...G {}
}