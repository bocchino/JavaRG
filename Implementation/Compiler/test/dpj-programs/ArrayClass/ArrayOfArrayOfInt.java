/**
 * Test array of array of int
 */

arrayclass IntArray { int; }
arrayclass IntIntArray { IntArray; }

class ArrayOfArrayOfInt {
    IntIntArray create(int size) {
        IntIntArray a = new IntIntArray(size);
        for (int i = 0; i < a.length; ++i)
            a[i] = new IntArray(size);
        return a;
    }
    IntArray index1D(IntIntArray a, int i) {
	return a[i];
    }
    int index2D(IntIntArray a, int i, int j) {
	return a[i][j];
    }

}