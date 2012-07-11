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
    
    @Test public void testErrorUniqueTreeEffect() throws Throwable {
	compileExpectingErrors("ErrorUniqueTreeEffect", 1);
    }
    
    @Test public void testErrorDontMaskFinalUniqueField() throws Throwable {
	compileExpectingErrors("ErrorDontMaskFinalUniqueField", 1);	
    }
    
    @Test public void testIncludeEffectOnFinalUniqueField() throws Throwable {
	compile("IncludeEffectOnFinalUniqueField");
    }

    @Test public void testErrorFinalUniqueCantEscape() throws Throwable {
	compileExpectingErrors("ErrorFinalUniqueCantEscape", 1);
    }
    
    @Test public void testBorrowByAssignment() throws Throwable {
	compile("BorrowByAssignment");
    }

    @Test public void testErrorConsumeByAssignment() throws Throwable {
	compileExpectingErrors("ErrorConsumeByAssignment", 1);
    }

}
