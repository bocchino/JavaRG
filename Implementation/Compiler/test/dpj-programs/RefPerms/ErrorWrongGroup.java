/**
 * Test that compiler catches attempt to assign to wrong group
 * (without copy permission)
 */
class ErrorWrongGroup {
    class Data{}
    <refgroup G>unique(G) Data m1() { return new Data(); }
    void m2() {
	refgroup g1, g2;
	unique(g1) Data x = this.<refgroup g2>m1(); // Mismatch
    }
}