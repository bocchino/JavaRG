/**
* Test array class access with generic cell type
*/
arrayclass Array<type T, region R> { 
    T in R; 
}

class ArrayAccessGeneric<region R> extends Harness {

    Array<Data,R> array;

    @Override
    public void initialize() {
	array = new Array<Data,R>(size);
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

    public ArrayAccessGeneric(String[] args) {
	super("ArrayAccessGeneric", args);
    }

    public static void main(String[] args) {
	region r;
	ArrayAccessGeneric<r> test = new ArrayAccessGeneric<r>(args);
	test.run();
    }
}
