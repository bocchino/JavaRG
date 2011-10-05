/**
* Test array class access with 'int' cell type
*/

class ArrayAccessInt<region R> extends Harness {
    IntArray<R> array;

    @Override
    public void initialize() {
	array = new IntArray<R>(size);
    }

    @Override
    public void runTest() {
	for (int i = 0; i < size; ++i) {
	    assert (array[i] == i);
	}
    }

    @Override
    public void runWork() {
	for (int i = 0; i < size; ++i) {
	    array[i] = i;
	}
    }

    public ArrayAccessInt(String[] args) {
	super("ArrayAccessInt", args);
    }

    public static void main(String[] args) {
	region r;
	ArrayAccessInt<r> test = new ArrayAccessInt<r>(args);
	test.run();
    }
}
