/**
 * Tests for method region parameters
 * 
 * @author Rob Bocchino
 */

import org.junit.Test;


public class MethodRegionParams extends DPJTestCase {
    
    public MethodRegionParams() {
	super("MethodRegionParams");
    }
    
    @Test public void testErrorInferCaptureParams() throws Throwable {
	compileExpectingErrors("ErrorInferCaptureParams", 1);
    }
    
    @Test public void testErrorInferEffect() throws Throwable {
	compileExpectingErrors("ErrorInferEffect", 1);
    }

    @Test public void testErrorCaptureExplicitArgs() throws Throwable {
	compileExpectingErrors("ErrorCaptureExplicitArgs", 1);
    }

    @Test public void testErrorExplicitEffect() throws Throwable {
	compileExpectingErrors("ErrorExplicitEffect", 1);
    }

}
