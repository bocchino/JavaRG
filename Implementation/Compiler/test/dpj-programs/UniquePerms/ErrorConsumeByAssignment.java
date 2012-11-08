class ErrorConsumeByAssignment {
    class C {}
    unique C m() {
	unique C c1 = new C();
	{
	    // consume c1 into c2
	    unique C c2 = c1;
	}
	// error:  can't return c1 here
	return c1;
    }
}