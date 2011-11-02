/**
 * A Barnes Hut force calculation tree
 * Adapted from Olden BH by Joshua Barnes et al.
 * @author Robert L. Bocchino Jr.
 * @author Rakesh Komuravelli
 */

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

public class Tree {

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
     * Flag indicating whether to print debug information
     */
    boolean printBodies;

    /**
     * Calculate bounding box once instead of expanding it on every
     * body insertion
     */
    <refgroup A>void setRsize(BodyArray<A> bodies)
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
    <refgroup A>unique(A) BodyArray<A> stepsystem(int nstep, 
						  unique(A) BodyArray<A> bodies) 
    {
        long start = 0, end = 0;
        // 1. Rebuild the tree with the new positions
	refgroup T;
	// Requires 'copies bodies...A to T'
	unique(T) Node root = 
	    this.<refgroup T,A>maketree(nstep,bodies);
	
	// 2. Reorder the bodies according to position in tree
	refgroup NewA;
        BodyArray<NewA> newBodies = 
	    new BodyArray<NewA>(bodies.length);
	// Requires 'copies root...T to NewA'
        this.<refgroup T,NewA>reorderBodies(root, newBodies);

	// 3. Generate test output
	this.<refgroup NewA>testOutput(newBodies);

        // 4. Compute gravity on particles
	start = System.nanoTime();
        this.<refgroup T,NewA>computegrav(nstep,root,newBodies);
	end = System.nanoTime();
	count += (end-start)/1000000000.0;
	if(!printBodies)
	    System.out.println("timestep " + nstep + " " + (end-start)/1000000000.0);
        
        // 5. Update positions
	this.<refgroup NewA>vp(nstep,newBodies);
	this.<refgroup NewA>setRsize(newBodies);

	return bodies;
    }

    private <refgroup A>void testOutput(BodyArray<A> bodies) 
    {
        if(printBodies) {
            for each i in bodies {
		final int k = i;
                Body p = bodies[k];
                for each j in p.pos.elts {
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
    <refgroup T,A>unique(T) Node maketree(int step, BodyArray<A> bodies) 
	copies bodies...A to T
    {
        int[] xqic;
        unique(T) Node root = null;
        for each i in bodies {
            Body body = bodies[i];
            // only load massive ones
            if (body.mass != 0.0) {
                // insert into tree
                xqic = intcoord(body);
		// Consumes 'copies bodies[i] to T'
                root = this.<refgroup T>loadtree(bodies[i], xqic, root,
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
    <refgroup T,A>void reorderBodies(Node root, 
				     BodyArray<A> newBodies) 
	copies root...T to A
    {
	reorderIndex = 0;
	this.<refgroup T,A>recursiveReorder(root, newBodies);
    }

    <refgroup T,A>void recursiveReorder(Node root, 
					BodyArray<A> newBodies)
	copies root...T to A
    {
	switch (root) instanceof  {
	    case Cell<T>:
		for each i in root.subp {
		    if(root.subp[i] instanceof Body) {
			// Consumes 'copies root.subp[i] to A'
			/*unique(A)*/ Node node = root.subp[i];
			Body body = Util.<Body>cast(node);
			newBodies[reorderIndex] = body;
			assert(newBodies[reorderIndex]!=null);
			reorderIndex++;
		    }
		    else if (root.subp[i] != null) {
			// Consumes 'copies root.subp[i]...T to A'
			recursiveReorder(root.subp[i], newBodies);
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
    <refgroup T>unique(T) Node loadtree(Body body, int[] xpic, 
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
        cell.subp[si] = this.<refgroup T>loadtree(body, xpic, 
						  cell.subp[si],
						  level >> 1, idx);
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
    <refgroup T,A>void computegrav(int nstep,
				   Node root,
				   BodyArray<A> bodies) {

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
    <refgroup A>void vp(int nstep,BodyArray<A> bodies) {
                
      long start1 = System.nanoTime();
      for each i in bodies {
          Vector dvel = new Vector();
          Vector vel1 = new Vector();
          Vector dpos = new Vector();
          double dthf = 0.5 * Constants.dtime;
          
          dvel.MULVS(bodies[i].acc, dthf);
          vel1.ADDV(bodies[i].vel, dvel);
          dpos.MULVS(vel1, Constants.dtime);
          bodies[i].pos.ADDV(bodies[i].pos, dpos);
          bodies[i].vel.ADDV(vel1, dvel);
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

