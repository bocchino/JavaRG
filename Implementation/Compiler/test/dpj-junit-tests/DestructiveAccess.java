/**
 * Tests for destructive field access
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;

public class DestructiveAccess extends DPJTestCase {
    
    public DestructiveAccess() {
	super("DestructiveAccess");
    }
    
    @Test public void testField() throws Throwable {
	compile("Field");
    }
    
    @Test public void testArray() throws Throwable {
	compile("Array");
    }
    
    @Test public void testErrorNoAccess() throws Throwable {
	compileExpectingErrors("ErrorNoAccess", 1);
    }
    
    @Test public void testErrorCantUpdate() throws Throwable {
	compileExpectingWarnings("ErrorCantUpdate", 1);
    }
    
}
