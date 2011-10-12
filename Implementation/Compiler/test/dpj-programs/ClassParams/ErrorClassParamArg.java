/**
 * Test of incompatible class param args
 */
class ErrorClassParamArg {
    class Data<refgroup G> {}
    void m() {
	refgroup g1,g2;
	Data<g1> data = new Data<g2>();
    }
}