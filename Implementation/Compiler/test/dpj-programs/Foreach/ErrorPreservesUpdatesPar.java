/**
 * Test preserve/update conflict in parallel for loop
 */
class ErrorPreservesUpdatesPar {
    arrayclass IntArray { int; }
    IntArray array;
    <refgroup G>void m1() preserves G {}
    <refgroup G>void m2() {} // updates G
    void conflict(int size) {
	refgroup g;
	array = new IntArray(size);
	for each i in array pardo {
	    this.<refgroup g>m1(); // preserves g
	    this.<refgroup g>m2(); // updates g
	}
    }
}