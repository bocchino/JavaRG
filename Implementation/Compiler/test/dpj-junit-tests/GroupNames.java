/**
 * Tests for group name declarations
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class GroupNames extends DPJTestCase {
    
    public GroupNames() {
	super("GroupNames");
    }
    
    @Test public void testDecl() throws Throwable {
	compile("Decl");
    }
    
}
