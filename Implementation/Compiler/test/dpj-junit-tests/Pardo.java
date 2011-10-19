/**
 * Tests for pardo  statements
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class Pardo extends DPJTestCase {
    
    public Pardo() {
	super("Pardo");
    }
    
    @Test public void testBasic() throws Throwable {
	compile("Basic");
    }
    
    @Test public void testErrorPreservesUpdates() throws Throwable {
	compileExpectingErrors("ErrorPreservesUpdates", 1);
    }
    
}
