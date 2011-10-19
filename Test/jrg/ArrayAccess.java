/**
* Test array class access with class cell type
*/

class ArrayAccess extends Harness {

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

    private <region R, refgroup G>void localWork() updates G {
	DataArray<R,G> array = new DataArray<R,G>(size);
	for (int i = 0; i < size; ++i) {
	    array[i] = new Data();
	}
	for (int i = 0; i < size; ++i) {
	    assert (array[i] instanceof Data);
	}
    }

    public ArrayAccess(String[] args) {
	super("ArrayAccess", args);
    }

    public static void main(String[] args) {
	ArrayAccess test = new ArrayAccess(args);
	test.run();
    }
}
