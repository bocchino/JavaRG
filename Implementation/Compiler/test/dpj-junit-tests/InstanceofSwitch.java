/**
 * Tests for instanceof switch statements
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class InstanceofSwitch extends DPJTestCase {
    
    public InstanceofSwitch() {
	super("InstanceofSwitch");
    }
    
    @Test public void testBasic() throws Throwable {
	compile("Basic");
    }
    
    @Test public void testVariableDowncast() throws Throwable {
	compile("VariableDowncast");
    }
    
    @Test public void testErrorNotIdentifier() throws Throwable {
	compileExpectingErrors("ErrorNotIdentifier", 1);
    }

}
