/**
* Test destructive field access
*/

class DestructiveArrayAccess<refgroup G> extends Harness {

    DataArray<Root,G> array = new DataArray<Root,G>(2);

    @Override
    public void initialize() {
	this.array[0] = new Data();
    }

    @Override
    public void runTest() {
	assert(array[0] == null);
	assert(array[1] instanceof Data);
    }

    @Override
    public void runWork() {
	// Should read data1 into data2, then null out data1
	array[1] = !array[0];
    }

    public DestructiveArrayAccess(String[] args) {
	super("DestructiveArrayAccess", args);
    }

    public static void main(String[] args) {
	refgroup g;
	DestructiveArrayAccess<g> test = 
	    new DestructiveArrayAccess<g>(args);
	test.run();
    }
}
