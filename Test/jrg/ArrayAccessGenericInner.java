/**
 * Test array class access with generic cell type 
 * and inner class
 */

class ArrayAccessGenericInner extends Harness {

    static arrayclass GenericArray<type T, region R, refgroup G> { 
	unique(G) T in R; 
    }

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
	GenericArray<Data,R,G> array = new GenericArray<Data,R,G>(size);
	for (int i = 0; i < size; ++i) {
	    array[i] = new Data();
	}
	for (int i = 0; i < size; ++i) {
	    assert (array[i] instanceof Data);
	}
    }

    public ArrayAccessGenericInner(String[] args) {
	super("ArrayAccessGenericInner", args);
    }

    public static void main(String[] args) {
	ArrayAccessGenericInner test = 
	    new ArrayAccessGenericInner(args);
	test.run();
    }
}
