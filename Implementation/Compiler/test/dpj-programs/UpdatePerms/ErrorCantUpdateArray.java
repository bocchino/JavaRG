/**
 * Test of error because there isn't permission to update an array cell
 */
class ErrorCantUpdateArray<refgroup G> {
    class Data{}
    arrayclass DataArray<refgroup G> { unique(G) Data; }
    void m(DataArray<G> array) preserves G {
	array[0] = new Data();
    }
}