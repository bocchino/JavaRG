/**
 * Test of error: Can't switch and preserve in method perms
 */
interface ErrorCantSwitchAndPreserve<refgroup G> {
    void m1() switches G; // OK
    void m2() preserves G switches G; // Error
}