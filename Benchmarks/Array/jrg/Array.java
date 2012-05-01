/**
 * A simple disjoint array class
 */
public class Array<region R,refgroup G> {

    public static abstract class Data<region R> {
	public abstract void updateParallel() 
	    writes R via this;
	public abstract void updateSequential();
    }

    private arrayclass Rep {
	unique(G) Data<R> in R;
    }

    private unique(G) Rep rep in R;

    public Array(int size) {
	rep = new Rep(size);
    }

    public <refgroup NewG>Array<R,NewG> freshArray() 
	fresh NewG 
    {
	Array<R,NewG> result = new Array<R,NewG>(rep.length);
	unique(G) Rep rep = !this.rep;
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

    void put(unique(G) Data<R> data, int idx) 
	writes R via this 
    {
	rep[idx] = data;
    }

    Data<R> getShared(int idx) 
	reads R via this 
    {
	return rep[idx];
    }

    unique(G) Data<R> getUnique(final int idx) 
	reads R via this 
    {
	return !rep[idx];
    }

    void updateAllParallel() 
	writes R via this...G
    {
	for each i in rep pardo {
		if (rep[i] != null) {
		    this.rep[i].updateParallel();
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