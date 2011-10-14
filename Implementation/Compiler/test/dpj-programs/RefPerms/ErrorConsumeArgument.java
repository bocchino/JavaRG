/**
 * Test that compiler correctly consumes unique(G) on binding
 * to method formal parameter
 */
class ErrorConsumeArgument {
    class Data {}
    
    <refgroup G>void m1(unique(G) Data data) {}
    
    void m2() {
	refgroup g;
	unique(g) Data data = new Data();
	this.<refgroup g>m1(data);
	unique(g) Data data1 = data; // Only shared left in data
    }
}