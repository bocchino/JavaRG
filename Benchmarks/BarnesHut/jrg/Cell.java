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
