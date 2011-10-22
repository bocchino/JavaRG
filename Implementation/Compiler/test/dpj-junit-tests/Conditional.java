/**
 * Tests for permission attribution in conditionals
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class Conditional extends DPJTestCase {
    
    public Conditional() {
	super("Conditional");
    }
    
    @Test public void testIfMergeRefPerms() throws Throwable {
	compile("IfMergeRefPerms");
    }
    
    @Test public void testErrorIfMergeRefPerms() throws Throwable {
	compileExpectingErrors("ErrorIfMergeRefPerms", 1);
    }
}