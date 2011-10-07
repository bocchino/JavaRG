/**
 * Test variable downcast with type switch
 */

class VariableDowncast {
    void m(A x) {
	typeswitch(x) {
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
