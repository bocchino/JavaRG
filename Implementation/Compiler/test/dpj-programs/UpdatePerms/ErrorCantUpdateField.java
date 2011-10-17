/**
 * Test of error because there isn't permission to update a field
 */
class ErrorCantUpdateField<refgroup G> {
    class Data{}
    unique(G) Data field;
    void m() {
	this.field = new Data();
    }
}