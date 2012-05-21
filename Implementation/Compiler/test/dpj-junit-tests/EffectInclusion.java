/**
 * Tests for effect inclusion checking
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class EffectInclusion extends DPJTestCase {
    
    public EffectInclusion() {
	super("EffectInclusion");
    }
    
    @Test public void testErrorInitializer() throws Throwable {
	compileExpectingErrors("ErrorInitializer", 1);
    }

    @Test public void testErrorDefaultConstructor() throws Throwable {
	compileExpectingErrors("errorDefaultConstructor", 1);
    }

    @Test public void testErrorNeedRootPerm() throws Throwable {
	compileExpectingErrors("ErrorNeedRootPerm", 1);
    }
    
}