/**
 * Test array class creation with 'int' cell type
 */

arrayclass IntArray { int; }

class ArrayCreationInt {
    int m1(IntArray a) { return a[0]; }
    int m2() {
        IntArray a = new IntArray(10);
        return m1(a);
    }
}
