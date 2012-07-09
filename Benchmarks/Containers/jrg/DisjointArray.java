/**
 * A simple disjoint array class
 */
public class DisjointArray<region R,refgroup G> {

    public interface SequentialOperation {
	public <region R>void op(Data<R> data);
    }

    public interface ParallelOperation {
	public <region R>void op(Data<R> data)
	    writes R via data;
    }

    private region Rep;

    private arrayclass RepArray {
	unique(G) Data<R> in R:Rep;
    }

    private unique(G) RepArray rep in R:Rep;

    public DisjointArray(int size) {
	rep = new RepArray(size);
    }

    public int size() {
	return rep.length;
    }

    public <refgroup NewG>DisjointArray<R,NewG> freshArray() 
	fresh NewG switches G, NewG
    {
	DisjointArray<R,NewG> result = 
	    new DisjointArray<R,NewG>(rep.length);
	unique(G) RepArray rep = !this.rep;
	for each i in rep {
		// Consumes 'copies rep[i] to NewG'
		unique(NewG) Data<R> data = rep[i];
		result.rep[i] = data;
	    }
	this.rep = rep;
	return result;
    }

    public int length() {
	return rep.length;
    }

    void set(unique(G) Data<R> data, int idx) 
	writes R:*
    {
	rep[idx] = data;
    }

    Data<R> getShared(int idx) 
	reads R:*
    {
	return rep[idx];
    }

    unique(G) Data<R> getUnique(final int idx) 
	reads R:*
    {
	return !rep[idx];
    }

    void updateAllParallel(ParallelOperation operation)
       reads R:* writes R via this...G
    {
	for each i in rep pardo {
		if (rep[i] != null) {
		    operation.<region R>op(this.rep[i]);
		}
	    }
    }

    void updateAllSequential(SequentialOperation operation) 
    {
	for each i in rep {
		if (rep[i] != null) {
		    operation.<region R>op(this.rep[i]);
		}
	    }
    }

}