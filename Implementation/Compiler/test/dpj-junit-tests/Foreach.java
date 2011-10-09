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
    
}
