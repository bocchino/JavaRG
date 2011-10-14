/**
 * Test that compiler requires ! for reading unique out of array
 */
class ErrorNonDestructive<refgroup G> {
    class Data{}
    arrayclass DataArray<refgroup G> { unique(G) Data; }

    unique(G) Data m(DataArray<G> a) {
	return a[0]; // Should be !a[0]
    }
}