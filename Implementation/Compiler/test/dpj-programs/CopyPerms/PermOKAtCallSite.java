/**
 * Test that we can find the copy permission at the call site
 */
abstract class PermOKAtCallSite {
    class Data{}
    abstract <refgroup G1>void m1(Data x) copies x to G1;
    <refgroup G2>void m2() {
	refgroup g;
	unique(G2) Data y = new Data();
	this.<refgroup g>m1(y); // OK because g is fresh
    }
}