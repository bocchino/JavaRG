/**
* Test destructive field access
*/

class DestructiveFieldAccess<refgroup G> extends Harness {
    unique(G) Data data1;
    unique(G) Data data2;

    @Override
    public void initialize() updates G {
	this.data1 = new Data();
    }

    @Override
    public void runTest() {
	assert(data1 == null);
	assert(data2 instanceof Data);
    }

    @Override
    public void runWork() updates G {
	// Should read data1 into data2, then null out data1
	data2 = !this.data1;
    }

    public DestructiveFieldAccess(String[] args) {
	super("DestructiveFieldAccess", args);
    }

    public static void main(String[] args) {
	refgroup g;
	DestructiveFieldAccess<g> test = 
	    new DestructiveFieldAccess<g>(args);
	test.run();
    }
}
