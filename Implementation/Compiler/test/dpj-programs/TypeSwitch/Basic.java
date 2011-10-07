/**
 * Basic test of type switch syntax
 */

class Basic {
    String tag;
    void m(A x) {
	typeswitch(x) {
	    case A:
		tag = "A";
	    case B:
		tag = "B";
	    case C:
		tag = "C";
	    default:
		tag = "I don't know!";    
	}
    }
}

class A {}
class B extends A {}
class C extends B {}