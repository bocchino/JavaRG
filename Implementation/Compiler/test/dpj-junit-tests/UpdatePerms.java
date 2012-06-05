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
	compileExpectingErrors("ErrorCantUpdateField", 1);
    }

    @Test public void testErrorCantUpdateArray() throws Throwable {
	compileExpectingErrors("ErrorCantUpdateArray", 1);
    }

    @Test public void testErrorPermDestroyedBySwitch() throws Throwable {
	compileExpectingErrors("ErrorPermDestroyedBySwitch", 1);
    }    
    
}
