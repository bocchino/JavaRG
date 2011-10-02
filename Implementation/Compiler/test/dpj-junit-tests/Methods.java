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

    @Test public void testThisRefPerm() throws Throwable {
	compile("ThisRefPerm");
    }

    @Test public void testFreshGroups() throws Throwable {
	compile("FreshGroups");
    }
    
    @Test public void testCopyPerms() throws Throwable {
	compile("CopyPerms");
    }

    @Test public void testEffectPerms() throws Throwable {
	compile("EffectPerms");
    }

    @Test public void testUpdatePerms() throws Throwable {
	compile("UpdatePerms");
    }
    
}
