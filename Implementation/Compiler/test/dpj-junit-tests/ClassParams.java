/**
 * Tests for class parameters
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
    
    @Test public void testRPLParamsExtends() throws Throwable {
	compile("RPLParamsExtends");
    }
    
    @Test public void testGroupParamsExtends() throws Throwable {
	compile("GroupParamsExtends");
    }

    @Test public void testAllParamsExtends() throws Throwable {
	compile("AllParamsExtends");
    }
    
    @Test public void testRPLParamsImplements() throws Throwable {
	compile("RPLParamsImplements");
    }
    
    @Test public void testGroupParamsImplements() throws Throwable {
	compile("GroupParamsImplements");
    }
    
    @Test public void testAllParamsImplements() throws Throwable {
	compile("AllParamsImplements");
    }

}
