/**
 * Represents a node in the Barnes-Hut tree
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

public abstract class Node {

    /**
     * Regions
     */
    region Forces, Masses, Positions;

    /**
     * Total mass of node
     */
    public double mass in Masses;

    /**
     * Position of node
     */
    public final unique Vector<Positions> pos 
	in Positions = new Vector<Positions>();

    /**
     * Constructor
     */
    public Node() {}

    /**
     * Copy Constructor
     * @param node
     */
    public Node(Node node) {
        this.mass = node.mass;
        this.pos.SETV(node.pos);
    }

    /**
     * Descend tree finding center-of-mass coordinates.
     */
    public abstract double hackcofm();

    /**
     *  Decide if a node should be opened.
     * @param p Node of interest
     * @param dsq
     * @param tolsq
     * @param hg Object holding intermediate computations 
     *           and other required info
     * @return
     */
    protected abstract <region Rhg> boolean subdivp(Node p, double dsq, 
						    double tolsq, HGStruct<Rhg> hg_Node) 
	reads Masses, Positions
	writes Rhg via hg_Node;

}
