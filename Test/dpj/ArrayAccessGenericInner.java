/**
 * Test array class access with generic cell type 
 * and inner class
 */

class ArrayAccessGenericInner<region R> extends Harness {

    static arrayclass Array<type T, region R> { 
	T in R; 
    }

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

    public ArrayAccessGenericInner(String[] args) {
	super("ArrayAccessGenericInner", args);
    }

    public static void main(String[] args) {
	region r;
	ArrayAccessGenericInner<r> test = new ArrayAccessGenericInner<r>(args);
	test.run();
    }
}
