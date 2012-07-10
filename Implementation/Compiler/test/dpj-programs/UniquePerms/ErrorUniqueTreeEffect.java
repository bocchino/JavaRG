/**
 * Check for invalid effect e.f...G, where f is unique
 */
abstract class ErrorUniqueTreeEffect<refgroup G> {
    class Data { int x in Root; }
    unique Data f;
    // Effect is not valid
    abstract void m()
    	writes Root via this.f...G;
}