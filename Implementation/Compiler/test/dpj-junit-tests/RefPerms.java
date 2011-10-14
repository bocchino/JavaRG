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
    
    @Test public void testErrorInit() throws Throwable {
	compileExpectingErrors("ErrorInit", 1);
    }
    
    @Test public void testErrorFieldAccess() throws Throwable {
	compileExpectingErrors("ErrorFieldAccess", 1);
    }
    
    @Test public void testDestructiveRead() throws Throwable {
	compile("DestructiveRead");
    }
    
    @Test public void testErrorWrongGroup() throws Throwable {
	compileExpectingErrors("ErrorWrongGroup", 1);
    }
    
}
