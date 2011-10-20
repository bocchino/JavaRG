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
    
}
