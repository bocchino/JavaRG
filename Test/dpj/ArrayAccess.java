/**
* Test array class access with class cell type
*/

class Data {}

arrayclass DataArray<region R> { 
    Data in R; 
}

class ArrayAccess<region R> extends Harness {
    DataArray<R> array;

    @Override
    public void initialize() {
	array = new DataArray<R>(size);
    }

    @Override
    public void runTest() {
	for (int i = 0; i < size; ++i) {
	    assert (array[i] != null);
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
	ArrayAccess<r> test = new ArrayAccess<r>(args);
	test.run();
    }
}
