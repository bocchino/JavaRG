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
	compile("ErrorPreservesUpdates", 1, 1);
    }
    
    @Test public void testWarnCoarsenUpdatedGroup() throws Throwable {
	compileExpectingWarnings("WarnCoarsenUpdatedGroup", 1);
    }
    
    @Test public void testErrorCantSwitch() throws Throwable {
	compileExpectingErrors("ErrorCantSwitch", 2);
    }
}
