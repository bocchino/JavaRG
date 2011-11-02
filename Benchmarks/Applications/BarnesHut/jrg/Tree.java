/**
 * A Barnes Hut force calculation tree
 * Adapted from Olden BH by Joshua Barnes et al.
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

public class Tree<refgroup T,A> {

    /**
     * Bounding box for tree
     */
    public final Vector rmin = new Vector();
    public double rsize;

    /**
     * Count of time elapsed for force computation
     */
    public float count;

    /**
     * Root of the tree
     */
    public Node root;

    /**
     * Nodes of the tree
     */
    public BodyArray<A> bodies;

    /**
     * Flag indicating whether to print debug information
     */
    boolean printBodies;

    /**
     * Calculate bounding box once instead of expanding it on every
     * body insertion
     */
    void setRsize()
    {
        Vector max  = new Vector();
        Vector min  = new Vector();
        double side = 0;
        min.SETVS(Double.MAX_VALUE);
        max.SETVS(Double.MIN_VALUE);
        for(int i = 0; i < bodies.length; i++)
        {
	    final int k = i;
            Body p = bodies[k];
            for(int j = 0; j < Constants.NDIM; j++)
            {
                if(p.pos.elts[j] < min.elts[j])
                    min.elts[j] = p.pos.elts[j];
                if(p.pos.elts[j] > max.elts[j])
                    max.elts[j] = p.pos.elts[j];
            }
        }
        max.SUBV(max, min);
        for(int i = 0; i < Constants.NDIM; i++)
        {
            if(side < max.elts[i])
                side = max.elts[i];
        }
        rmin.ADDVS(min, -side/100000.0);
        rsize = 1.00002*side;
    }

    /**
     * Advance N-body system one time-step
     * @param nstep nth step
     */
    void stepsystem(int nstep, BodyArray<A> bodies) 
    {
        long start = 0, end = 0;
        // 1. Rebuild the tree with the new positions
	root = maketree(nstep,bodies);
        BodyArray<A> newBodies = new BodyArray<A>(bodies.length);
        reorderBodies(root, newBodies);
        bodies = newBodies;
	// Generate test output
	testOutput(newBodies);

        // 2. Compute gravity on particles
	start = System.nanoTime();
        computegrav(nstep);
	end = System.nanoTime();
	count += (end-start)/1000000000.0;
	if(!printBodies)
	    System.out.println("timestep " + nstep + " " + (end-start)/1000000000.0);
        
        // 3. Update positions
	vp(nstep);
	setRsize();
    }

    private void testOutput(BodyArray<A> bodies) 
    {
        if(printBodies)
        {
            for(int i = 0; i < bodies.length; i++)
            {
		final int k = i;
                Body p = bodies[k];
                for(int j = 0; j < Constants.NDIM; j++)
                {
                    System.out.printf("%.6f", p.pos.elts[j]);
                    System.out.print(" ");
                }
                System.out.println("");
            }
        }
    }

    /**
     *  Initialize tree structure for hack force calculation.                     
     */
    unique(T) Node maketree(int step, BodyArray<A> bodies) 
    {
        int[] xqic;
        unique(T) Node root = null;
        for (int i = 0; i < bodies.length; ++i) {
	    final int j = i;
            Body body = bodies[j];
            // only load massive ones
            if (body.mass != 0.0) {
                // insert into tree
                xqic = intcoord(body);
                root = loadtree(body, xqic, root,
				Constants.IMAX >> 1, i);
            }
        }
        assert(Util.chatting("About to hackcofm\n"));
        root.hackcofm();
        return root;
    }

    /**
     * Reorder the body array to capture the positioning in the tree
     * @param root
     * @param index
     * @return
     */
    int reorderIndex;
    void reorderBodies(Node root, BodyArray<A> newBodies) 
    {
	reorderIndex = 0;
	recursiveReorder(root, newBodies);
    }

    void recursiveReorder(Node root, BodyArray<A> newBodies)
    {
        if (root instanceof Cell) {
            Cell<T> cell = Util.<Cell<T>>cast(root);
            for(int i = 0; i < Constants.NSUB; i++) 
            {
                if(cell.subp[i] == null)
                    continue;
                if(cell.subp[i] instanceof Body)
                {
		    final int j = i;
                    Body body = Util.<Body>cast(cell.subp[j]);
		    newBodies[reorderIndex] = body;
                    assert(newBodies[reorderIndex]!=null);
		    reorderIndex++;
                }
                else
                {
                    recursiveReorder(cell.subp[i], newBodies);
                }
            }
        }
    }

    /**
     * Descend tree and insert particle.
     * @param body - body to be loaded 
     * @param xpic - integer coordinates of p
     * @param level - current level in tree 
     * @param idx - index of body in 
     */
    unique(T) Node 
	loadtree(Body body, int[] xpic, 
		 unique(T) Node subroot, 
		 int level, int idx) 
	copies body to T
    {
        if (subroot == null) {
            return body;
        }
        /*   dont run out of bits   */
        assert(level != 0);
        unique(T) Cell<T> cell = null;
        if (subroot instanceof Body) {
            cell = new Cell<T>();
            final int si1 = subindex(intcoord(Util.<Body>cast(subroot)), 
				     level); 
            cell.subp[si1] = subroot;
        } 
        else {
            cell = Util.<Cell<T>,refgroup T>castUnique(subroot);
        }
        final int si = subindex(xpic, level);
        cell.subp[si] = this.loadtree(body, xpic, 
				      cell.subp[si], level >> 1, idx);
        return cell;
    }

    /**
     * Find the sub index into the cell children
     * @param x int coords of the body pos
     * @param l level
     * @return
     */
    int subindex(int[] x, int l) {
        int i, k;
        boolean yes;
        i = 0;
        yes = false;
        if ((x[0] & l) != 0) {
            i += Constants.NSUB >> 1;
            yes = true;
        }
        for (k = 1; k < Constants.NDIM; k++) {
            if ((((x[k] & l) != 0) && !yes)  || ((!((x[k] & l) != 0) && yes))) {
                i += Constants.NSUB >> (k + 1);
                yes = true;
            }
            else
                yes = false;
        }

        return (i);
    }

    /**
     * Compute and update forces on particles
     */
    void computegrav(int nstep) {

        for each i in bodies pardo {
	    region r;
	    HGStruct<r> hg = new HGStruct<r>();
            Vector acc1 = new Vector();
            Vector dacc = new Vector();
            Vector dvel = new Vector();
            double dthf = 0.5 * Constants.dtime;
        
            hg.pskip = bodies[i];
            hg.phi0 = 0;
            hg.pos0.SETV(bodies[i].pos);
            hg.acc0.CLRV();
            acc1.SETV(bodies[i].acc);
            bodies[i].<region r,refgroup T> hackgrav(hg, rsize, root);
            if(nstep > 0)
            {
                dacc.SUBV(bodies[i].acc, acc1);
                dvel.MULVS(dacc, dthf);
                bodies[i].vel.ADDV(bodies[i].vel, dvel);
            }
        }

    }


    /**
     * Update the points based on computed forces
     */
    void vp(int nstep) {
                
      long start1 = System.nanoTime();
      for (int i = 0; i < bodies.length; i++) {
	  final int j = i;
          Vector dvel = new Vector();
          Vector vel1 = new Vector();
          Vector dpos = new Vector();
          double dthf = 0.5 * Constants.dtime;
          
          dvel.MULVS(bodies[j].acc, dthf);
          vel1.ADDV(bodies[j].vel, dvel);
          dpos.MULVS(vel1, Constants.dtime);
          bodies[j].pos.ADDV(bodies[j].pos, dpos);
          bodies[j].vel.ADDV(vel1, dvel);
        }
      long end1 = System.nanoTime();
      if(!printBodies)
          System.out.println("vp " + (end1-start1)/1000000000.0);
    }

    /**
     * Compute integerized coordinates.
     * Returns: TRUE unless rp was out of bounds.
     */
    public int[] intcoord(Body p) {
        double xsc;
        int[] ic = new int[3];
        boolean inb;
        Vector pos = new Vector();
        pos.SETV(p.pos);

        xsc = (pos.elts[0] - rmin.elts[0]) / rsize;
        if (0.0 <= xsc && xsc < 1.0) 
            ic[0] = 
                (int) Math.floor(Constants.IMAX * xsc);
        else {
            inb = false;
        }

        xsc = (pos.elts[1] - rmin.elts[1]) / rsize;
        if (0.0 <= xsc && xsc < 1.0)
            ic[1] = 
                (int) Math.floor(Constants.IMAX * xsc);
        else {
            inb = false;
        }

        xsc = (pos.elts[2] - rmin.elts[2]) / rsize;
        if (0.0 <= xsc && xsc < 1.0)
            ic[2] = 
                (int) Math.floor(Constants.IMAX * xsc);
        else { 
            inb = false;
        }
        return (ic);
    }
}

