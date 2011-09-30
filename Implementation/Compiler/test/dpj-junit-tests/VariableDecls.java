/**
 * Tests for variable declarations
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class VariableDecls extends DPJTestCase {
    
    public VariableDecls() {
	super("VariableDecls");
    }
    
    @Test public void testLocallyUniqueField() throws Throwable {
	compile("LocallyUniqueField");
    }

    @Test public void testLocallyUniqueVar() throws Throwable {
	compile("LocallyUniqueVar");
    }
    
    @Test public void testFieldInRegion() throws Throwable {
	compile("FieldInRegion");
    }
    
}
