/**
* Test array class access with class cell type
*/

class ArrayAccess<region R, refgroup G> extends Harness {
    DataArray<R,G> array;

    @Override
    public void initialize() {
	array = new DataArray<R,G>(size);
    }

    @Override
    public void runTest() {
	for (int i = 0; i < size; ++i) {
	    assert (array[i] instanceof Data);
	}
    }

    @Override
    public void runWork() {
	for (int i = 0; i < size; ++i) {
	    array[i] = new Data();
	}
    }

    public ArrayAccess(String[] args) {
	super("ArrayAccess", args);
    }

    public static void main(String[] args) {
	region r;
	refgroup g;
	ArrayAccess<r,g> test = new ArrayAccess<r,g>(args);
	test.run();
    }
}
