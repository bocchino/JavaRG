/**
 * Tests for update permissions
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class UpdatePerms extends DPJTestCase {
    
    public UpdatePerms() {
	super("UpdatePerms");
    }
    
    @Test public void testErrorCantUpdateField() throws Throwable {
	compileExpectingWarnings("ErrorCantUpdateField", 1);
    }

    @Test public void testErrorCantUpdateArray() throws Throwable {
	compileExpectingWarnings("ErrorCantUpdateArray", 1);
    }

    @Test public void testErrorPermDestroyedBySwitch() throws Throwable {
	compileExpectingWarnings("ErrorPermDestroyedBySwitch", 1);
    }    
    
}
