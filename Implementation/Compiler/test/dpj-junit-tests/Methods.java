/**
 * Tests for method syntax
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class Methods extends DPJTestCase {
    
    public Methods() {
	super("Methods");
    }
    
    @Test public void testParams() throws Throwable {
	compile("Params");
    }

    @Test public void testReturnType() throws Throwable {
	compile("ReturnType");
    }

    @Test public void testParamsAndReturnType() throws Throwable {
	compile("ParamsAndReturnType");
    }

}
