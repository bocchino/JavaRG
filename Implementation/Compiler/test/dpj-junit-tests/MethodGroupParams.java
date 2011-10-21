/**
 * Tests for method group parameters
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class MethodGroupParams extends DPJTestCase {
    
    public MethodGroupParams() {
	super("MethodGroupParams");
    }
    
    @Test public void testAssignReturnType() throws Throwable {
	compile("AssignReturnType");
    }
   
    @Test public void testErrorAssignReturnType() throws Throwable {
	compileExpectingErrors("ErrorAssignReturnType", 1);
    }
    
    @Test public void testErrorDuplicateArgs() throws Throwable {
	compileExpectingErrors("ErrorDuplicateArgs", 1);
    }
}
