/**
 * A simple disjoint tree class.
 */
public class Tree<region R,refgroup G> {

    private final int arity;
    private Node root in R:Rep;

    public int getArity() pure {
	return arity;
    }

    public Tree(int arity) pure {
	this.arity = arity;
    }

    private region Rep;

    public interface NodeExpander {
        /**
         * Method to choose in which slot to insert nextValue in
         * thisNode at level 'level' in the tree.  This method must
         * return a value i such that 0 <= i < arity which says create
         * a new inner node and insert as ith child of the new node
         */
        public <region R>int indexToExpand(final Data<R> thisNodeData,
					   final Data<R> parentNodeData,
					   final Data<R> nextValue) pure;
	/**
	 * Method to create a new object for an inner tree node at level 'level'.
	 * Region 'R' in T<R> ensures that the object must be a fresh object.
	 */
	public <region R,refgroup G> unique(G) Data<R> 
	    nodeFactory(Data<R> thisNodeData,
			Data<R> parentNodeData,
			int idxInParent,
			Data<R> nextValue) pure;
    }

    private arrayclass RepArray {
	unique(G) Node in R:Rep;
    }

    public interface NodeInterface<region R> {
	public Data<R> getData();
    }

    private class Node 
	implements NodeInterface<R>
    {
        unique(G) Data<R> data in R:Rep;

        public Node(unique(G) Data<R> data) pure {
            this.data = data;
        }

        public void setData(unique(G) Data<R> data) 
	    writes R:Rep 
	{ 
	    this.data = data; 
	}

        public Data<R> getData()        
	    reads R:Rep  
	{ 
	    return this.data; 
	}
    }

    private class InnerNode
	extends Tree<R,G>.Node
	implements NodeInterface<R>
    {
        public unique(G) RepArray children in R:Rep;
        
        public InnerNode(unique(G) Data<R> data) 
	    pure 
	{
            super(data);
            children = new RepArray(arity);
        }
        
	public unique(G) Node getChild(final int idx) 
	    writes R:Rep 
	{
	    return !children[idx];
	}
	
	public void setChild(final int idx, unique(G) Node node)
	    writes R:Rep
	{
	    children[idx] = node;
	}
    }
    
    private class LeafNode
	extends Node
	implements NodeInterface<R>
    {
        public LeafNode(unique(G) Data<R> data)
	    pure
	{
            super(data);
        }
    }

    /**
     * @method buildTree: Build a tree at all inner nodes.
     * @param  elts     : The objects to insert as leaves of the tree.
     * @param  expander : The logic to choose where to insert each object.
     */
    public <refgroup AG>void buildTree(UniqueDataArray<R,AG> elts,
				       NodeExpander expander)
	copies elts...AG to G
    {
	unique(G) Node root = null;
	unique(G) Node lastNode1 = null;
	unique(G) Node lastNode2 = null;
        for each i in elts {
		unique(G) Data<R> next = elts[i];
		root = this.buildTreeHelper(next, expander,
					    root, lastNode2, -1);
		lastNode2 = lastNode1;
		lastNode1 = root;
        }
	this.root = root;
    }

    private unique(G) Node
	buildTreeHelper(unique(G) Data<R> elt,
			NodeExpander expander,
			unique(G) NodeInterface<R> thisNode, 
			Node parentNode,
			int idxInParent) 
	writes R:Rep
    {
	// 1. null:  create and return new leaf
	if (thisNode == null) {
            return new LeafNode(elt);
	}
        
        unique(G) InnerNode result = null;
        Node newParent = null;

	if (thisNode instanceof LeafNode) {
	    switch (thisNode) instanceof {
		case LeafNode: {
		    // 2. Leaf node: Must be non-empty:
		    //    (a) Create new InnerNode; this is node to return.
		    //    (b) Ask factory for a fresh object to go in that node, if any.
		    //    (c) Ask which slot to use for old node; insert it in that slot.
		    assert(thisNode.getData() != null);
		    unique(G) Data<R> newObject = 
			expander.<region R,refgroup G>nodeFactory(thisNode.getData(),
								  parentNode==null? null: parentNode.getData(),
								  idxInParent, elt);
		    unique(G) InnerNode newInner =
			new InnerNode(newObject);
		    int ci = expander.indexToExpand(newObject,
						    parentNode==null? null : parentNode.getData(),
						    thisNode.getData());
		    assert(0 <= ci && ci < arity);
		    newInner.setChild(ci, thisNode);
		    result = newInner;
		    newParent = parentNode;
		}
		}
	}
	else {
	    assert (thisNode instanceof InnerNode);
	    switch (thisNode) instanceof {
		case InnerNode: {
		    // 3. Inner node:
		    //    (a) This node is result
		    //    (b) Ask factory for a fresh object to go in that node, if any.
		    unique(G) Data<R> newObject = 
			expander.<region R,refgroup G>nodeFactory(thisNode.getData(),
								  parentNode==null? null : parentNode.getData(),
								  idxInParent, elt);
		    thisNode.setData(newObject);
		    result = (InnerNode) ((Object)thisNode);
		    newParent = parentNode;
		}
		}
	}
	// 4. Ask which slot to use for new object
	final int cj = expander.indexToExpand(result.getData(),
					      newParent==null? null : newParent.getData(),
					      elt);
	assert(0 <= cj && cj < arity);
        
	// 5. Recursively call self to build new subtree at child cj,
        //    inserting 'elt' in that subtree.
        final unique(G) Node newTree =
            buildTreeHelper(elt, expander, result.getChild(cj), result, cj);
        
	// 6. Insert new subtree in slot computed in step 4.
        result.setChild(cj, newTree);
        
	// 7. Return result
        return result;
    }

    region Result;

    private static arrayclass ResultArray<region R1,R2> {
	Data<R1:Result> in R2;
    }

    public static abstract class POVisitor {
        public abstract <region R1,R2 | R1:* # R2:*>Data<R1:Result>
	    visit(Data<R1> curNodeData, ResultArray<R1,R2> results)
	    reads R1:Result,R2 writes R1 via curNodeData;
    }

    /**
     * Traverse tree in postorder and apply given operation to each
     * node, passing in the results of the children as a ResultArray.
     */
    public Data<R:Result> parallelPO(POVisitor visitor) {
	return this.parallelPORecursive(visitor, root);
    }

    private Data<R:Result>
	parallelPORecursive(POVisitor visitor, NodeInterface<R> subtree)
	reads R:Rep,R:Result writes R via subtree...G
    {
	if (subtree == null)
	    return null;

	region Loc;

	ResultArray<R,Loc> results = null;

	switch (subtree) instanceof {
	    case InnerNode:
		// Recursively visit the children in parallel
		results = new ResultArray<R,Loc>(arity);
	    
		for each i in results pardo {
			// This causes interference but it shouldn't
			// I removed array index regions from JavaRG but we need them here!
			results[i] =
			    // Ref groups establish no interference here
			    parallelPORecursive(visitor, subtree.children[i]);
		    }
	    }

	Data<R:Result> result = null;
	switch (subtree) instanceof {
	    case Node:
		// Visit current node, passing in children's result.
		// Return the result of this postorder visit.
		result = visitor.<region R,Loc>
		    visit(subtree.data, results);
	    }

	return result;
    }

}
