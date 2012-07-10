package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Permission.RefPerm;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Translation.AsMemberOf;
import com.sun.tools.javac.code.Translation.AtCallSite;
import com.sun.tools.javac.code.Translation.SubstRefGroups;
import com.sun.tools.javac.code.Translation.SubstVars;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree.DPJNegationExpression;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

/**
 * Class representing a dereference set e [...G]
 * @author Rob Bocchino
 *
 */

public class DerefSet implements
	SubstRefGroups<DerefSet>,
	AsMemberOf<DerefSet>,
	AtCallSite<DerefSet>,
	SubstVars<DerefSet>
{
    
    /**
     * Object representing no deref set
     */
    public static final DerefSet NONE = new DerefSet();
    
    /** 
     * 'e' in 'e [...G]'
     */
    public final JCExpression exp;
    
    /** 
     * 'G' in 'e [...G]'
     * If this field is NO_GROUP, then there is no tree group.
     */
    public final RefGroup treeGroup;

    public Resolve rs;

    public DerefSet() {
	this.exp = null;
	this.treeGroup = RefGroup.NONE;
	this.rs = null;
    }
    
    public DerefSet(JCExpression exp, RefGroup treeGroup, Resolve rs) {
	this.exp = addImplicitThis(exp, rs);
	this.treeGroup = treeGroup;
	this.rs = rs;
    }
    
    public DerefSet(JCExpression exp, Resolve rs) {
	this(exp, RefGroup.NONE, rs);
    }
    
    public boolean isTreeSet() {
	return treeGroup != RefGroup.NONE;
    }
    
    public RefGroup refGroup(Attr attr, Env<AttrContext> env) {
	if (this.treeGroup != RefGroup.NONE)
	    return this.treeGroup;
	if (this.exp != null) {
	    RefGroup rg = attr.getRefGroupFor(this.exp, env);
	    if (rg != null) return rg;
	}
	return RefGroup.NONE;
    }
    
    
    /**
     * Add implicit 'this' to front of exp
     */
    public JCExpression addImplicitThis(JCExpression exp, Resolve rs) {	
	TreeMaker maker = rs.getTreeMaker();
	if (exp instanceof JCIdent) {
	    JCIdent id = (JCIdent) exp;
	    if ((id.sym.owner != null) && (id.sym.owner.thisSym != null)) {
		if (!id.name.equals(rs.getNames()._this)) {
		    Symbol thisSym = id.sym.owner.thisSym;
		    JCExpression thisExp = maker.Ident(thisSym);
		    JCExpression result = maker.Select(thisExp, id.sym);
		    return result;
		}
	    }
	    return exp;
	}
	else if (exp instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) exp;
	    JCExpression newExp = addImplicitThis(fa.selected, rs);
	    return (newExp == exp) ? exp : maker.Select(newExp, fa.sym);	    
	}
	else if (exp instanceof JCArrayAccess) {
	    JCArrayAccess aa = (JCArrayAccess) exp;
	    JCExpression newExp = addImplicitThis(aa.indexed, rs);
	    return (newExp == exp) ? exp : maker.Indexed(newExp, aa.index);	    	    
	}
	return exp;
    }
    
    public boolean isThis() {
	return exp != null && exp.getSymbol().toString().equals("this");
    }
    
    /**
     * Does this dereference set represent a chain of non-final unique permissions?
     */
    public boolean isUniqueChain(Attr attr, Env<AttrContext> env) {
	return isUniqueChainHelper(this.exp, attr, env);
    }
    // where
    private boolean isUniqueChainHelper(JCExpression exp, 
	    Attr attr, Env<AttrContext> env) {
	if (exp == null)
	    return false;
	if (exp.getSymbol() == null) {
	    return false;
	}
	if ((exp.getSymbol().flags() & Flags.STATIC) != 0)
	    return false;
	RefGroup group = attr.getRefGroupFor(exp, env);
	if (group != RefGroup.UNIQUE) 
	    return false;
	if (exp instanceof JCIdent)
	    return true;
	if (exp instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) exp;
	    return isUniqueChainHelper(fa.selected, attr, env);
	}
	return false;
    }
    
    /**
     * Is this a valid dereference set?  Requirements:
     * 1. Syntax:  Must be id or sequence of selects or array accesses
     * 2. Ref groups must match along path
     */    
    public boolean isValid(Attr attr, Env<AttrContext> env, Types types) {
	return isValidHelper(exp, treeGroup, attr, env, types);
    }
    // where
    private boolean isValidHelper(JCExpression exp, RefGroup tg, 
	    Attr attr, Env<AttrContext> env, Types types) {
	Resolve rs = attr.rs;
	if (exp instanceof JCIdent) {
	    // Identifier must be in scope
	    if (!rs.isInScope(exp, env))
		return false;
	    RefGroup group = attr.getRefGroupFor(exp, env);
	    // Groups must be in scope
	    if (!rs.isInScope(group, env) || !rs.isInScope(tg, env))
		return false;
	    // Groups must match
	    if (group == RefGroup.UNIQUE || group == RefGroup.NONE)
		return true;
	    return group.equals(tg);
	}
	if (exp instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) exp;
	    // Groups must match
	    RefGroup group = attr.getRefGroupFor(exp, env);
	    if (!checkGroup(attr.rs, env, tg, group)) {
		return false;
	    }
	    // Selected expression must be valid
	    return isValidHelper(fa.selected, group, attr, env, types);
	}
	if (exp instanceof JCArrayAccess) {
	    JCArrayAccess aa = (JCArrayAccess) exp;
	    // Indexed expression must be JRG array class, not Java array
	    if (!types.isArrayClass(aa.indexed.type))
		return false;
	    // Groups must match
	    RefGroup group = attr.getRefGroupFor(exp, env);
	    if (!checkGroup(attr.rs, env, tg, group))
		return false;
	    // Indexed expression must be valid
	    return isValidHelper(aa.indexed, group, attr, env, types);
	}
	return false;
    }

    /**
     * Is this deref set disjoint from other?
     */
    private JCExpression e1;
    private JCExpression e2;
    public boolean isDisjointFrom(DerefSet other, 
	    Env<AttrContext> env) 
    {
	e1 = this.exp;
	e2 = other.exp;
	equalizeExpLens();
	return areDisjointExps(e1,e2,env);
    }
    // where
    private boolean areDisjointExps(JCExpression e1, JCExpression e2,
	    Env<AttrContext> env) {
	if (e1 instanceof JCFieldAccess
		&& e2 instanceof JCFieldAccess) {
	    return areDisjointFields((JCFieldAccess) e1, 
		    (JCFieldAccess) e2, env);	    
	}
	else if (e1 instanceof JCArrayAccess
		&& e2 instanceof JCArrayAccess) {
	    return areDisjointCells((JCArrayAccess) e1, 
		    (JCArrayAccess) e2, env);
	}
	return false;	
    }
    private void equalizeExpLens() {
	int len1 = lengthOf(e1);
	int len2 = lengthOf(e2);
	if (len1 > len2)
	    for (int i = 0; i < len1 - len2; ++i)
		e1 = peel(e1);
	else if (len2 > len1)
	    for (int i = 0; i < len2 - len1; ++i)
		e2 = peel(e2);		   
    }
    private int lengthOf(JCExpression e) {
	JCExpression peeled = peel(e);
	if (peeled != e)
	    return 1+lengthOf(peeled);
	return 1;
    }
    private JCExpression peel(JCExpression e) {
	if (e instanceof JCFieldAccess)
	    return ((JCFieldAccess) e).selected;
	else if (e instanceof JCArrayAccess)
	    return ((JCArrayAccess) e).indexed;
	return e;
    }    
    private boolean areDisjointFields(JCFieldAccess fa1, JCFieldAccess fa2,
	    Env<AttrContext> env) {
	if (areDisjointExps(fa1.selected, fa2.selected, env))
	    return true;
	// Selected symbols must be the same
	if (fa1.getSymbol() != fa1.getSymbol()) return false;
	// Field names must be different
	if (fa1.sym == fa2.sym) return false;
	// Groups must match
	return matchingGroups(fa1, fa2, env);
    }
    private boolean areDisjointCells(JCArrayAccess aa1, JCArrayAccess aa2,
	    Env<AttrContext> env) {
	if (areDisjointExps(aa1.indexed, aa2.indexed, env))
	    return true;
	// Indices must be different
	if (!areNeverEqualExprs(aa1.index, aa2.index)) return false;
	// Groups must match
	return matchingGroups(aa1, aa2, env);
    }
    private boolean matchingGroups(JCExpression e1, JCExpression e2,
	    Env<AttrContext> env) {
	RefGroup g1 = rs.getAttr().getRefGroupFor(e1, env);
	RefGroup g2 = rs.getAttr().getRefGroupFor(e2, env);
	if (g1 == RefGroup.NONE || g2 == RefGroup.NONE)
	    return false;
	if (g1 == RefGroup.UNIQUE || g2 == RefGroup.UNIQUE)
	    return false;
	return g1.equals(g2);

    }
    
    public static boolean areNeverEqualExprs(JCExpression first, JCExpression second) {
        if (first instanceof JCLiteral && second instanceof JCLiteral) {
            return !((JCLiteral) first).getValue().equals(((JCLiteral) second).getValue());
        }
        if (first instanceof DPJNegationExpression) {
            return areNeverEqualExprs(second, first);
        } else if (!(first instanceof DPJNegationExpression) &&
                second instanceof DPJNegationExpression){
            if (first.getSymbol() == second.getSymbol())
                return true;
        }
        return false;
    }

    /**
     * Is this deref set included in d?
     */
    public boolean isIncludedIn(DerefSet d, 
	    Env<AttrContext> env, Resolve rs) {
	// None isn't included in anything and doesn't include anything
	if (d==DerefSet.NONE || this==DerefSet.NONE)
	    return false;
	// Check whether the expression is unique
	RefGroup expGroup = rs.getAttr().getRefGroupFor(this.exp, env);
	boolean uniqueExp = (expGroup == RefGroup.UNIQUE);
	// If this.exp is ident...
	if (this.exp instanceof JCIdent) {
	    // d.exp must be ident
	    if (!(d.exp instanceof JCIdent))
		return false;
	    // this.exp and d.exp must have the same symbol
	    if (!this.exp.getSymbol().equals(d.exp.getSymbol()))
		return false;
	    // If this has a tree group, then the groups must match
	    if (this.treeGroup != RefGroup.NONE && this.treeGroup != RefGroup.UNIQUE)
		return this.treeGroup.equals(d.treeGroup);
	    return true;
	}
	// Otherwise if this.exp is field access e1.f1...
	else if (this.exp instanceof JCFieldAccess) {
	    // If symbols are the same, inclusion holds
	    if (this.exp.getSymbol()==d.exp.getSymbol())
		return true;
	    // Otherwise peek into the selected exp e1
	    JCFieldAccess fa1 = (JCFieldAccess) this.exp;
	    RefGroup group1 = rs.getAttr().getRefGroupFor(fa1, env);
	    DerefSet d1 = new DerefSet(fa1.selected, group1, rs);
	    // exp must be unique, or d must have a tree group
	    if (!uniqueExp && (d.treeGroup == RefGroup.NONE))
		return false;
	    // Inclusion must hold for e1
	    return d1.isIncludedIn(d, env, rs);
	}
	// Array access is analogous to field access
	else if (this.exp instanceof JCArrayAccess) {
	    if (this.exp.getSymbol()==d.exp.getSymbol())
		return true;
	    JCArrayAccess aa1 = (JCArrayAccess) this.exp;
	    RefGroup group1 = rs.getAttr().getRefGroupFor(aa1, env);
	    DerefSet d1 = new DerefSet(aa1.indexed, group1, rs);
	    if (!uniqueExp && (d.treeGroup == RefGroup.NONE))
		return false;
	    return d1.isIncludedIn(d, env, rs);		
	}    
	// Otherwise we don't know what to do (should never happen)
	return false;
    }
    
    /**
     * Check whether deref set is a valid tree root
     */
    public boolean isTreeRoot(Attr attr, Env<AttrContext> env) {
	return isTreeRootHelper(this.exp, attr, env);
    }
    // where
    private boolean isTreeRootHelper(JCExpression exp, Attr attr,
	    Env<AttrContext> env) {
	if (exp instanceof JCIdent) {
	    RefPerm perm = attr.getRefPermFor(exp, env);
	    // Leftmost variable must be locally unique
	    return perm.isLocallyUnique();
	}
	else if (exp instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) exp;
	    return isTreeRootHelper(fa.selected, attr, env);
	}
	else if (exp instanceof JCArrayAccess) {
	    JCArrayAccess aa = (JCArrayAccess) exp;
	    return isTreeRootHelper(aa.indexed, attr, env);
	}
	return false;
    }
    
    /**
     * Check upper against lower group in tree:
     * 1. Groups must be in scope
     * 2. If upper == NONE or lower == UNIQUE, then OK
     * 3. Otherwise upper must match lower
     */
    private boolean checkGroup(Resolve rs, Env<AttrContext> env,
	    RefGroup upper, RefGroup lower) {
	if (upper == RefGroup.NONE || lower == RefGroup.UNIQUE)
	    return true;
	if (!rs.isInScope(upper, env) || !rs.isInScope(lower, env))
	    return false;
	return upper.equals(lower);
    }
    
    public DerefSet substRefGroups(List<RefGroup> from, 
	    List<RefGroup> to) {
	RefGroup treeGroup = 
		this.treeGroup.substRefGroups(from, to);
	if (this.treeGroup != treeGroup)
	    return new DerefSet(this.exp, treeGroup, rs);
	return this;
    }
    
    public DerefSet asMemberOf(Types types, Type t) {
	RefGroup treeGroup = this.treeGroup.asMemberOf(types, t);
	if (this.treeGroup != treeGroup)
	    return new DerefSet(this.exp, treeGroup, rs);
	return this;
    }
    
    public DerefSet atCallSite(Resolve rs, Env<AttrContext> env,
	    JCMethodInvocation tree) {
	RefGroup treeGroup = this.treeGroup.atCallSite(rs, 
		env, tree);
	DerefSet result = this;
	if (this.treeGroup != treeGroup)
	    result = new DerefSet(this.exp, treeGroup, rs);
	MethodSymbol methSym = tree.getMethodSymbol();
	if (methSym != null) {
            result = result.substVars(rs.getPermissions(), 
        	    methSym.params(), tree.args);
            result = Translation.substForThis(result, tree.meth,
        	    rs, env, methSym.owner.thisSym);
	}
	return result;
    }

    public DerefSet atNewClass(Resolve rs, Env<AttrContext> env,
	    JCNewClass tree) {
	RefGroup treeGroup = this.treeGroup.atNewClass(rs, 
		env, tree);
	DerefSet result = this;
	if (this.treeGroup != treeGroup)
	    result = new DerefSet(this.exp, treeGroup, rs);
	MethodSymbol methSym = tree.getMethodSymbol();
	if (methSym != null) {
            result = result.substVars(rs.getPermissions(), 
        	    methSym.params(), tree.args);
	}
	return result;
    }
    
    public DerefSet substVars(Permissions permissions, 
	    List<VarSymbol> from, List<JCExpression> to) {
	JCExpression newExp = permissions.substVars(exp, from, to);
	if (newExp != exp) {
	    return new DerefSet(newExp, this.treeGroup, rs);
	}
	return this;
    }

    public DerefSet inEnvironment(Resolve rs, Env<AttrContext> env) {
	if (this == DerefSet.NONE) return this;
	else if (!rs.isInScope(exp, env))
	    return DerefSet.NONE;
	else if (!rs.isInScope(treeGroup, env))
	    return DerefSet.NONE;
	return this;
    }
    
    @Override public String toString() {
	if (this == DerefSet.NONE)
	    return "NONE";
	StringBuffer sb = new StringBuffer();	
	if (exp != null) {
	    sb.append(exp);
	    if (treeGroup != RefGroup.NONE) {
		sb.append("...");
		sb.append(treeGroup);
	    }
	}
	return sb.toString();
    }    
    
    @Override public int hashCode() {
	return this.toString().hashCode();
    }

    @Override public boolean equals(Object o) {
	return this.toString().equals(o.toString());
    }
}