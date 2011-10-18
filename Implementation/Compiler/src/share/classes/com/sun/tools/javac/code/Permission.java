package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Substitutions.AsMemberOf;
import com.sun.tools.javac.code.Substitutions.AtCallSite;
import com.sun.tools.javac.code.Substitutions.SubstRefGroups;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

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
	
	private final RefGroup updatedGroup;
	private final RefGroup preservedGroup;
	
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
	 * Class representing 'copies v [ [ (.f | '[' i ']') ] ...G1 ] to G2'
	 */ 
	public static class CopyPerm extends EnvPerm 
		implements SubstRefGroups<CopyPerm>,
		AsMemberOf<CopyPerm>, AtCallSite<CopyPerm> {

	    /** 'v' in 'copies v [ [ (.f | '[' i ']') ] ...G1 ] to G2' */
	    public final VarSymbol var;

	    /** 'f' in 'copies v.f...G1 to G2'; may be null */
	    public final Name field;
	
	    /** 'i' in 'copies v[i]...G1 to G2'; may be null */
	    public final VarSymbol indexVar;

	    /** 'G1' in 'copies v [ [ (.f | '[' i ']') ] ...G1 ] to G2'; may be null */
	    public final RefGroup sourceGroup;

	    /** 'G2' in 'copies v [ [ (.f | '[' i ']') ] ...G1 ] to G2' */
	    public final RefGroup targetGroup;
	
	    private CopyPerm(VarSymbol var, Name field, VarSymbol indexVar,
		    RefGroup sourceGroup, RefGroup targetGroup) {
		super(sourceGroup, RefGroup.NO_GROUP);
		this.var = var;
		this.field = field;
		this.indexVar = indexVar;
		this.sourceGroup = sourceGroup;
		this.targetGroup = targetGroup;
	    }

	    /** 'copies v to G' */
	    public CopyPerm(VarSymbol var, RefGroup targetGroup) {
		this(var, null, null, null, targetGroup);
	    }
	
	    /** 'copies v...G1 to G2' */
	    public CopyPerm(VarSymbol var, RefGroup sourceGroup, 
		    RefGroup targetGroup) {
		this(var, null, null, sourceGroup, targetGroup);
	    }
	
	    /** 'copies v.f...G1 to G2' */
	    public CopyPerm(VarSymbol var, Name field, RefGroup sourceGroup,
		    RefGroup targetGroup) {
		this(var, field, null, sourceGroup, targetGroup);
	    }
	    
	    /** 'copies v[i]...G1 to G2' */
	    public CopyPerm(VarSymbol var, VarSymbol indexVar,
		    RefGroup sourceGroup, RefGroup targetGroup) {
		this(var, null, indexVar, sourceGroup, targetGroup);
	    }
	
	    public boolean hasField() {
		return field != null;
	    }
	
	    public boolean hasIndexVar() {
		return indexVar != null;
	    }
	
	    public boolean hasSourceGroup() {
		return sourceGroup != null;
	    }
	
	    public CopyPerm substRefGroups(List<RefGroup> from, List<RefGroup> to) {
		// TODO
		throw new UnsupportedOperationException();
	    }
	    
	    public CopyPerm asMemberOf(Types types, Type t) {
		// TODO
		throw new UnsupportedOperationException();
	    }
	    
	    public CopyPerm atCallSite(Types types, JCMethodInvocation tree) {
		// TODO
		throw new UnsupportedOperationException();
	    }

	    
	    @Override public String toString() {
		StringBuffer sb = new StringBuffer("copies ");
		sb.append(var);
		if (field != null) {
		    sb.append(".");
		    sb.append(field);
		}
		if (indexVar != null) {
		    sb.append("[");
		    sb.append(indexVar);
		    sb.append("]");
		}
		if (sourceGroup != null) {
		    sb.append("...");
		    sb.append(sourceGroup);
		}
		sb.append(" to ");
		sb.append(targetGroup);
		return sb.toString();
	    }
	
	    @Override public boolean equals(Object obj) {
		if (!(obj instanceof CopyPerm))
		    return false;
		CopyPerm copyPerm = (CopyPerm) obj;
		if (!this.var.equals(copyPerm.var)) return false;
		if (this.field == null) {
		    if (copyPerm.field != null) return false;
		}
		else {
		    if (!this.field.equals(copyPerm.field)) return false;
		}
		if (this.indexVar == null) {
		    if (copyPerm.indexVar != null) return false;
		}
		else {
		    if (!this.indexVar.equals(copyPerm.indexVar))
			return false;
		}
		if (this.sourceGroup == null) {
		    if (copyPerm.sourceGroup != null) return false;
		}
		else {
		    if (!this.sourceGroup.equals(copyPerm.sourceGroup))
			return false;
		}
		if (!this.targetGroup.equals(copyPerm.targetGroup))
		    return false;
		return true;
	    }
	
	    @Override public int hashCode() {
		return (var.hashCode() << 3) + 1;
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