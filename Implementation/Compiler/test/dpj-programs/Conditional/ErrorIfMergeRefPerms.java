/**
 * Potentially used ref perm should not survive conditional
 */
abstract class ErrorIfMergeRefPerms {
    class Data{}
    abstract boolean test();
    <refgroup G>unique(G) Data m() {
	unique(G) Data data = new Data();
	unique(G) Data data1 = null;
	if (test()) {
	    // data permission might be lost here
	    data1 = data;
	}
	else {
	    // Do something else
	}
	return data; // This should not work
    }
}