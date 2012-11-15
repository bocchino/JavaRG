/**
 * Tests for copy permissions
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class CopyPerms extends DPJTestCase {
    
    public CopyPerms() {
	super("CopyPerms");
    }
    
    @Test public void testAttribMethodPerm() throws Throwable {
	compile("AttribMethodPerm");
    }
    
    @Test public void testAttribMethodPermError() throws Throwable {
	compileExpectingErrors("AttribMethodPermError", 1);
    }
    
    @Test public void testCopyFromVar() throws Throwable {
	compile("CopyFromVar");
    }
    
    @Test public void testCopyFromField() throws Throwable {
	compile("CopyFromField");
    }
    
    @Test public void testErrorRootNotLocallyUnique() throws Throwable {
	compileExpectingWarnings("ErrorRootNotLocallyUnique", 1);
    }
    
    @Test public void testErrorMissingPermAtCallSite() throws Throwable {
	compileExpectingWarnings("ErrorMissingPermAtCallSite", 1);
    }
    
    @Test public void testPermOKAtCallSite() throws Throwable {
	compile("PermOKAtCallSite");
    }
    
    @Test public void testErrorKillByAssigningVar() throws Throwable {
	compileExpectingWarnings("ErrorKillByAssigningVar", 1);
    }
    
    @Test public void testErrorPermConsumed() throws Throwable {
	compileExpectingWarnings("ErrorPermConsumed", 1);
    }
    
    @Test public void testErrorKillByAssigningField() throws Throwable {
	compileExpectingWarnings("ErrorKillByAssigningField", 1);
    }

}
