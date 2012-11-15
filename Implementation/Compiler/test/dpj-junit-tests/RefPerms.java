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
	compileExpectingWarnings("ErrorAssignSharedToUnique", 1);
    }
    
    @Test public void testErrorConsumeUnique() throws Throwable {
	compileExpectingWarnings("ErrorConsumeUnique", 1);
    }
    
    @Test public void testErrorInit() throws Throwable {
	compileExpectingWarnings("ErrorInit", 1);
    }
    
    @Test public void testErrorFieldAccess() throws Throwable {
	compileExpectingWarnings("ErrorFieldAccess", 1);
    }
    
    @Test public void testDestructiveRead() throws Throwable {
	compile("DestructiveRead");
    }
    
    @Test public void testErrorWrongGroup() throws Throwable {
	compileExpectingWarnings("ErrorWrongGroup", 1);
    }
    
    @Test public void testErrorArgumentMismatch() throws Throwable {
	compileExpectingWarnings("ErrorArgumentMismatch", 1);
    }
    
    @Test public void testErrorConsumeArgument() throws Throwable {
	compileExpectingWarnings("ErrorConsumeArgument", 1);
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
    
    @Test public void testErrorSharedThis() throws Throwable {
	compileExpectingWarnings("ErrorSharedThis", 1);
    }
    
    @Test public void testLocallyUniqueThis() throws Throwable {
	compile("LocallyUniqueThis");
    }
    
    @Test public void testErrorConsumeUniqueThis() throws Throwable {
	compileExpectingWarnings("ErrorConsumeUniqueThis", 1);
    }
    
    @Test public void testExplicitThis() throws Throwable {
	compile("ExplicitThis");
    }
    
    @Test public void testImplicitThis() throws Throwable {
	compile("ImplicitThis");
    }
    
    @Test public void testErrorExplicitThis() throws Throwable {
	compileExpectingWarnings("ErrorExplicitThis", 1);
    }
    
    @Test public void testErrorImplicitThis() throws Throwable {
	compileExpectingWarnings("ErrorImplicitThis", 1);
    }
    
    @Test public void testErrorSharedArgToThis() throws Throwable {
	compileExpectingWarnings("ErrorSharedArgToThis", 1);
    }
 
    @Test public void testErrorDontDestroyFinal() throws Throwable {
	compileExpectingErrors("ErrorDontDestroyFinal", 1);
    }
    
}
