/**
 * A simple disjoint array class
 */
public class Array<region R,refgroup G> {

    public static abstract class Data<region R> {
	public abstract <refgroup G>void updateParallel() 
	    writes R via this...G;
	public abstract void updateSequential();
    }

    private region Rep;

    private arrayclass RepArray {
	unique(G) Data<R> in R:Rep;
    }

    private unique(G) RepArray rep in R:Rep;

    public Array(int size) {
	rep = new RepArray(size);
    }

    public int size() {
	return rep.length;
    }

    public <refgroup NewG>Array<R,NewG> freshArray() 
	fresh NewG switches G, NewG
    {
	Array<R,NewG> result = 
	    new Array<R,NewG>(rep.length);
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

    void updateAllParallel()
       reads R:* writes R via this...G
    {
	for each i in rep pardo {
		if (rep[i] != null) {
		    this.rep[i].<refgroup G>updateParallel();
		}
	    }
    }

    void updateAllSequential() 
    {
	for each i in rep {
		this.rep[i].updateSequential();
	    }
    }

}