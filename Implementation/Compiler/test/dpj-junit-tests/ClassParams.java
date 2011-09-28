/**
 * Some placeholder tests
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class ClassParams extends DPJTestCase {
    
    public ClassParams() {
	super("ClassParams");
    }
    
    @Test public void testRPLParams() throws Throwable {
	compile("RPLParams");
    }
    
    @Test public void testGroupParams() throws Throwable {
	compile("GroupParams");
    }
    
    @Test public void testAllParams() throws Throwable {
	compile("AllParams");
    }
    
}
