/**
* Test instanceof switch
*/

class InstanceofSwitch extends Harness {
    class A {}
    class B extends A {
	int bField;
    }
    class C extends B {
	int cField;
    }

    A mainField;

    @Override
    public void initialize() {
	mainField = new C();
    }

    @Override
    public void runWork() {
	A tmp = mainField;
	switch(tmp) instanceof {
	    case B:
		tmp.bField = 1;
	    case C:
		tmp.cField = 1;
	    }
    }

    @Override
    public void runTest() {
	C tmp = (C) mainField;
	assert tmp.bField == 1;
	assert tmp.cField == 0;
    }

    public InstanceofSwitch(String[] args) {
	super("InstanceofSwitch", args);
    }

    public static void main(String[] args) {
	InstanceofSwitch test = new InstanceofSwitch(args);
	test.run();
    }
}
