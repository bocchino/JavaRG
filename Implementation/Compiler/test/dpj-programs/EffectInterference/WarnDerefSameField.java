/**
 * Non-disjoint deref sets --- fields
 */
class WarnDerefSameField<refgroup G> {
    class Data {
	region r;
	int d in r;
    }
    unique(G) Data f = new Data();
    void m() preserves G {
	pardo {
	    // Should warn
	    this.f.d=0;
	    this.f.d=1;
	}
    }
}