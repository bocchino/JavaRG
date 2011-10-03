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
    
}
