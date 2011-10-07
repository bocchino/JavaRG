/**
 * Test that the compiler catches the error of writing !e where
 * e is not e'.f (nor is it a boolean expression)
 */

class Data {}

class ErrorNoFieldAccess<refgroup G> {
    void m() {
	unique(G) Data x = new Data();
	unique(G) Data y = !x; // Error:  !x is not a field access
    }
}