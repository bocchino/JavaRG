/**
 * Test array class access with 'int' cell type
 */

arrayclass IntArray { int; }

class ArrayCreationInt {
    IntArray m() {
        IntArray a = new IntArray(10);
        for (int i = 0; i < a.length; ++i)
            a[i] = i;
        return a;
    }
}
