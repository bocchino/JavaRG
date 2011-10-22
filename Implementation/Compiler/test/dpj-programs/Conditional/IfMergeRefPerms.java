/**
 * Unused ref perms should survive conditional
 */
abstract class MergeRefPerms {
    class Data{}
    abstract boolean test();
    <refgroup G>unique(G) Data m() {
	unique(G) Data data = new Data();
	if (test()) {
	    // Do something
	}
	else {
	    // Do something else
	}
	return data; // This should work
    }
}