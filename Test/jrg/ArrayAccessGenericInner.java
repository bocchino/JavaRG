/**
 * Test array class access with generic cell type 
 * and inner class
 */

class ArrayAccessGenericInner<region R, refgroup G> extends Harness {

    static arrayclass GenericArray<type T, region R, refgroup G> { 
	unique(G) T in R; 
    }

    GenericArray<Data,R,G> array;

    @Override
    public void initialize() {
	array = new GenericArray<Data,R,G>(size);
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

    public ArrayAccessGenericInner(String[] args) {
	super("ArrayAccessGenericInner", args);
    }

    public static void main(String[] args) {
	region r;
	refgroup g;
	ArrayAccessGenericInner<r,g> test = 
	    new ArrayAccessGenericInner<r,g>(args);
	test.run();
    }
}
