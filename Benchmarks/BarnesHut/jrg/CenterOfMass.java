public class CenterOfMass {

    /**
     * Descend tree finding center-of-mass coordinates.
     */
    public static <refgroup G>double hackcofm(Node node)
	reads BarnesHut.Links
	writes Node.Masses via node...G, Node.Positions via node...G
    {
	region Loc;

	VectorArray<Loc> tmpv = new VectorArray<Loc>(Constants.NSUB);
	Vector<Loc>   tmp_pos = new Vector<Loc>();
	double   mq = 0.0;
	DoubleArray<Loc> mrs = new DoubleArray<Loc>(Constants.NSUB);

	switch (node) instanceof {
	    case Cell<G>:
		for each i in node.subp pardo {
			if (node.subp[i] != null) {
			    tmpv[i] = new Vector<Loc>();
			    mrs[i] = 
				CenterOfMass.<refgroup G>hackcofm(node.subp[i]);
			    /* find moment */
			    tmpv[i].MULVS(node.subp[i].pos, mrs[i]);
			}
		    }
		for each i in tmpv {
			/* sum tot. moment */
			if (tmpv[i] != null)
			    tmp_pos.ADDV(tmp_pos, tmpv[i]);
			mq = mrs[i] + mq;
		    }
	
		node.mass = mq;
		/* rescale cms position */
		node.pos.DIVVS(tmp_pos, node.mass);
	    case Body:
		mq = node.mass;
	    }

        return mq;
    }


}