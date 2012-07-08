package com.sun.tools.javac.code;

import com.sun.tools.javac.code.RPLElement.RPLCaptureParameter;
import com.sun.tools.javac.code.RPLElement.RPLParameterElement;
import com.sun.tools.javac.code.RPLElement.UndetRPLParameterElement;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Translation.AsMemberOf;
import com.sun.tools.javac.code.Translation.AtCallSite;
import com.sun.tools.javac.code.Translation.SubstRPLs;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/** A class for representing DPJ region path lists (RPLs).  An RPL is a list
 *  of RPL elements.  Various operations on RPLs, pairs of RPLs, and lists
 *  of RPLs required by the DPJ type system are supported.
 */
public class RPL 
	implements
	SubstRPLs<RPL>,
	AsMemberOf<RPL>,
	AtCallSite<RPL>
{
    
    /** No RPL */
    public static final RPL NONE = new RPL();
    
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    /** The elements comprising this RPL */
    public List<RPLElement> elts;
        
    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public RPL() {
	this.elts = List.nil();
    }
    
    public RPL(RPLElement singletonElement) {
	this.elts = List.of(singletonElement);
    }
    
    public RPL(List<RPLElement> elts) {
	this.elts = elts;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // RPL query methods
    ///////////////////////////////////////////////////////////////////////////
    
    public int size() {
	return elts.size();
    }
    
    public boolean isEmpty() {
	return elts.isEmpty();
    }

    /** RPL under relation
     *  See Section 1.2.2 of the DPJ Tech Report
     */
    public boolean isNestedUnder(RPL that) {
	// Deal capture parameters by converting first element
	// to its upper bound
	RPL upperBound = this.upperBound();
	if (upperBound != this && upperBound.isNestedUnder(that))
	    return true;
	// UNDER-ROOT
	if (that.isRoot()) return true;
	// UNDER-NAME
	if (!this.isEmpty()) {
	    if (this.withoutLastElement().isNestedUnder(that)) return true;
	}
	// UNDER-STAR
	if (this.endsWithStar() && this.withoutLastElement().isNestedUnder(that)) return true;
	// UNDER-INCLUDE
	if (this.isIncludedIn(that)) return true;
	return false;
    }
    
    /**
     * Does this RPL start with a local region name, or is it under an RPL
     * that starts with a local region name?
     */
    public boolean isUnderLocal() {
	RPL upperBound = this.upperBound();
	if (upperBound != this && upperBound.isUnderLocal())
	    return true;
	if (elts.isEmpty() || elts.head == null)
	    return false;
	return elts.head.isLocalName();
    }

    /** RPL inclusion relation
     *  See Section 1.2.3 of the DPJ Tech Report
     */
    public boolean isIncludedIn(RPL that) {
	// Handle undetermined parameters
	if (that.elts.head instanceof UndetRPLParameterElement) {
	    UndetRPLParameterElement element = 
		(UndetRPLParameterElement) that.elts.head;
	    if (element.includedIn==null) {
		element.includedIn = this;
		return true;
	    }
	    else if (this.isIncludedIn(element.includedIn))
		return true;
	    return false;
	}
	// Handle capture parameters
	if (this.elts.head instanceof RPLCaptureParameter) {
	    return this.upperBound().isIncludedIn(that);
	}
	// Reflexivity
	if (this.equals(that)) return true;
	// INCLUDE-STAR
	if (that.endsWithStar()) {
	    if (this.isNestedUnder(that.withoutLastElement())) return true;
	}
	// INCLUDE-NAME
	if (!this.isEmpty() && !that.isEmpty()) {
	    if (this.elts.last().isIncludedIn(that.elts.last()) && 
		    this.withoutLastElement().isIncludedIn(that.withoutLastElement()))
		return true;
	}
	return false;
    }
    
    private boolean endsWithStar() {
	return size() > 1 && elts.last() == RPLElement.STAR;
    }
    
    private boolean isRoot() {
	return size() == 1 && (elts.last() == RPLElement.ROOT_ELEMENT);
    }
    
    /**
     * Is this RPL fully specified?  
     * An RPL is fully specified if it contains no *.
     */
    
    public boolean isFullySpecified() {
	for (RPLElement e : elts) {
	    if (!e.isFullySpecified()) return false;
	}
	return true;
    }
    
    /**
     * Compute an upper bound for this RPL
     */
    public RPL upperBound() {
	if (elts.isEmpty() ||
		!(elts.head instanceof RPLCaptureParameter))
	    return this;
	RPL upperBound = elts.head.upperBound();
	return new RPL(upperBound.elts.appendList(elts.tail));
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // RPL manipulation methods
    ///////////////////////////////////////////////////////////////////////////
    
    private RPL withoutLastElement() {
	ListBuffer<RPLElement> buf = new ListBuffer<RPLElement>();
	List<RPLElement> elts = this.elts;
	while (elts.tail != null) {
	    if (elts.tail.tail == null) break;
	    buf.append(elts.head);
	    elts = elts.tail;
	}
	return new RPL(buf.toList());
    }
    
    public RPL substRPLs(List<RPL> from, List<RPL> to) {
	while (from.nonEmpty() && to.nonEmpty()) {
	    if (this.elts.head.equals(from.head.elts.head)) {
		return new RPL(to.head.elts.appendList(this.elts.tail));
	    }
	    from = from.tail;
	    to = to.tail;
	}
	return this;
    }
    
    /** Compute the capture of an RPL:
     *  - If the RPL is fully specified, then the capture is the same as the input
     *  - If the RPL is partially specified, then the capture is a fresh RPL consisting
     *    of a fresh capture parameter under the input RPL
     */
    
    public RPL capture() {
	return this.isFullySpecified() ? this : 
	    new RPL(new RPLElement.RPLCaptureParameter(this));
    }
    
    /**
     * The RPL as a member of t
     * @param t     The type where we want this to be a member, after translation
     * @param owner The symbol associated with the class where this was defined
     */
    public RPL asMemberOf(Types types, Type t, Symbol owner) {
	RPL result = this;
	if (owner.type.hasRegionParams()) {
            Type base = types.asOuterSuper(t, owner);
            if (base != null) {
                List<RPL> from = owner.type.allrgnparams();
                List<RPL> to = base.allrgnactuals();
                if (from.nonEmpty()) {
                    result = result.substRPLs(from, to);
                }
            }
        }
	return result;
	
    }
    
    public RPL asMemberOf(Types types, Type t) {
	RPLElement elt = this.elts.head;
	if (elt instanceof RPLParameterElement) {
	    RPLParameterElement paramElt = 
		    (RPLParameterElement) elt;
	    Symbol owner = paramElt.sym.owner;
	    return this.asMemberOf(types, t, owner);
	}
	return this;
    }

    public RPL atCallSite(Resolve rs, Env<AttrContext> env, 
	    JCMethodInvocation tree) {
	MethodSymbol methSym = tree.getMethodSymbol();
	if (methSym != null) {
	    MethodType methodType = (MethodType) tree.meth.type;
	    RPL rpl = Translation.<RPL>accessElt(this, 
		    rs.getTypes(), tree.meth, env);
	    if (methSym.rgnParams != null)
		rpl = rpl.substRPLs(methSym.rgnParams, 
			methodType.regionActuals);
	    return rpl;
	}
	return this;
    }
    
    public RPL atNewClass(Resolve rs, Env<AttrContext> env, 
	    JCNewClass tree) {
	MethodSymbol methSym = tree.getMethodSymbol();
	if (methSym != null) {
	    ListBuffer<RPL> rpls = ListBuffer.lb();
	    for (DPJRegionPathList rpl : tree.regionArgs)
		rpls.append(rpl.rpl);
	    return this.substRPLs(methSym.rgnParams, rpls.toList());
	}
	return this;
    }    

    /**
     * Conform the RPL to an enclosing environment.  An RPL may contain 
     * elements written in terms of local region names and/ or local variables
     * that are no longer in scope.  If so, we need to either (1) replace the RPL 
     * with a more general RPL whose elements are in scope; or (2) delete the
     * RPL (i.e., return NONE), if all regions it represents are out of scope.
     */
    public RPL inEnvironment(Resolve rs, Env<AttrContext> env, boolean pruneLocalEffects) {
	// If the RPL is a capture parameter, compute its bound in the enclosing
	// environment
	if (elts.head instanceof RPLCaptureParameter) {
	    RPLCaptureParameter capture = (RPLCaptureParameter) elts.head;
	    RPL includedIn = capture.includedIn.inEnvironment(rs, env, pruneLocalEffects);
	    // If bound is out of scope, so is capture parameter
	    if (includedIn == RPL.NONE) return RPL.NONE;
	    // Otherwise return new parameter only if bound changed
	    return (includedIn == capture.includedIn) ? this :
		new RPL(List.<RPLElement>of(new RPLCaptureParameter(includedIn)).
			appendList(elts.tail));
	}	
	// Truncate an RPL containing an element E that is out of scope.  If
	// E occurs in the first position, then the whole RPL is out of scope; return null.  
	// Otherwise, replace E and all following elements with *.
	for (RPLElement elt : elts) {
	    if (!rs.isInScope(elt, env))
		return this.truncateTo(elt);
	    if (pruneLocalEffects && elt.isLocalName())
		return this.truncateTo(elt);
	}
	return this;
    }
    
    private RPL truncateTo(RPLElement elt) {
	ListBuffer<RPLElement> buf = ListBuffer.lb();
	List<RPLElement> list = elts;
	while (list.nonEmpty() && list.head != elt) {
	    buf.append(list.head);
	    list = list.tail;
	}
	if (buf.isEmpty()) return RPL.NONE;
	buf.append(RPLElement.STAR);
	return new RPL(buf.toList());
    }
    
    ///////////////////////////////////////////////////////////////////////////

    @Override public boolean equals(Object other) {
	if (this == other)
	    return true;
	else if (other != null && other instanceof RPL)
	    return this.elts.equals(((RPL)other).elts);
	else
	    return false;
    }
  
    @Override public int hashCode() {
	return elts.hashCode();
    }
    
    ///////////////////////////////////////////////////////////////////////////

    @Override public String toString() {
	StringBuilder sb = new StringBuilder();
	boolean first = true;
	for (RPLElement e : elts) {
	    if (first) first = false; else sb.append(" : ");
	    sb.append(e);
	}
	return sb.toString();
    }

    /**
     * The Java source which this RPL list represents.  A List is
     * represented as a comma-separated listing of the elements in
     * that list.
     */
    public static String toString(java.util.List<RPL> rpls) {
	return rpls.toString();
    }
    
}
