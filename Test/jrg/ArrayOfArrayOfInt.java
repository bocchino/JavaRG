/**
* Test use of array of array of int
*/

class ArrayOfArrayOfInt<region R> extends Harness {
    IntIntArray<R> array;

    @Override
    public void initialize() {
	array = new IntIntArray<R>(size);
    }

    @Override
    public void runTest() {
	for (int i = 0; i < array.length; ++i)
	    for (int j = 0; j < array[i].length; ++j)
		assert (array[i][j] == i*j);
    }

    @Override
    public void runWork() {
        for (int i = 0; i < array.length; ++i) {
            array[i] = new IntArray<R>(size);
	    for (int j = 0; j < array[i].length; ++j) {
		array[i][j] = i*j;
	    }
	}
    }

    public ArrayOfArrayOfInt(String[] args) {
	super("ArrayOfArrayOfInt", args);
    }

    public static void main(String[] args) {
	region r;
	ArrayOfArrayOfInt<r> test = 
	    new ArrayOfArrayOfInt<r>(args);
	test.run();
    }
}
