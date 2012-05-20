/**
 * Disjoint deref sets --- fields
 */
class DisjointDerefField<refgroup G> {
    class Data {
	region r;
	int d in r;
    }
    unique(G) Data f1 = new Data();
    unique(G) Data f2 = new Data();
    void m() unique(G) preserves G {
	pardo {
	    this.f1.d=0;
	    this.f2.d=1;
	}
    }
}