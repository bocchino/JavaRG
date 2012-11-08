class BorrowByAssignment {
    class C {}
    unique C m() {
	unique C c1 = new C();
	{
	    // borrow c1 into c2
	    final unique C c2 = c1;
	}
	// return c1 here
	return c1;
    }
}