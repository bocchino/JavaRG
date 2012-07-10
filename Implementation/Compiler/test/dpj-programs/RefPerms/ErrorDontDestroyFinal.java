/**
 * Test error: Don't destroy final field
 */
class ErrorUniqueNotFinal {
    class Data {}
    final unique Data data = new Data();
    unique Data m() {
	return !this.data; // Not allowed!
    }
}