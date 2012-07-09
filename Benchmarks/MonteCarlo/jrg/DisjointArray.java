/**
 * A simple disjoint array class
 */
public class DisjointArray<region R,refgroup G> {

    public interface SequentialOperation {
	public <region R>void op(Data<R> data, int i);
    }

    public interface ParallelOperation<region R1> {
	public <region R2 | R1 # R2>void op(Data<R2> data, int i)
	    reads R1 writes R2 via data;
    }

    private region Rep;

    private arrayclass RepArray {
	unique(G) Data<R> in R:Rep;
    }

    private unique(G) RepArray rep in R:Rep;

    public DisjointArray(int size) 
	pure
    {
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

    <region R1 | R1 # R>void updateAllParallel(ParallelOperation<R1> operation)
	 reads R1 writes R:*, R via this...G
    {
	for each i in rep pardo {
		if (rep[i] != null) {
		    operation.<region R>op(this.rep[i], i);
		}
	    }
    }

    public interface ParallelMapping<region R1, refgroup G> {
	public <region R2 | R1 # R2>
	    unique(G) Data<R2> map(Data<R2> data, int i)
	    reads R1 writes R2 via data;
    }
	
    public <region R1,refgroup G1 | R1 # R>
	DisjointArray<R,G1>
	withParallelMapping(ParallelMapping<R1,G1> pm)
	reads R1 writes R:*, R via this...G
    {
	DisjointArray<R,G1> result = 
	    new DisjointArray<R,G1>(rep.length);

	for each i in rep pardo {
		if (rep[i] != null) {
		    unique(G1) Data<R> data = pm.<region R>map(this.rep[i],i);
		    result.rep[i] = data;
		}
	    }

	return result;
    }


    void updateAllSequential(SequentialOperation operation) 
    {
	for each i in rep {
		if (rep[i] != null) {
		    operation.<region R>op(this.rep[i], i);
		}
	    }
    }

}