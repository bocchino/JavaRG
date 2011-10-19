/**
* Test array class access with generic cell type
*/

class ArrayAccessGeneric extends Harness {

    @Override
    public void initialize() {
    }

    @Override
    public void runTest() {
    }

    @Override
    public void runWork() {
	region r;
	refgroup g;
	this.<region r; refgroup g>localWork();
    }

    private <region R, refgroup G>void localWork() {
	GenericArray<Data,R,G> array;
	array = new GenericArray<Data,R,G>(size);
	for (int i = 0; i < size; ++i) {
	    array[i] = new Data();
	}
	for (int i = 0; i < size; ++i) {
	    assert (array[i] instanceof Data);
	}
    }


    public ArrayAccessGeneric(String[] args) {
	super("ArrayAccessGeneric", args);
    }

    public static void main(String[] args) {
	ArrayAccessGeneric test = 
	    new ArrayAccessGeneric(args);
	test.run();
    }
}
