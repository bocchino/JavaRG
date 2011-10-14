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
}
