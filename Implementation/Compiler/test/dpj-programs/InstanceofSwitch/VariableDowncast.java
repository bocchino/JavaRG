/**
 * Test variable downcast with instanceof switch
 */

class VariableDowncast {
    void m(A x) {
	switch(x) instanceof {
	    case B:
		x.b = 1;
	    case C:
		x.c = 1;
	}
    }
}

class A {}

class B extends A {
    int b;
}

class C extends B {
    int c;
}
