/**
 * Tests for effect interference checking
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class EffectInterference extends DPJTestCase {
    
    public EffectInterference() {
	super("EffectInterference");
    }
    
    @Test public void testWarnInterferenceHeap() throws Throwable {
	compileExpectingWarnings("WarnInterferenceHeap", 1);
    }

    @Test public void testWarnInterferenceStack() throws Throwable {
	compileExpectingWarnings("WarnInterferenceStack", 1);
    }
    
    @Test public void testDisjointNames() throws Throwable {
	compile("DisjointNames");
    }

    @Test public void testDisjointDerefField() throws Throwable {
	compile("DisjointDerefField");
    }

    @Test public void testWarnDerefSameField() throws Throwable {
	compileExpectingWarnings("WarnDerefSameField", 1);
    }

    @Test public void testDisjointDerefCell() throws Throwable {
	compile("DisjointDerefCell");
    }
    
    @Test public void testWarnDerefSameCell() throws Throwable {
	compileExpectingWarnings("WarnDerefSameCell", 1);
    }

}