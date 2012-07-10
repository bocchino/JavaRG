/**
 * Tests for unique permissions
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class UniquePerms extends DPJTestCase {
    
    public UniquePerms() {
	super("UniquePerms");
    }
    
    @Test public void testMaskedEffectViaUniqueVar() throws Throwable {
	compile("MaskedEffectViaUniqueVar");
    }

    @Test public void testMaskedEffectViaUniqueField() throws Throwable {
	compile("MaskedEffectViaUniqueField");
    }

}
