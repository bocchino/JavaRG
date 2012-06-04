/**
 * Test error:  Attempt to switch inside pardo
 */
abstract class ErrorCantSwitch {
    void m1() {
	refgroup g;
	this.<refgroup g>m1();
    }
    <refgroup G>void m2() {
	pardo {
	    this.<refgroup G>m3(); // can't switch g
	    this.<refgroup G>m4(); // OK
	    this.<refgroup G>m5(); // can't switch
	}	
    }
    abstract <refgroup G>void m3() pure switches G;
    abstract <refgroup G>void m4() pure;
    abstract <refgroup G>void m5() pure preserves G;
}