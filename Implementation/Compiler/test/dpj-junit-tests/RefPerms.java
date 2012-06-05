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
    
    @Test public void testErrorSharedThis() throws Throwable {
	compileExpectingErrors("ErrorSharedThis", 1);
    }
    
    @Test public void testLocallyUniqueThis() throws Throwable {
	compile("LocallyUniqueThis");
    }
    
    @Test public void testErrorConsumeUniqueThis() throws Throwable {
	compileExpectingErrors("ErrorConsumeUniqueThis", 1);
    }
    
    @Test public void testExplicitThis() throws Throwable {
	compile("ExplicitThis");
    }
    
    @Test public void testImplicitThis() throws Throwable {
	compile("ImplicitThis");
    }
    
    @Test public void testErrorExplicitThis() throws Throwable {
	compileExpectingErrors("ErrorExplicitThis", 1);
    }
    
    @Test public void testErrorImplicitThis() throws Throwable {
	compileExpectingErrors("ErrorImplicitThis", 1);
    }
    
    @Test public void testErrorSharedArgToThis() throws Throwable {
	compileExpectingErrors("ErrorSharedArgToThis", 1);
    }
 
    @Test public void testErrorUniqueNotFinal() throws Throwable {
	compileExpectingErrors("ErrorUniqueNotFinal", 1);
    }
    
}
