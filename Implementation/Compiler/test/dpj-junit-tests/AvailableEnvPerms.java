/**
 * Test availability of environment perms at method invocation
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class AvailableEnvPerms extends DPJTestCase {
    
    public AvailableEnvPerms() {
	super("AvailableEnvPerms");
    }
 
    @Test public void testFreshGroupPermOK() throws Throwable {
	compile("FreshGroupPermOK");
    }

    @Test public void testFreshGroupPermError() throws Throwable {
	compileExpectingErrors("FreshGroupPermError", 1);
    }
    
    @Test public void testPreservedGroupPermOK() throws Throwable {
	compile("PreservedGroupPermOK");
    }
    
    @Test public void testPreservedGroupPermError() throws Throwable {
	compileExpectingErrors("PreservedGroupPermError", 1);
    }
    
}
