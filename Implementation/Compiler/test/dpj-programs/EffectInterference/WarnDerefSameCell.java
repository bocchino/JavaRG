/**
 * Non-disjoint deref sets --- array cells
 */
class WarnDerefSameCell<refgroup G> {
    class Data {
	region DR;
	int d in DR;
    }
    arrayclass Array<refgroup G> {
	region AR;
	unique(G) Data in AR;
    }
    void m(Array<G> a) unique(G) preserves G {
	pardo {
	    a[0].d=0;
	    a[0].d=1;
	}
    }
}