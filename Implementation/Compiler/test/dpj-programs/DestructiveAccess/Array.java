/**
 * Basic test of destructive array access
 */
class Data{}
arrayclass DataArray<refgroup G> { unique(G) Data; }

class Basic<refgroup G> {
    DataArray<G> array;
    unique(G) Data m() {
	unique(G) Data temp = !array[0];
	return temp;
    }
}