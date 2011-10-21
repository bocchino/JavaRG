/**
 * Error condition:  Needed copy perm isn't there at call site
 */
abstract class ErrorMissingPermAtCallSite {
    class Data{}
    abstract <refgroup G1>void m1(Data x) copies x to G1;
    <refgroup G2,G3>void m2() {
	unique(G2) Data y = new Data();
	this.<refgroup G3>m1(y); // Should fail because G3 is not fresh
    }
}