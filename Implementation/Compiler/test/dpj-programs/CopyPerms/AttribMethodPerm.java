/**
 * Basic test of copy permission in method
 */
class AttribMethodPerm {
    class Data {}
    <refgroup G1,G2>void m(final Data x) 
    	copies x...G1 to G2 {}
}