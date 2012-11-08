class ErrorFinalUniqueCantEscape {
    unique Object m(final unique Object obj) {
	// Error:  Final unique object is borrowed
	return obj;
    }
}