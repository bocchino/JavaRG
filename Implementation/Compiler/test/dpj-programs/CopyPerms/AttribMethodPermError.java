/**
 * Error test:  Bad attribution of method copy perm
 */
class AttribMethodPermError {
    class Data { 
	void m1() {} 
    }
    <refgroup G1,G2>void m(final Data x) 
    	copies x.m1()...G1 to G2 {}
}