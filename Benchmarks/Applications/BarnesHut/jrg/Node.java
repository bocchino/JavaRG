/**
 * Represents a node in the Barnes-Hut tree
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

public abstract class Node {

    /**
     * Total mass of node
     */
    public double mass in BarnesHut.Masses;

    /**
     * Position of node
     */
    public Vector pos in BarnesHut.Positions = new Vector();

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
    protected abstract boolean subdivp(Node p, double dsq, 
				       double tolsq, HGStruct hg);
}
