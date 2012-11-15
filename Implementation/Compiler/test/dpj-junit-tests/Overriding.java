/**
 * Tests for method overriding checks
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class Overriding extends DPJTestCase {
    
    public Overriding() {
	super("Overriding");
    }
    
    @Test public void testErrorReturnPerm() throws Throwable {
	compileExpectingErrors("ErrorReturnPerm", 1);
    }
 
    @Test public void testErrorArgPerm() throws Throwable {
	compileExpectingErrors("ErrorArgPerm", 1);
    }
    
    @Test public void testErrorThisPerm() throws Throwable {
	compileExpectingErrors("ErrorThisPerm", 1);
    }
    
    @Test public void testOverridePreserves() throws Throwable {
	compile("OverridePreserves");
    }
    
    @Test public void testErrorMissingPreserves() throws Throwable {
	compileExpectingWarnings("ErrorMissingPreserves", 1);
    }
    
    @Test public void testCopyPerm() throws Throwable {
	compile("CopyPerm");
    }
    
    @Test public void testSplitCopyPermsThroughFields() throws Throwable {
	compile("SplitCopyPermsThroughFields");
    }
    
    @Test public void testErrorCopyPermFromNothing() throws Throwable {
	compileExpectingWarnings("ErrorCopyPermFromNothing", 2);
    }
    

}
