/**
 * Basic test of destructive field access
 */
class Data{}

class Basic<refgroup G> {
    unique(G) Data data;
    unique(G) Data m() {
	unique(G) Data temp = !this.data;
	return temp;
    }
}