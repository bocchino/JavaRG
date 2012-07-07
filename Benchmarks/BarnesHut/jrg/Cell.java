/**
 * Cell in the BH tree, i.e., a node with children.
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

public class Cell<refgroup T> extends Node {

    /**
     * Descendants of cell
     */
    public final unique(T) NodeArray<T> subp 
	in BarnesHut.Links = new NodeArray<T>(Constants.NSUB); 

    /**
     * Descend tree finding center-of-mass coordinates.
     */
    @Override
    public double hackcofm()
    {
	region Loc;

	VectorArray<Loc> tmpv = new VectorArray<Loc>(Constants.NSUB);
	Vector<Loc>   tmp_pos = new Vector<Loc>();
	double   mq;
	DoubleArray<Loc> mrs = new DoubleArray<Loc>(Constants.NSUB);
	
	mq   = 0.0;
	    
	for each i in subp {
		if (subp[i] != null) {
		    tmpv[i] = new Vector<Loc>();
		    mrs[i] = this.subp[i].hackcofm();
		    /* find moment */
		    tmpv[i].MULVS(this.subp[i].pos, mrs[i]);
		}
	    }
	for each i in tmpv {
		/* sum tot. moment */
		if (tmpv[i] != null)
		    tmp_pos.ADDV(tmp_pos, tmpv[i]);
		mq = mrs[i] + mq;
	    }
	
	this.mass = mq;
	/* rescale cms position */
	pos.DIVVS(tmp_pos, this.mass);
        return mq;
    }

    /**
     * Decide if a node should be opened.
     */
    @Override
    protected <region Rhg>boolean subdivp(Node p, double dsq, 
					  double tolsq, HGStruct<Rhg> hg) 
	reads Masses, Positions
	writes Rhg via hg
    {
        double drsq;
        /* compute displacement */   
        hg.dr.SUBV(p.pos, hg.pos0);
        /* and find dist squared */
        drsq = hg.dr.DOTVP(hg.dr);
        /* use geometrical rule */
        return (tolsq * drsq < dsq);
    }
}
