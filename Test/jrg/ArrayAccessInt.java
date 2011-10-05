/**
* Test array class access with 'int' cell type
*/

arrayclass IntArray { int; }

class ArrayAccessInt extends Harness {
    IntArray array;

    @Override
    public void initialize() {
	array = new IntArray(size);
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
	ArrayAccessInt test = new ArrayAccessInt(args);
	test.run();
    }
}
