package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Substitutions.AsMemberOf;
import com.sun.tools.javac.code.Substitutions.AtCallSite;
import com.sun.tools.javac.code.Substitutions.SubstRefGroups;
import com.sun.tools.javac.code.Substitutions.SubstVars;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

public abstract class Permission {
    
    public static abstract class RefPerm extends Permission 
    	implements SubstRefGroups<RefPerm>, AsMemberOf<RefPerm>
{
	
	/**
	 * The reference permission 'this' as a member of type t
	 */
	public RefPerm asMemberOf(Types types, Type t) {
	    return this;
	}
	
	public RefPerm substRefGroups(List<RefGroup> from, List<RefGroup> to) {
	    return this;
	}
	
	public static final RefPerm NO_PERM = new RefPerm() {
	    @Override public String toString() {
		return "[no permission]";
	    }
	};
	
	public static final RefPerm SHARED = new RefPerm() {
	    @Override public String toString() {
		return "shared";
	    }
	};
	    
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
	 * by assigning to the var in the leftmost position.
	 */
	protected boolean recursiveIsKilled(VarSymbol var, JCExpression e) {
	    if (e instanceof JCIdent) {
		return e.getSymbol().equals(var);
	    }
	    else if (e instanceof JCFieldAccess) {
		JCFieldAccess fa = (JCFieldAccess) e;
		return recursiveIsKilled(var, fa.selected);
	    }
	    else if (e instanceof JCArrayAccess) {
		JCArrayAccess aa = (JCArrayAccess) e;
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
		super(refGroup, RefGroup.NO_GROUP);
		this.refGroup = refGroup;
	    }
	
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

	    public FreshGroupPerm atCallSite(Types types, JCMethodInvocation tree) {
		RefGroup refGroup = this.refGroup.atCallSite(types, tree);
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

	    /** 'e' in 'copies e [...G1] to G2 */
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
		super(sourceGroup, RefGroup.NO_GROUP);
		this.exp = exp;
		this.consumedFields = consumedFields;
		this.sourceGroup = sourceGroup;
		this.targetGroup = targetGroup;
	    }

	    /** Create a permission 'copies e to G' */
	    public static CopyPerm simplePerm(JCExpression exp,
		    RefGroup targetGroup) {
		return new CopyPerm(exp, null, RefGroup.NO_GROUP, targetGroup);
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
	    
	    public boolean isTreePerm() {
		return sourceGroup != null;
	    }
	    
	    public boolean representsMultiplePerms() {
		return consumedFields != null;
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
	    
	    public CopyPerm atCallSite(Types types, JCMethodInvocation tree) {
		RefGroup sourceGroup = (this.sourceGroup == null) ?
			null : this.sourceGroup.atCallSite(types, tree);
		RefGroup targetGroup = this.targetGroup.atCallSite(types, tree);
		if (this.sourceGroup != sourceGroup || this.targetGroup != targetGroup)
		    return new CopyPerm(this.exp, this.consumedFields,
			    sourceGroup, targetGroup);
		return this;
	    }
	    
	    public CopyPerm substVarExprs(Permissions permissions, 
		    List<VarSymbol> from, List<JCExpression> to) {
		JCExpression newExp = permissions.substVars(exp, from, to);
		if (newExp != exp) {
		    return new CopyPerm(newExp, consumedFields,
			    sourceGroup, targetGroup);
		}
		return this;
	    }
	    
	    public CopyPerm substVarSymbols(Permissions permissions,
		    List<VarSymbol> from, List<VarSymbol> to) {
		ListBuffer<JCExpression> lb = ListBuffer.lb();
		for (VarSymbol var : to) {
		    lb.append(permissions.maker.Ident(var));
		}
		return substVarExprs(permissions, from, lb.toList());
	    }
	    
	    @Override public String toString() {
		StringBuffer sb = new StringBuffer("copies ");
		sb.append(exp);
		if (sourceGroup != null) {
		    sb.append("...");
		    sb.append(sourceGroup);
		}
		if (consumedFields != null) {
		    for (Name field : consumedFields) {
			sb.append("\\");
			sb.append(field);
		    }
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
		if (this.representsMultiplePerms() != 
			copyPerm.representsMultiplePerms())
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
	     * (1) P1 is a single perm and P1 equals P2; or
	     * (2) P1 is a multiple tree perm, P2's expression is a field 
	     * access e.f, P1 is equal to P2 with its expression replaced by e, 
	     * f is in the source group of P1, and f is not in the consumed perms
	     * of P1.
	     * 
	     * For example:  
	     * (1) copies x...G1 to G2 represents copies x.f...G1 to G2
	     * if f is in group G1
	     * (2) copies x...G1\f to G2 does not represent copies x.f...G1 to G2,
	     * because f is in the consumed perms set of P1
	     * (3) copies x...G1 to G2 does not represent copies x.f...G1 to G2 if
	     * f is not in group G1.
	     */
	    public boolean representsPerm(CopyPerm perm, Attr attr,
		    Env<AttrContext> env) {
		if (!this.representsMultiplePerms())
		    return this.equals(perm);
		if (!(perm.exp instanceof JCFieldAccess))
		    return false;
		JCFieldAccess fa = (JCFieldAccess) perm.exp;
		if (!this.equals(multipleTreePerm(fa.selected, perm.sourceGroup,
			perm.targetGroup)))
		    return false;
		Pair<VarSymbol,RefPerm> pair = 
			attr.getSymbolAndRefPermFor(exp, env);
		if (!pair.snd.equals(this.sourceGroup)) return false;
		if (this.consumedFields.contains(fa.name)) return false;
		return true;
	    }
	    
	    @Override public int hashCode() {
		return targetGroup.toString().hashCode() << 3 + 1;
	    }

	}
    
	public static class EffectPerm extends EnvPerm 
		implements SubstRefGroups<EffectPerm>,
		AsMemberOf<EffectPerm>, AtCallSite<EffectPerm>
	{
	
	    public EffectPerm() {
		super(RefGroup.NO_GROUP, RefGroup.NO_GROUP);
	    }
	    
	    public EffectPerm substRefGroups(List<RefGroup> from, List<RefGroup> to) {
		// TODO
		throw new UnsupportedOperationException();
	    }
	    
	    public EffectPerm asMemberOf(Types types, Type t) {
		// TODO
		throw new UnsupportedOperationException();
	    }
	    
	    public EffectPerm atCallSite(Types types, JCMethodInvocation tree) {
		// TODO
		throw new UnsupportedOperationException();
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
		super(refGroup, RefGroup.NO_GROUP);
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
	    
	    public PreservedGroupPerm atCallSite(Types types, 
		    JCMethodInvocation tree) {
		RefGroup refGroup = this.refGroup.atCallSite(types, tree);
		return (this.refGroup == refGroup) ?
			this : new PreservedGroupPerm(refGroup);
	    }

	    
	}
    
	/**
	 * Class representing a permission 'updates G'
	 */
	public static class UpdatedGroupPerm extends EnvPerm 
		implements SubstRefGroups<UpdatedGroupPerm>,
		AsMemberOf<UpdatedGroupPerm>, AtCallSite<UpdatedGroupPerm>
	{
	
	    /** The updated group */	
	    public final RefGroup refGroup;
	
	    public UpdatedGroupPerm(RefGroup refGroup) {
		super(RefGroup.NO_GROUP, refGroup);
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

	    public UpdatedGroupPerm atCallSite(Types types, 
		    JCMethodInvocation tree) {
		RefGroup refGroup = this.refGroup.atCallSite(types, tree);
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