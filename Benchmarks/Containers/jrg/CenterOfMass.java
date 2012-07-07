/**
 * <p>Simplified 1-D version of the center-of-mass computation from
 * the Barnes-Hut n-body algorithm.</p>
 **/
public class CenterOfMass {
    final static int NBODIES = 10;
    final static int LEFT = 0, RIGHT = 1;
    final static double HORIZON = NBODIES * 2;
    final static boolean DEBUG = true;

    public static class Body<region R> 
	implements Data<R>
    {
	/**
	 * Range of body masses is 0..MAXMASS.  Units are arbitrary.
	 */
	public static final double MAXMASS = 1000.0;
	
	/**
	 * Random number generator for values in the range [0.0..1.0].
	 * The mass is random*MAXMASS.
	 */
	public static final java.util.Random rstream =
	    new java.util.Random(0); // don't care seed
	
	/**
	 * Operation to generate each new body.
	 */
	public static <region R,refgroup G>unique(G) Body<R> create(int i) 
	    pure 
	{
	    double mass = MAXMASS * rstream.nextDouble();
	    return new Body<R>(2*i, mass, 0.0);
	}

        public double pos in R;
        public double mass in R;
        public double phi in R;
        public Body(int x, double mass, double phi) pure {
            pos = (double) x;
            this.mass = mass;
            this.phi = phi;
        }
	public Body() pure {}
	public String toString() {
	    return "Body (pos=" + this.pos +
		", mass=" + this.mass + ")";
	}
    }
    
    public static class Cell<region R>
	extends Body<R>
    {
	/**
	 * Left end of bounding box
	 */
        public double left  in R;
	/**
	 * Right end of bounding box
	 */
        public double right in R;
        public Cell(double left, double right) pure {
            this.left = left;
            this.right = right;
        }
        public double midpoint() reads R {
            return (left + right) / 2;
        }
	public String toString() {
	    return "Cell (box=[" + this.left +
		"," + this.right + 
		"], pos=" + this.pos +
		", mass=" + this.mass + ")";
	}
    }

    public static class BHExpander<region R,refgroup G>
	implements Tree.NodeExpander<R,G> {
        public int indexToExpand(Data<R> curValue,
				 Data<R> parentValue,
				 Data<R> valueToInsert)
	    reads R 
	{
	    switch (curValue) instanceof {
		case Cell<R>:
		    return (curValue.midpoint() > ((Body<R>)valueToInsert).pos)
			? LEFT : RIGHT;
            }
	    return LEFT;
        }
        
        public unique(G) Data<R>
	    nodeFactory(Data<R> curValue,
			Data<R> parentValue,
			int indexOfCurNodeInParent,
			Data<R> valueToInsert)
	    reads R 
	{
	    switch (parentValue) instanceof {
		case Cell<R>:
		    return  (indexOfCurNodeInParent == LEFT) ?
			new Cell<R>(parentValue.left, parentValue.midpoint()) :
		    new Cell<R>(parentValue.midpoint(), parentValue.right);
		}
	    return new Cell<R>(0.0, HORIZON);
        }

    }
    
    private static class MassPos 
	implements Result
    {
	public final double mass;
	public final double pos;
	public MassPos(double m, double p)
	    pure
	{
	    this.mass = m;
	    this.pos = p;
	}
    }

    public static class CofMVisitor
	implements Tree.POVisitor
    {	
        public <region R1,R2>Result
	    visit(Data<R1> curBody, ResultArray<R2> childValues)
	    reads R2 writes R1 via curBody
	{

	    switch (curBody) instanceof {
		case Cell<R1>:
		    double pos = curBody.mass * curBody.pos;
		    double mass = curBody.mass;
		    for each i in childValues {
			    Result result = childValues[i];
			    switch (result) instanceof {
				case MassPos:
				    pos += result.mass * result.pos;
				    mass += result.mass;
				}
			}
		    
		    curBody.mass = mass;
		    curBody.pos = pos / mass;
		}

	    switch (curBody) instanceof {
		case Body<R1>:
		    return new MassPos(curBody.mass, curBody.pos);
		}

	    return null;
	}
    }

    public static void main(String[] args) {
	region DataRegion;
	refgroup ArrayGroup, TreeGroup;

        // Construct the body array
	unique(ArrayGroup) UniqueDataArray<DataRegion,ArrayGroup> bodies = 
	    new UniqueDataArray<DataRegion,ArrayGroup>(NBODIES);
	for each i in bodies {
		bodies[i] = 
		    Body.<region DataRegion,refgroup ArrayGroup>create(i);
	    }

        // Construct the tree
	Tree<DataRegion,TreeGroup> tree = 
	    new Tree<DataRegion,TreeGroup>(2);
	tree.<refgroup ArrayGroup>buildTree(bodies, 
					    new BHExpander<DataRegion,TreeGroup>());
	// Walk the tree, computing CofM
	tree.parallelPO(new CofMVisitor());

	// Print the tree
	tree.print();
    }
}
