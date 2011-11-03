/**
 * A simple disjoint array class
 */
public class Array<region R,refgroup G> {

    public interface Data<region R,refgroup G> {
	public void updateParallel() writes R via this;
	public void updateSequential();
    }

    private arrayclass Rep {
	unique(G) Data in R;
    }

    private final Rep rep;

    public Array(int size) {
	rep = new Rep(size);
    }

    void put(unique(G) Data data, int idx) {
	rep[idx] = data;
    }

    Data getShared(int idx) {
	return rep[idx];
    }

    unique(G) Data getUnique(int idx) {
	return !rep[idx];
    }

    void updateAllParallel() {
	for each i in rep pardo {
		rep[i].update();
	    }
    }

    void updateAllSequential() {
	for each i in rep {
		rep[i].update();
	    }
    }

}