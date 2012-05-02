/**
 * Test that default constructor generates default effect
 */
class Init {
    Init m() pure {
	// Should have effect writes Root:*
	return new Init();
    }
}