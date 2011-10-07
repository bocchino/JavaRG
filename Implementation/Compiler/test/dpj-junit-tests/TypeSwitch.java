/**
 * Tests for type switch statements
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class TypeSwitch extends DPJTestCase {
    
    public TypeSwitch() {
	super("TypeSwitch");
    }
    
    @Test public void testBasic() throws Throwable {
	compile("Basic");
    }
    
    @Test public void testVariableDowncast() throws Throwable {
	compile("VariableDowncast");
    }

}
