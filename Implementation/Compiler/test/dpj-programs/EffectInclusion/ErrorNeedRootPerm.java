/**
 * Test missing root effect perm
 */
class ErrorNeedRootPerm<refgroup G> {
    class Data {
	region r;
	int d in r;
    }
    unique(G) Data f1 = new Data();
    unique(G) Data f2 = new Data();
    void m()  
    	// Missing effect writes r via this.f1
    	// Missing effect writes r via this.f2
    	// this is shared ==> writes Root:* doesn't cover these effects
    	// after they are used in a tree comparison
    	preserves G    
    {
	pardo {
	    this.f1.d=0;
	    this.f2.d=1;
	}
    }
}