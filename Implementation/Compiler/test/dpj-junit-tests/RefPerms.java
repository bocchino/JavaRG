/**
 * Tests for reference permissions
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class RefPerms extends DPJTestCase {
    
    public RefPerms() {
	super("RefPerms");
    }
    
    @Test public void testErrorAssignSharedToUnique() throws Throwable {
	compileExpectingErrors("ErrorAssignSharedToUnique", 1);
    }
    
    @Test public void testErrorConsumeUnique() throws Throwable {
	compileExpectingErrors("ErrorConsumeUnique", 1);
    }
    
}
