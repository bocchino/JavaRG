package com.sun.tools.javac.code;

import static com.sun.tools.javac.code.Kinds.MTH;

import com.sun.tools.javac.code.Symbol.RegionNameSymbol;
import com.sun.tools.javac.code.Symbol.RegionParameterSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.tree.JCTree.DPJNegationExpression;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

/**
 * A class to represent RPL elements.  There is one inner class for each type of
 * RPL element.
 * 
 * @author Rob Bocchino
 */

public abstract class RPLElement {

    // THE RPLElement API
    
    /**
     * Whether the element is fully specified (i.e., contains no *).
     */
    public boolean isFullySpecified() { return true; }
    
    /**
     * Whether the element is a local region name
     */
    public boolean isLocalName() { return false; }

    /**
     * Whether the element is included in another element.  Used in determining
     * inclusion of RPLs.
     */
    public boolean isIncludedIn(RPLElement e) {
	return this.equals(e) || e == STAR;	
    }

    /**
     * Whether the element is disjoint from another element, given constraints.
     * Used in determining disjointness of RPLs.
     */
    public boolean isDisjointFrom(RPLElement e, RPLs rpls,
	    List<Pair<RPL,RPL>> constraints) {
	return !this.equals(e);
    }
    
    /**
     * An RPL that bounds this element from above, used in the disjointness test.
     */
    public RPL upperBound() {
	return new RPL(this);
    }

    /**
     * The symbol corresponding to this RPL element, if any
     */
    public Symbol getSymbol() { return null; }
    
    // SINGLE-INSTANCE CLASSES
    
    /** The RPL element Root.
     */
    public static final RPLElement ROOT_ELEMENT = new RPLElement() {
        @Override public String toString() {
            return "Root";
        }
        public boolean isDisjointFrom(RPLElement e, RPLs rpls, 
        	List<Pair<RPL,RPL>> constraints) {
            if (e instanceof RPLCaptureParameter) {
        	return rpls.areDisjoint(((RPLCaptureParameter) e).includedIn, 
        		RPLs.ROOT, constraints);
            }
            return e != ROOT_ELEMENT;
        }
    };

    /**
     * The RPL element *.
     */
    public static final RPLElement STAR = new RPLElement() {
	@Override public String toString() {
	    return "*";
	}
	public boolean isFullySpecified() { return false; }
	public boolean isDisjointFrom(RPLElement e, RPLs rpls,
		List<Pair<RPL,RPL>> constraints) {
	    return false;
	}
    };

    // MULTIPLE-INSTANCE CLASSES
    
    /** A name RPL element
     */
    public static class NameRPLElement extends RPLElement {

	/**
	 * The region name symbol representing the name of this element
	 */
	public final RegionNameSymbol sym;

	@Override
	public Symbol getSymbol() { return sym; }
	
	public NameRPLElement(RegionNameSymbol sym) {
	    this.sym = sym;
	}

	@Override
	public boolean isLocalName() {
	    return (sym.owner.kind == MTH);    
	}
	
	public boolean isDisjointFrom(RPLElement e, RPLs rpls,
		List<Pair<RPL, RPL>> constraints) {
	    if (e instanceof NameRPLElement) {
		return this.sym != ((NameRPLElement)e).sym;
	    }
	    return e.isDisjointFrom(this, rpls, constraints);
	}
	
	public String toString() {
	    return sym.toString();
	}
	
	public boolean equals(Object o) {
	    if (o instanceof NameRPLElement) {
		return this.sym == ((NameRPLElement) o).sym;
	    }
	    return false;
	}
    }
    
    /** An RPL parameter element
     */
    public static class RPLParameterElement extends RPLElement {

	/**
	 * The region parameter symbol of this element
	 */
	public RegionParameterSymbol sym;

	@Override
	public Symbol getSymbol() { return sym; }
	
	/** RPL in which this parameter is included -- used for capture parameters
         */
        public RPL includedIn;

        public RPLParameterElement(RegionParameterSymbol sym, RPL includedIn) {
	    this.sym = sym;
	    this.includedIn = includedIn;
	}

        public RPLParameterElement(RegionParameterSymbol sym) {
            this.sym = sym;
        }
        
	public boolean isDisjointFrom(RPLElement e, RPLs rpls,
		List<Pair<RPL, RPL>> constraints) {
	    return false;
	}

	@Override
	public boolean equals(Object o) {
	    if (o instanceof RegionParameterSymbol)
		return this.sym == o;
	    if (o instanceof RPLParameterElement)
		return this.sym == ((RPLParameterElement) o).sym;
	    return false;
	}
	
	@Override
	public String toString() {
	    return sym.toString();
	}
    }

    // INTERNAL-USE RPL ELEMENTS
    // These elements are used by the type checker but do not correspond to
    // anything in the programmer-visible DPJ syntax.
    
    /**
     * Capture parameter of an RPL.  When a type is captured, its partially
     * specified RPLs turn into these parameters.  See the tech report for
     * more details.
     */
    public static class RPLCaptureParameter extends RPLElement {
	/**
	 * RPL this parameter is included in
	 */
	public RPL includedIn;

	public RPLCaptureParameter(RPL includedIn) {
	    this.includedIn = includedIn;
	}
	
	@Override
	public RPL upperBound() {
	    return includedIn;
	}
	
	public String toString() {
	    return "capture of (" + includedIn.toString() + ")";
	}
    }
    
    /** A class for undetermined region parameters, used during parameter inference
     *  in resolving call sites.
     */
    public static class UndetRPLParameterElement extends RPLParameterElement {
	
        public UndetRPLParameterElement(RegionParameterSymbol sym) {
            super(sym, new RPL(List.<RPLElement>of(RPLElement.ROOT_ELEMENT, 
        	    RPLElement.STAR)));
        }
	
	public String toString() {
	    return sym + "? under " + includedIn;
	}

    }
    
    /**
     * A region on the stack.  Used to make sure that stack accesses don't
     * interfere across parallel sections.
     */
    public static class StackRPLElement extends RPLElement {

	/**
	 * Stack variable that this RPL element represents
	 */
	public VarSymbol sym;
	
	@Override
	public Symbol getSymbol() { return sym; }
	
	public static RPL RPL(Symtab syms, VarSymbol sym) {
	    return new RPL(List.<RPLElement>of(new StackRPLElement(sym)));
	}

	public StackRPLElement(VarSymbol sym) {
	    this.sym = sym;
	}
	
	public boolean equals(Object o) {
	    if (!(o instanceof StackRPLElement)) return false;
	    return this.sym == ((StackRPLElement) o).sym;
	}
	
	public String toString() {
	    return "stack region of " + sym;
	}
	
	@Override
	public boolean isLocalName() { return true; }
    }

}
