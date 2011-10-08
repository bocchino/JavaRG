/**
* Basic test of code generation for 'pardo' statement
*/

class PardoBasic extends Harness {
    region r1, r2;
    int x1 in r1, x2 in r2;

    @Override
    public void initialize() {}

    @Override
    public void runWork() {
	pardo {
	    x1 = 1;
	    x2 = 2;
	}
    }

    @Override
    public void runTest() {
	assert (x1 == 1);
	assert (x2 == 2);
    }

    public PardoBasic(String[] args) {
	super("PardoBasic", args);
    }

    public static void main(String[] args) {
	PardoBasic test = new PardoBasic(args);
	test.run();
    }
}
