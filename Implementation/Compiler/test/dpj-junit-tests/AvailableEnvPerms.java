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
}
