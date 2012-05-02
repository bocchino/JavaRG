/**
 * Test that compiler catches missing initializer effect in constructor
 */
abstract class Init {
    region r;
    // Initializer writes r
    abstract int doInit() writes r; 
    // Every constructor calls doInit()
    final int x = doInit();
    // Pure effect omits write to r
    public Init() pure {}
}