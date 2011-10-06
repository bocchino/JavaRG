/**
 * Test of group name bound to method param
 */

class Data {}

class MethodParamArg {
    <refgroup G>unique(G) Data m1() {
	return new Data();
    }
    void m2() {
	refgroup g;
	unique(g) Data data = this.<refgroup g>m1();
    }
}