package com.sun.tools.javac.code;

import java.util.Set;

import com.sun.tools.javac.code.Permission.RefPerm.LocallyUnique;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Translation.AsMemberOf;
import com.sun.tools.javac.code.Translation.AtCallSite;
import com.sun.tools.javac.code.Translation.SubstRPLs;
import com.sun.tools.javac.code.Translation.SubstRefGroups;
import com.sun.tools.javac.code.Translation.SubstVars;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public abstract class Permission {
    
    public static abstract class RefPerm extends Permission 
    	implements com.sun.tools.javac.code.Translation.SubstRefGroups<RefPerm>, 
    	com.sun.tools.javac.code.Translation.AsMemberOf<RefPerm>
{
	
	public RefGroup getRefGroup() {
	    return RefGroup.NONE;
	}
	
	/**
	 * The reference permission 'this' as a member of type t
	 */
	public RefPerm asMemberOf(Types types, Type t) {
	    return this;
	}
	
	public RefPerm substRefGroups(List<RefGroup> from, List<RefGroup> to) {
	    return this;
	}
	
	public boolean isLocallyUnique() {
	    return this instanceof LocallyUnique;
	}
	
	public boolean isShared() {
	    return this == SHARED;
	}
	
	public boolean isLeaf() {
	    return this == UNIQUE;
	}
	
	public static final RefPerm NONE = new RefPerm() {
	    @Override public String toString() {
		return "[no permission]";
	    }
	};

	/**
	 * Object representing the SHARED ref perm
	 */
	public static final RefPerm SHARED = new RefPerm() {
	    @Override public String toString() {
		return "shared";
	    }
	};
	
	/**
	 * Object representing the UNIQUE ref perm
	 */
	public static final RefPerm UNIQUE = new LocallyUnique(RefGroup.UNIQUE) {
	    @Override public String toString() {
		return "unique";
	    }
	};
	    
	/**
	 * Object representing an erroneous ref perm
	 */
	public static final RefPerm ERROR = new RefPerm() {
	    @Override public String toString() {
		return "error";
	    }
	};
	
	
	public static class LocallyUnique extends RefPerm {
		
	    public final RefGroup refGroup;

	    public LocallyUnique(RefGroup refGroup) {
		this.refGroup = refGroup;
	    }

	    @Override public RefGroup getRefGroup() {
		return refGroup;
	    }
	    
	    @Override public RefPerm asMemberOf(Types types, Type t) {
		RefGroup refGroup = this.refGroup.asMemberOf(types, t);
		return (this.refGroup == refGroup) ?
			this: new LocallyUnique(refGroup);
	    }
		
	    @Override public RefPerm substRefGroups(List<RefGroup> from, 
		    List<RefGroup> to) {
		RefGroup refGroup = this.refGroup.substRefGroups(from, to);
		return (this.refGroup == refGroup) ?
			this : new LocallyUnique(refGroup);
	    }
	    
	    @Override public String toString() {
		return "unique(" + refGroup + ")";
	    }
		
	    @Override public boolean equals(Object obj) {
		if (!(obj instanceof LocallyUnique)) return false;
		return this.refGroup.equals(((LocallyUnique) obj).refGroup);
	    }
		
	}
	
    }
    
    /**
     * Class representing a permission in the environment
     */
    public static abstract class EnvPerm extends Permission {
	
	public final RefGroup updatedGroup;
	public final RefGroup preservedGroup;
	
	/** Is this perm consumed on use? */
	public boolean isLinear() { return false; }
	
	protected EnvPerm(RefGroup preservedGroup, 
		RefGroup updatedGroup) {
	    this.preservedGroup = preservedGroup;
	    this.updatedGroup = updatedGroup;
	}
	
	public boolean updatesGroup(RefGroup refGroup) {
	    return refGroup.equals(updatedGroup);
	}
	
	public boolean preservesGroup(RefGroup refGroup) {
	    return refGroup.equals(preservedGroup);
	}
		
	/**
	 * Is this permission killed by assigning to var?
	 */
	public boolean isKilledByAssigningTo(VarSymbol var) {
	    return false;
	}
	
	/**
	 * An env permission that refers to an expression is killed
	 * by assigning to the var in the leftmost position or any
	 * index var appearing in an array access.
	 */
	protected boolean recursiveIsKilled(VarSymbol var, JCExpression e) {
	    if (e instanceof JCIdent) {
		return var.equals(e.getSymbol());
	    }
	    else if (e instanceof JCFieldAccess) {
		JCFieldAccess fa = (JCFieldAccess) e;
		return recursiveIsKilled(var, fa.selected);
	    }
	    else if (e instanceof JCArrayAccess) {
		JCArrayAccess aa = (JCArrayAccess) e;
		if (var.equals(aa.index.getSymbol()))
		    return true;
		return recursiveIsKilled(var, aa.indexed);
	    }
	    return false;
	}
	
	/**
	 * Class representing a permission 'fresh G'
	 */
	public static class FreshGroupPerm extends EnvPerm 
		implements SubstRefGroups<FreshGroupPerm>, 
		AsMemberOf<FreshGroupPerm>, AtCallSite<FreshGroupPerm>
	{
	
	    /** The fresh group */	
	    public final RefGroup refGroup;
	
	    public FreshGroupPerm(RefGroup refGroup) {
		super(refGroup, RefGroup.NONE);
		this.refGroup = refGroup;
	    }
	
	    @Override public boolean isLinear() { return true; }
	    
	    @Override public String toString() {
		return "fresh " + refGroup;
	    }
	
	    @Override public boolean equals(Object obj) {
		if (!(obj instanceof FreshGroupPerm))
		    return false;
		return this.refGroup.equals(((FreshGroupPerm) obj).refGroup);
	    }
	
	    @Override public int hashCode() {
		return refGroup.hashCode() << 3;
	    }
	    
	    public FreshGroupPerm substRefGroups(List<RefGroup> from, 
		    List<RefGroup> to) {
		RefGroup refGroup = this.refGroup.substRefGroups(from, to);
		return (this.refGroup == refGroup) ?
			this : new FreshGroupPerm(refGroup);
	    }

	    public FreshGroupPerm asMemberOf(Types types, Type t) {
		RefGroup refGroup = this.refGroup.asMemberOf(types, t);
		return (this.refGroup == refGroup) ?
			this : new FreshGroupPerm(refGroup);
	    }

	    public FreshGroupPerm atCallSite(Resolve rs, Env<AttrContext> env,
		    JCMethodInvocation tree) {
		RefGroup refGroup = this.refGroup.atCallSite(rs, env, tree);
		return (this.refGroup == refGroup) ?
			this : new FreshGroupPerm(refGroup);
	    }
	    
	    public FreshGroupPerm atNewClass(Resolve rs, Env<AttrContext> env,
		    JCNewClass tree) {
		RefGroup refGroup = this.refGroup.atNewClass(rs, env, tree);
		return (this.refGroup == refGroup) ?
			this : new FreshGroupPerm(refGroup);
	    }

	}
    
	/**
	 * Class representing 'copies e [...G1] to G2'
	 */ 
	public static class CopyPerm extends EnvPerm 
		implements 
		SubstRefGroups<CopyPerm>,
		AsMemberOf<CopyPerm>, 
		AtCallSite<CopyPerm>,
		SubstVars<CopyPerm> 
	{

	    /** 'e' in 'copies e [...G1] to G2' */
	    public final JCExpression exp;
	    
	    /** If this field is non-null, then this instance represents
	     *  all permissions 'copies e.f [...G1] to G2' such that 
	     *  (1) f has permission unique(G1) and (2) f is NOT in
	     *  consumedFields.
	     */
	    public final List<Name> consumedFields;

	    /** 'G1' in 'copies e [...G1] to G2.  If this field is null,
	     *  then there is no source group.
	     */
	    public final RefGroup sourceGroup;

	    /** 'G2' in 'copies e [...G1] to G2 */
	    public final RefGroup targetGroup;
	
	    private CopyPerm(JCExpression exp, List<Name> consumedFields,
		    RefGroup sourceGroup, RefGroup targetGroup) {
		super(sourceGroup, RefGroup.NONE);
		this.exp = exp;
		this.consumedFields = consumedFields;
		this.sourceGroup = sourceGroup;
		this.targetGroup = targetGroup;
	    }

	    /** Create a permission 'copies e to G' */
	    public static CopyPerm simplePerm(JCExpression exp,
		    RefGroup targetGroup) {
		return new CopyPerm(exp, null, RefGroup.NONE, targetGroup);
	    }

	    /** Create a permission 'copies e...G1 to G2' */
	    public static CopyPerm singleTreePerm(JCExpression exp, 
		    RefGroup sourceGroup, RefGroup targetGroup) {
		return new CopyPerm(exp, null, sourceGroup, targetGroup);
	    }
	    
	    /** Create a set of permissions 'copies e{.f}...G1 to G2 */
	    public static CopyPerm multipleTreePerm(JCExpression exp,
		    RefGroup sourceGroup, RefGroup targetGroup) {
		return new CopyPerm(exp, List.<Name>nil(), sourceGroup,
			targetGroup);
	    }
	    
	    public static CopyPerm multipleTreePerm(JCExpression exp,
		    List<Name> consumedFields, RefGroup sourceGroup, 
		    RefGroup targetGroup) {
		return new CopyPerm(exp, consumedFields, sourceGroup,
			targetGroup);
	    }
	    
	    @Override
	    public boolean isLinear() { return true; }
	    
	    public boolean isTreePerm() {
		return sourceGroup != RefGroup.NONE;
	    }
	    
	    public boolean isMultipleTreePerm() {
		return consumedFields != null;
	    }
	
	    public boolean canBeSplitFromFreshGroup(Scope scope) {
		// exp must be local variable
		if (!(exp instanceof JCIdent)) return false;
		JCIdent id = (JCIdent) exp;
		if (!(id.sym instanceof VarSymbol) || 
			(id.sym.owner.kind != Kinds.MTH)) 
		    return false;
		// perm of exp must match source group
		RefPerm refPerm = scope.getRefPermFor((VarSymbol) id.sym);
		if (!(refPerm instanceof LocallyUnique))
		    return false;
		LocallyUnique lu = (LocallyUnique) refPerm;
		RefGroup refGroup = lu.refGroup;
		if (!refGroup.equals(sourceGroup)) return false;		
		return true;
	    }
	    
	    @Override public boolean isKilledByAssigningTo(VarSymbol var) {
		return recursiveIsKilled(var, exp);
	    }
	    
	    public CopyPerm substRefGroups(List<RefGroup> from, List<RefGroup> to) {
		RefGroup sourceGroup = (this.sourceGroup == null) ?
			null : this.sourceGroup.substRefGroups(from, to);
		RefGroup targetGroup = this.targetGroup.substRefGroups(from, to);
		if (this.sourceGroup != sourceGroup || this.targetGroup != targetGroup)
		    return new CopyPerm(this.exp, this.consumedFields,
			    sourceGroup, targetGroup);
		return this;
	    }
	    
	    public CopyPerm asMemberOf(Types types, Type t) {
		RefGroup sourceGroup = (this.sourceGroup == null) ?
			null : this.sourceGroup.asMemberOf(types, t);
		RefGroup targetGroup = this.targetGroup.asMemberOf(types, t);
		if (this.sourceGroup != sourceGroup || this.targetGroup != targetGroup)
		    return new CopyPerm(this.exp, this.consumedFields,
			    sourceGroup, targetGroup);
		return this;
	    }
	    
	    public CopyPerm atCallSite(Resolve rs, Env<AttrContext> env,
		    JCMethodInvocation tree) {
		RefGroup sourceGroup = (this.sourceGroup == null) ?
			null : this.sourceGroup.atCallSite(rs, env, tree);
		RefGroup targetGroup = this.targetGroup.atCallSite(rs, env, tree);
		CopyPerm result = this;
		if (this.sourceGroup != sourceGroup || this.targetGroup != targetGroup)
		    result = new CopyPerm(this.exp, this.consumedFields,
			    sourceGroup, targetGroup);
		MethodSymbol methSym = tree.getMethodSymbol();
		if (methSym != null) {
	            result = result.substVars(rs.getPermissions(), 
	        	    methSym.params(), tree.args);
	            result = Translation.substForThis(result, tree.meth,
	        	    rs, env, methSym.owner.thisSym);
		}
		return result;
	    }

	    public CopyPerm atNewClass(Resolve rs, Env<AttrContext> env,
		    JCNewClass tree) {
		RefGroup sourceGroup = (this.sourceGroup == null) ?
			null : this.sourceGroup.atNewClass(rs, env, tree);
		RefGroup targetGroup = this.targetGroup.atNewClass(rs, env, tree);
		CopyPerm result = this;
		if (this.sourceGroup != sourceGroup || this.targetGroup != targetGroup)
		    result = new CopyPerm(this.exp, this.consumedFields,
			    sourceGroup, targetGroup);
		MethodSymbol methSym = tree.getMethodSymbol();
		if (methSym != null) {
	            result = result.substVars(rs.getPermissions(), 
	        	    methSym.params(), tree.args);
		}
		return result;
	    }
	    
	    public CopyPerm substVars(Permissions permissions, 
		    List<VarSymbol> from, List<JCExpression> to) {
		JCExpression newExp = permissions.substVars(exp, from, to);
		if (newExp != exp) {
		    return new CopyPerm(newExp, consumedFields,
			    sourceGroup, targetGroup);
		}
		return this;
	    }
	    
	    @Override public String toString() {
		StringBuffer sb = new StringBuffer("copies ");
		sb.append(exp);
		if (consumedFields != null) {
		    sb.append(".?");
		    for (Name field : consumedFields) {
			sb.append("\\");
			sb.append(field);
		    }
		}
		if (sourceGroup != RefGroup.NONE) {
		    sb.append("...");
		    sb.append(sourceGroup);
		}
		sb.append(" to ");
		sb.append(targetGroup);
		return sb.toString();
	    }
	
	    /**
	     * Test copy perms for equality:
	     * 1. The expressions, source groups, and target groups must match.
	     * 2. Both must represent multiple perms, or not.
	     */
	    @Override public boolean equals(Object obj) {
		if (!(obj instanceof CopyPerm))
		    return false;
		CopyPerm copyPerm = (CopyPerm) obj;
		if (!Permissions.matchingExprs(this.exp, 
			copyPerm.exp)) return false;
		if (this.isMultipleTreePerm() != 
			copyPerm.isMultipleTreePerm())
		    return false;
		if (this.isTreePerm() != copyPerm.isTreePerm())
		    return false;
		if (this.isTreePerm() &&	
			!this.sourceGroup.equals(copyPerm.sourceGroup)) {
		    return false;
		}
		if (!this.targetGroup.equals(copyPerm.targetGroup))
		    return false;
		return true;
	    }

	    /**
	     * Does this permission represent perm?
	     * 
	     * P1 represents P2 if 
	     * (1) P1 is a simple perm 'copies e to G' or single tree perm
	     * copies 'e...G1 to G2' and P1 equals P2; or
	     * (2) P1 is a multiple tree perm 'copies e.?{\f}...G1 to G2', 
	     * P2 has the form 'copies e.f...G1 to G2', f is in G1, and f 
	     * is not in the consumed perms of P1.
	     * 
	     * For example:  
	     * (1) copies x.?...G1 to G2 represents copies x.f...G1 to G2
	     * if f is in group G1
	     * (2) copies x.?\f...G1 to G2 does not represent copies x.f...G1 to G2,
	     * because f is in the consumed perms set of P1
	     * (3) copies x.?...G1 to G2 does not represent copies x.f...G1 to G2 if
	     * f is not in group G1.
	     */
	    public boolean representsPerm(CopyPerm perm, Attr attr,
		    Env<AttrContext> env) {
		if (!this.isMultipleTreePerm())
		    return this.equals(perm);
		if (!(perm.exp instanceof JCFieldAccess))
		    return false;
		JCFieldAccess fa = (JCFieldAccess) perm.exp;
		if (!this.equals(multipleTreePerm(fa.selected, perm.sourceGroup,
			perm.targetGroup)))
		    return false;
		RefPerm refPerm = attr.getRefPermFor(exp, env);
		if (!refPerm.equals(this.sourceGroup)) return false;
		if (this.consumedFields.contains(fa.name)) return false;
		return true;
	    }
	    
	    @Override public int hashCode() {
		return targetGroup.toString().hashCode() << 3 + 1;
	    }

	}
    
	/**
	 * Class representing '(reads|writes) R [via e [...G]]'
	 */
	public static class EffectPerm extends EnvPerm 
		implements 
		SubstRPLs<EffectPerm>,
		SubstRefGroups<EffectPerm>,
		AsMemberOf<EffectPerm>, 
		AtCallSite<EffectPerm>,
		SubstVars<EffectPerm>
	{

	    public static EffectPerm DEFAULT =
		    new EffectPerm(true, new RPL(List.of(RPLElement.ROOT_ELEMENT, 
			    RPLElement.STAR)),
			    DerefSet.NONE);
	    
	    public static EffectPerm UNKNOWN =
		    new EffectPerm(false, new RPL(List.of(RPLElement.ROOT_ELEMENT,
			    RPLElement.STAR)), DerefSet.NONE) {
		public String toString() { return "UNKNOWN"; }
	    };
	    
	    public static EffectPerm NONE =
		    new EffectPerm(false, new RPL(), DerefSet.NONE) {
		public String toString() { return "NONE"; }
	    };
	    
	    /**
	     * Is this a write effect?
	     */
	    public boolean isWrite;
	    
	    /** 'R' in '(reads|writes) R [via e [...G]]' */
	    public final RPL rpl;
	    
	    /**
	     * 'D' in '(reads|writes) R [via D]'
	     * If there is no deref set, then this field equals DerefSet.NONE
	     */
	    public final DerefSet derefSet;

	    /**
	     * Was the tree part of this permission (if any) used to establish
	     * noninterference?
	     */
	    public boolean usedInTreeComparison;
	    
	    /**
	     * Does this effect permission have a deref set?
	     */
	    public boolean hasDerefSet() {
		return this.derefSet != DerefSet.NONE;
	    }
	    
	    public EffectPerm(boolean isWrite, RPL rpl, DerefSet derefSet) {
		super(derefSet.treeGroup, RefGroup.NONE);
		this.isWrite = isWrite;
		this.rpl = rpl;
		this.derefSet = derefSet;
	    }
	    
	    public EffectPerm substRPLs(List<RPL> from,
		    List<RPL> to) {
		RPL rpl = this.rpl.substRPLs(from, to);
		if (this.rpl != rpl)
		    return new EffectPerm(this.isWrite, rpl, this.derefSet);
		return this;
	    }
	    
	    public EffectPerm substRefGroups(List<RefGroup> from, 
		    List<RefGroup> to) {
		DerefSet derefSet =
			this.derefSet.substRefGroups(from, to);
		if (this.derefSet != derefSet)
		    return new EffectPerm(this.isWrite, this.rpl, derefSet);
		return this;
	    }
	    
	    public EffectPerm substVars(Permissions permissions, 
		    List<VarSymbol> from, List<JCExpression> to) {
		DerefSet derefSet = this.derefSet.substVars(permissions, from, to);
		if (derefSet != this.derefSet) {
		    return new EffectPerm(this.isWrite, this.rpl,
			    derefSet);
		}
		return this;
	    }
	    	    
	    public EffectPerm asMemberOf(Types types, Type t) {
		RPL rpl = this.rpl.asMemberOf(types, t);
		DerefSet derefSet = this.derefSet.asMemberOf(types, t);
		if (this.rpl != rpl || this.derefSet != derefSet)
		    return new EffectPerm(this.isWrite, rpl, derefSet);
		return this;
	    }
	    
	    public EffectPerm atCallSite(Resolve rs, Env<AttrContext> env,
		    JCMethodInvocation tree) {
		RPL rpl = this.rpl.atCallSite(rs, env, tree);
		DerefSet derefSet = this.derefSet.atCallSite(rs, 
			env, tree);
		if (this.rpl != rpl || this.derefSet != derefSet)
		    return new EffectPerm(this.isWrite, rpl, 
			    derefSet);
		return this;
	    }
	    
	    public EffectPerm atNewClass(Resolve rs, Env<AttrContext> env,
		    JCNewClass tree) {
		RPL rpl = this.rpl.atNewClass(rs, env, tree);
		DerefSet derefSet = this.derefSet.atNewClass(rs, 
			env, tree);
		if (this.rpl != rpl || this.derefSet != derefSet)
		    return new EffectPerm(this.isWrite, rpl, 
			    derefSet);
		return this;
	    }

	    @Override 
	    public int hashCode() {
		int code = rpl.hashCode() + derefSet.hashCode();
		if (isWrite) ++code;
		if (usedInTreeComparison) ++code;
		return (code << 3) + 2;
	    }
	    
	    @Override
	    public boolean equals(Object o) {
		if (o instanceof EffectPerm) {
		    EffectPerm effectPerm = (EffectPerm) o;
		    if (this.isWrite != effectPerm.isWrite)
			return false;
		    if (!this.rpl.equals(effectPerm.rpl))
			return false;
		    if (!this.derefSet.equals(effectPerm.derefSet))
			return false;
		    return this.usedInTreeComparison == 
			    effectPerm.usedInTreeComparison;
		}
		return false;
	    }
	    
	    public EffectPerm inEnvironment(Resolve rs, Env<AttrContext> env, 
			boolean pruneLocalEffects) {
		RPL newRPL = rpl.inEnvironment(rs, env, pruneLocalEffects);
		if (newRPL == RPL.NONE)
		    return EffectPerm.NONE;
		DerefSet newDerefSet = derefSet.inEnvironment(rs, env);
		if (this.rpl != newRPL || this.derefSet != newDerefSet)
		    return new EffectPerm(this.isWrite, newRPL, newDerefSet);
		return this;
	    }
	    
	    public EffectPerm coarsenWith(Set<RefGroup> updatedGroups,
		    Attr attr, Env<AttrContext> env) {
		RefGroup rg = this.derefSet.refGroup(attr, env);
		if (updatedGroups.contains(rg)) {
		    return new EffectPerm(this.isWrite, rpl, DerefSet.NONE);
		}
		else {
		    return this;
		}
	    }
	    
	    /**
	     * Is this included in e?
	     */
	    public boolean isIncludedIn(EffectPerm e, 
		    Env<AttrContext> env, Resolve rs) {
		// Anything is included in an unknown permission, and vice versa
		if (this==EffectPerm.UNKNOWN || e==EffectPerm.UNKNOWN) return true;
		// None is included in anything
		if (this==EffectPerm.NONE) return true;
		// Nothing is included in None
		if (e==EffectPerm.NONE) return false; 
		// Write can't be included in read
		if (this.isWrite && !e.isWrite) return false;
		// RPLs must be included
		if (!(this.rpl.isIncludedIn(e.rpl)))
		    return false;
		// If e has deref set, then this.D must be included in e.D
		if (e.hasDerefSet()) return this.hasDerefSet() &&
			    this.derefSet.isIncludedIn(e.derefSet, env, rs);
		// If this was used in tree comparison, and we are including it
		// in something with no deref set, then it must be a tree root
		if (this.usedInTreeComparison)
		    return this.derefSet.isTreeRoot(rs.getAttr(),  env);
		// Otherwise we are OK
		return true;
	    }	    

	    /**
	     * Is this noninterfering with e?
	     */
	    public boolean isNoninterferingWith(EffectPerm e,
		    Env<AttrContext> env, RPLs rpls, 
		    Constraints constraints) {
		if (this==EffectPerm.UNKNOWN || e==EffectPerm.UNKNOWN)
		    return true;
		if (this==EffectPerm.NONE || e==EffectPerm.NONE)
		    return true;
		if (!this.isWrite && !e.isWrite)
		    return true;
		if (rpls.areDisjoint(this.rpl, e.rpl, constraints.disjointRPLs))
		    return true;
		if (this.derefSet.isDisjointFrom(e.derefSet, env)) {
		    // Record that we needed the tree info
		    this.usedInTreeComparison=true;
		    e.usedInTreeComparison=true;
		    return true;
		}
		return false;
	    }
	    
	    @Override public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(isWrite ? "writes " : "reads ");
		sb.append(rpl);
		if (derefSet != DerefSet.NONE) {
		    sb.append(" via ");
		    sb.append(derefSet);
		}
		return sb.toString();
	    }

	}
    
	/**
	 * Class representing a permission 'preserves G'
	 */
	public static class PreservedGroupPerm extends EnvPerm 
		implements SubstRefGroups<PreservedGroupPerm>,
		AsMemberOf<PreservedGroupPerm>, AtCallSite<PreservedGroupPerm>
	{
	
	    /** The preserved group */	
	    public final RefGroup refGroup;
	    	
	    public PreservedGroupPerm(RefGroup refGroup) {
		super(refGroup, RefGroup.NONE);
		this.refGroup = refGroup;
	    }
	
	    @Override public String toString() {
		return "preserves " + refGroup;
	    }	
	
	    @Override public boolean equals(Object obj) {
		if (!(obj instanceof PreservedGroupPerm))
		    return false;
		return this.refGroup.equals(((PreservedGroupPerm) obj).refGroup);
	    }
	
	    @Override public int hashCode() {
		return (refGroup.hashCode() << 3) + 3;
	    }

	    public PreservedGroupPerm substRefGroups(List<RefGroup> from, 
		    List<RefGroup> to) {
		RefGroup refGroup = this.refGroup.substRefGroups(from, to);
		return (refGroup == this.refGroup) ? this : 
		    new PreservedGroupPerm(refGroup);
	    }

	    public PreservedGroupPerm asMemberOf(Types types, Type t) {
		RefGroup refGroup = this.refGroup.asMemberOf(types, t);
		return (refGroup == this.refGroup) ? this :
		    new PreservedGroupPerm(refGroup);
	    }
	    
	    public PreservedGroupPerm atCallSite(Resolve rs, Env<AttrContext> env,
		    JCMethodInvocation tree) {
		RefGroup refGroup = this.refGroup.atCallSite(rs, env, tree);
		return (this.refGroup == refGroup) ?
			this : new PreservedGroupPerm(refGroup);
	    }

	    public PreservedGroupPerm atNewClass(Resolve rs, Env<AttrContext> env,
		    JCNewClass tree) {
		RefGroup refGroup = this.refGroup.atNewClass(rs, env, tree);
		return (this.refGroup == refGroup) ?
			this : new PreservedGroupPerm(refGroup);
	    }
	    
	}
    
	/**
	 * Class representing a permission 'updates G'
	 */
	public static class UpdatedGroupPerm extends EnvPerm 
		implements 
		SubstRefGroups<UpdatedGroupPerm>,
		AsMemberOf<UpdatedGroupPerm>, 
		AtCallSite<UpdatedGroupPerm>
	{
	
	    /** The updated group */	
	    public final RefGroup refGroup;
	
	    public UpdatedGroupPerm(RefGroup refGroup) {
		super(RefGroup.NONE, refGroup);
		this.refGroup = refGroup;
	    }

	    public UpdatedGroupPerm substRefGroups(List<RefGroup> from, 
		    List<RefGroup> to) {
		RefGroup refGroup = this.refGroup.substRefGroups(from, to);
		return (refGroup == this.refGroup) ? this : 
		    new UpdatedGroupPerm(refGroup);
	    }
	
	    public UpdatedGroupPerm asMemberOf(Types types, Type t) {
		RefGroup refGroup = this.refGroup.asMemberOf(types, t);
		return (refGroup == this.refGroup) ? this :
		    new UpdatedGroupPerm(refGroup);
	    }

	    public UpdatedGroupPerm atCallSite(Resolve rs, Env<AttrContext> env,
		    JCMethodInvocation tree) {
		RefGroup refGroup = this.refGroup.atCallSite(rs, env, tree);
		return (this.refGroup == refGroup) ?
			this : new UpdatedGroupPerm(refGroup);
	    }

	    public UpdatedGroupPerm atNewClass(Resolve rs, Env<AttrContext> env,
		    JCNewClass tree) {
		RefGroup refGroup = this.refGroup.atNewClass(rs, env, tree);
		return (this.refGroup == refGroup) ?
			this : new UpdatedGroupPerm(refGroup);
	    }

	    @Override public String toString() {
		return "updates " + refGroup;
	    }

	    @Override public boolean equals(Object obj) {
		if (!(obj instanceof UpdatedGroupPerm))
		    return false;
		return this.refGroup.equals(((UpdatedGroupPerm) obj).refGroup);
	    }
	
	    @Override public int hashCode() {
		return (refGroup.hashCode() << 3) + 4;
	    }	

	}
    
    }
    
     
}