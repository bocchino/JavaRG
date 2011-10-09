/**
 * Basic test of sequential foreach
 */

arrayclass IntArray { int; }

class ForeachBasicSeq {
    IntArray array;
    void m(int size) {
	array = new IntArray(size);
	for each i in array {
	    array[i] = i;
	}
    }
    
}