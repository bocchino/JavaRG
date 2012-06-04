/**
 * A simple disjoint array class
 */
public class Array<region Elts,Rep,refgroup G | Elts # Rep> {

    public static abstract class Data<region Elts> {
	public abstract void updateParallel() 
	    writes Elts via this;
	public abstract void updateSequential();
    }

    private arrayclass RepArray {
	unique(G) Data<Elts> in Rep;
    }

    private unique(G) RepArray rep in Rep;

    public Array(int size) {
	rep = new RepArray(size);
    }

    public <refgroup NewG>Array<Elts,Rep,NewG> freshArray() 
	fresh NewG switches G, NewG
    {
	Array<Elts,Rep,NewG> result = 
	    new Array<Elts,Rep,NewG>(rep.length);
	unique(G) RepArray rep = !this.rep;
	for each i in rep {
		// Consumes 'copies rep[i] to NewG'
		unique(NewG) Data<Elts> data = rep[i];
		result.rep[i] = data;
	    }
	this.rep = rep;
	return result;
    }

    public int length() {
	return rep.length;
    }

    void put(unique(G) Data<Elts> data, int idx) 
	writes Rep
    {
	rep[idx] = data;
    }

    Data<Elts> getShared(int idx) 
	reads Rep, Elts
    {
	return rep[idx];
    }

    unique(G) Data<Elts> getUnique(final int idx) 
	reads Rep, Elts
    {
	return !rep[idx];
    }

    void updateAllParallel() 
	reads Rep writes Elts via this...G
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