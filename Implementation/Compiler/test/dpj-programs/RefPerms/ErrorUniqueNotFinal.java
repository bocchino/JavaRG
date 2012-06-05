/**
 * Test error: Unique field must be final
 */
class ErrorUniqueNotFinal {
    final unique Object f1; // OK
    	  unique Object f2; // Error
}