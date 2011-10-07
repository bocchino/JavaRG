/**
 * Tests for destructive field access
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class DestructiveFieldAccess extends DPJTestCase {
    
    public DestructiveFieldAccess() {
	super("DestructiveFieldAccess");
    }
    
    @Test public void testBasic() throws Throwable {
	compile("Basic");
    }
    
    
    @Test public void testErrorNoFieldAccess() throws Throwable {
	compileExpectingErrors("ErrorNoFieldAccess", 1);
    }
}
