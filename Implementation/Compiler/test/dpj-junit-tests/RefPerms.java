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
    
    @Test public void testErrorArgumentMismatch() throws Throwable {
	compileExpectingErrors("ErrorArgumentMismatch", 1);
    }
    
    @Test public void testErrorConsumeArgument() throws Throwable {
	compileExpectingErrors("ErrorConsumeArgument", 1);
    }
    
    @Test public void testMethodArgMethodParam() throws Throwable {
	compile("MethodArgMethodParam");
    }
    
    @Test public void testMethodArgClassParam() throws Throwable {
	compile("MethodArgClassParam");
    }
    
    @Test public void testConstructorArg() throws Throwable {
	compile("ConstructorArg");
    }
    
}
