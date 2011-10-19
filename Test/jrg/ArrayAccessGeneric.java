/**
* Test array class access with generic cell type
*/

class ArrayAccessGeneric<region R, refgroup G> extends Harness {

    GenericArray<Data,R,G> array;

    @Override
    public void initialize() {
	array = new GenericArray<Data,R,G>(size);
    }

    @Override
    public void runWork() {
	for (int i = 0; i < size; ++i) {
	    array[i] = new Data();
	}
    }

    @Override
    public void runTest() {
	for (int i = 0; i < size; ++i) {
	    assert (array[i] instanceof Data);
	}
    }

    public ArrayAccessGeneric(String[] args) {
	super("ArrayAccessGeneric", args);
    }

    public static void main(String[] args) {
	region r;
	refgroup g;
	ArrayAccessGeneric<r,g> test = 
	    new ArrayAccessGeneric<r,g>(args);
	test.run();
    }
}
