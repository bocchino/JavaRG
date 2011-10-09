/**
 * Basic test of sequential 'for each' statement
 */

class ForeachBasicSeq extends Harness {
    
    IntArray array;

    @Override
    public void initialize() {
	array = new IntArray(size);
    }

    @Override
    public void runWork() {
	for each i in array {
	    array[i] = i;
	}
    }

    @Override
    public void runTest() {
	for (int i = 0; i < array.length; ++i)
	    assert(array[i] == i);
    }

    public ForeachBasicSeq(String[] args) {
	super("ForeachBasicSeq", args);
    }

    public static void main(String[] args) {
	ForeachBasicSeq test = new ForeachBasicSeq(args);
	test.run();
    }
}
