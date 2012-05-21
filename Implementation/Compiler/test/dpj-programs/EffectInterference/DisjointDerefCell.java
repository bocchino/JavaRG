/**
 * Disjoint deref sets --- array cells
 */
class DisjointDerefCell<refgroup G> {
    class Data {
	region DR;
	int d in DR;
    }
    arrayclass Array<refgroup G> {
	region AR;
	unique(G) Data in AR;
    }
    void m(unique(G) Array<G> a) preserves G {
	pardo {
	    a[0].d=0;
	    a[1].d=1;
	}
    }
}