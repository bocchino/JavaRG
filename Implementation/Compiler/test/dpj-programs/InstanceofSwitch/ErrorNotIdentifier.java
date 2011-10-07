/**
 * Test error in instanceof switch:  selector not an identifier
 */

class A {}
class B extends A {}

class ErrorNotIdentifier {
    String tag;
    void m1() {
	switch(m2()) instanceof {
	case A:
	    tag = "A";
	case B:
	    tag = "B";
	}
    }
    A m2() { return new B(); }
}