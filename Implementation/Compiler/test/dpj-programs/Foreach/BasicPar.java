/**
 * Basic test of parallel foreach
 */

arrayclass IntArray { int; }

class ForeachBasicPar {
    IntArray array;
    void m(int size) {
	array = new IntArray(size);
	for each i in array pardo {
	    array[i] = i;
	}
    }
}