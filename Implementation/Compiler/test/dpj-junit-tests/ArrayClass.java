/**
 * Tests for array class declarations
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ArrayClass extends DPJTestCase {
    
    public ArrayClass() {
	super("ArrayClass");
    }
    
    @Test public void testBasic() throws Throwable {
	compile("Basic");
    }
    
    @Test public void testRefPerm() throws Throwable {
	compile("RefPerm");
    }
    
    @Test public void testRegionDecl() throws Throwable {
	compile("RegionDecl");
    }
    
    @Test public void testAllParams() throws Throwable {
	compile("AllParams");
    }

    @Test public void testIndexExpression() throws Throwable {
	compile("IndexExpression");
    }
    
    @Test public void testIndexExpressionGeneric() throws Throwable {
	compile("IndexExpressionGeneric");
    }
    
    @Test public void testIndexExpressionInt() throws Throwable {
	compile("IndexExpressionInt");
    }
    
    @Test public void testArrayCreationInt() throws Throwable {
	compile("ArrayCreationInt");
    }
    
    @Test public void testArrayAccessInt() throws Throwable {
	compile("ArrayAccessInt");
    }
    
    @Test public void testArrayCreation() throws Throwable {
	compile("ArrayCreation");
    }
    
    @Test public void testArrayAccess() throws Throwable{
	compile("ArrayAccess");
    }
    
    @Test public void testArrayAccessGeneric() throws Throwable {
	compile("ArrayAccessGeneric");
    }
    
    @Test public void testArrayCreationGeneric() throws Throwable {
	compile("ArrayCreationGeneric");
    }
    
    @Test public void testArrayOfArrayOfInt() throws Throwable {
	compile("ArrayOfArrayOfInt");
    }
    
    @Test public void testErrorNonDestructive() throws Throwable {
	compileExpectingWarnings("ErrorNonDestructive", 1);
    }
    
}
