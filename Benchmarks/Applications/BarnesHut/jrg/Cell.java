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
    public double hackcofm() {
        Vector[] tmpv    = new Vector[Constants.NSUB];
        Vector   tmp_pos = new Vector();
        double   mq;
        double[] mrs     = new double[Constants.NSUB];

        mq   = 0.0;
        
        for (int i = 0; i < Constants.NSUB; i++) {
            Node r = subp[i];
            if (r != null) {
                tmpv[i] = new Vector();
                mrs[i] = r.hackcofm();
                /* find moment */
                tmpv[i].MULVS(r.pos, mrs[i]);
            }
        }
        for (int i = 0; i < Constants.NSUB; ++i) {
            /* sum tot. moment */
            if (tmpv[i] != null)
                tmp_pos.ADDV(tmp_pos, tmpv[i]);
            mq = mrs[i] + mq;
        }

        mass = mq;
        /* rescale cms position */
        pos.DIVVS(tmp_pos, mass);
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
