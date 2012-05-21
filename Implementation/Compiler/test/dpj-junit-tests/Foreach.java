/**
 * Tests for foreach statements
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class Foreach extends DPJTestCase {
    
    public Foreach() {
	super("Foreach");
    }
    
    @Test public void testBasicSeq() throws Throwable {
	compile("BasicSeq");
    }

    @Test public void testBasicPar() throws Throwable {
	compileExpectingWarnings("BasicPar", 1);
    }
    
    @Test public void testErrorPreservesUpdatesPar() throws Throwable {
	compile("ErrorPreservesUpdatesPar", 1, 1);
    }

}
