

package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Permission.EnvPerm.EffectPerm;
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
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.util.List;

/** A class to represent a JRG effect
 */
public abstract class Effect implements
	SubstRPLs<Effect>,
	SubstRefGroups<Effect>,
	AsMemberOf<Effect>,
	AtCallSite<Effect>,
	SubstVars<Effect>
{
    
    public RPLs rpls;
    
    protected Effect(RPLs rpls) {
	this.rpls = rpls;
    }
    
    public Effect inEnvironment(Resolve rs, Env<AttrContext> env, 
	    boolean pruneLocalEffects) {
	return this;
    }
    
    public abstract boolean isSubeffectOf(Effect e,  
	    Env<AttrContext> env, Resolve rs);
    
    public boolean isSubeffectOf(Effects effects, 
	    Env<AttrContext> env, Resolve rs) {
	// Ignore NONE
	if (this.equals(MemoryEffect.makeEffectFrom(rpls, EffectPerm.NONE)))
	    return true;
	// Ignore UNKNOWN
	if (this.equals(MemoryEffect.makeEffectFrom(rpls, EffectPerm.UNKNOWN)))
	    return true;
	// SE-UNION-1
	for (Effect e : effects) {
	    if (this.isSubeffectOf(e, env, rs)) return true;
	}
	return false;
    }
    
    /**
     * Noninterfering effects
     */
    public abstract boolean isNoninterferingWith(Effect e, 
	    Constraints constraints);
    
    public boolean isNoninterferingWith(Effects effects,
	    Constraints constraints) { 
	// NI-EMPTY
	if (effects.isEmpty()) return true;
	// NI-UNION
	for (Effect e : effects) {
	    if (!this.isNoninterferingWith(e, constraints))
		return false;
	}
	return true;
    }
    
    /** 
     * A class for memory effects
     */
    public static class MemoryEffect extends Effect {

	/**
	 * The effect permission representing this effect
	 */
	public final EffectPerm perm;

	/**
	 * Private constructor; only factory methods have public access
	 */
	private MemoryEffect(RPLs rpls, EffectPerm perm) {
	    super(rpls);
	    this.perm = perm;
	}

	/**
	 * Factory methods for read and write effects
	 */
	public static MemoryEffect readEffect(RPLs rpls, RPL rpl,
		DerefSet derefSet) {
	    return new MemoryEffect(rpls, new EffectPerm(false, rpl, derefSet));
	}

	public static MemoryEffect writeEffect(RPLs rpls, RPL rpl,
		DerefSet derefSet) {
	    return new MemoryEffect(rpls, new EffectPerm(true, rpl, derefSet));
	}

	public static MemoryEffect makeEffectFrom(RPLs rpls,
		EffectPerm perm) {
	    return new MemoryEffect(rpls, perm);
	}
	
	public boolean isWrite() {
	    return perm.isWrite;
	}
	
	public boolean isSubeffectOf(Effect e,
		Env<AttrContext> env, Resolve rs) {
	    if (e instanceof MemoryEffect) {
		MemoryEffect me = (MemoryEffect) e;
		return this.perm.isIncludedIn(me.perm, env, rs);
	    }
	    return false;
	}
	
	@Override
	public boolean isNoninterferingWith(Effect e, Constraints constraints) {
	    if (e instanceof MemoryEffect) {
		// NI-READ
		return true;
	    }
	    if (e instanceof InvocationEffect) {
		// NI-INVOKES-1
		if (this.isNoninterferingWith(((InvocationEffect) e).withEffects,
			constraints))
		    return true;
	    }
	    return false;
	}
	
	public Effect substRPLs(List<RPL> from, List<RPL> to) {
	    return new MemoryEffect(rpls, this.perm.substRPLs(from, to));
	}
	
	public Effect substRefGroups(List<RefGroup> from, List<RefGroup> to) {
	    return new MemoryEffect(rpls, this.perm.substRefGroups(from, to));
	}
	
	public Effect asMemberOf(Types types, Type t) {
	    return new MemoryEffect(rpls, this.perm.asMemberOf(types, t));
	}
	
	public Effect atCallSite(Resolve rs, Env<AttrContext> env,
		JCMethodInvocation tree) {
	    return new MemoryEffect(rpls, this.perm.atCallSite(rs, env,
		    tree));
	}
	
	public Effect atNewClass(Resolve rs, Env<AttrContext> env,
		JCNewClass tree) {
	    return new MemoryEffect(rpls, this.perm.atNewClass(rs, env, tree));
	}

	public Effect substVars(Permissions perms, List<VarSymbol> from,
		List<JCExpression> to) {
	    return new MemoryEffect(rpls, this.perm.substVars(perms, from, to));
	}

	@Override
	public Effect inEnvironment(Resolve rs, Env<AttrContext> env, 
		boolean pruneLocalEffects) {
	    return new MemoryEffect(rpls, this.perm.inEnvironment(rs, env, 
		    pruneLocalEffects));
	}

	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    sb.append(perm);
	    return sb.toString();
	}
	
	@Override
	public int hashCode() {
	    return 3 * this.toString().hashCode();
	}
	
	public boolean equals(Object o) {
	    if (!(o instanceof MemoryEffect))
		return false;
	    MemoryEffect me = (MemoryEffect) o;
	    return this.perm.equals(me.perm);
	}

    }
    
    /** A class for invocation effects
     */
    public static class InvocationEffect extends Effect {

	public MethodSymbol methSym;
	
	public Effects withEffects;
	
	public InvocationEffect (RPLs rpls, MethodSymbol methSym, Effects withEffects) {
	    super(rpls);
	    this.methSym = methSym;
	    this.withEffects = withEffects;
	}
	
	public boolean isSubeffectOf(Effect e, 
		Env<AttrContext> env, Resolve rs) {
	    if (e instanceof InvocationEffect) {
		InvocationEffect ie = (InvocationEffect) e;
		// SE-INVOKES-1
		if (this.methSym == ie.methSym && 
			this.withEffects.areSubeffectsOf(ie.withEffects,
				env, rs))
		    return true;
	    }
	    Effects effects = new Effects();
	    effects.add(e);
	    if (this.withEffects.areSubeffectsOf(effects,
		    env, rs))
		return true;
	    return false;
	}
	
	@Override
	public boolean isNoninterferingWith(Effect e, Constraints constraints) {
	    if (e.isNoninterferingWith(withEffects, constraints)) return true;
	    if (e instanceof InvocationEffect) {
		InvocationEffect ie = (InvocationEffect) e;
		if (Effects.noninterferingEffects(withEffects, ie.withEffects,
			constraints)) {
		    return true;
		}
		if ((methSym == ie.methSym) && 
			((methSym.flags() & Flags.ISCOMMUTATIVE)) != 0) {
		    return true;
		}
	    }
	    return false;
	}
	
	@Override
	public boolean isSubeffectOf(Effects set, 
		Env<AttrContext> env, Resolve rs) {
	    if (super.isSubeffectOf(set, env, rs)) return true;
	    // SE-INVOKES-2
	    return (this.withEffects.areSubeffectsOf(set, env, rs));
	}
	
	public Effect substRPLs(List<RPL> from, List<RPL> to) {
	    return new InvocationEffect(rpls, methSym, 
		    withEffects.substRPLs(from, to));
	}
	
	public Effect substRefGroups(List<RefGroup> from, List<RefGroup> to) {
	    return new InvocationEffect(rpls, methSym,
		    withEffects.substRefGroups(from, to));
	}

	public Effect atCallSite(Resolve rs, Env<AttrContext> env,
		JCMethodInvocation tree) {
	    return new InvocationEffect(rpls, methSym,
		    withEffects.atCallSite(rs, env, tree));
	}
		
	public Effect atNewClass(Resolve rs, Env<AttrContext> env,
		JCNewClass tree) {
	    return new InvocationEffect(rpls, methSym,
		    withEffects.atNewClass(rs, env, tree));
	}

	public Effect asMemberOf(Types types, Type t) {
	    Effects memberEffects = withEffects.asMemberOf(types, t);
	    return (memberEffects == withEffects) ? 
		    this : new InvocationEffect(rpls, methSym, memberEffects);
	}

	public Effect substVars(Permissions perms, List<VarSymbol> from,
		List<JCExpression> to) {
	    Effects result = withEffects.substVars(perms, from, to);
	    return (result == withEffects) ?
		    this : new InvocationEffect(rpls, methSym, result);
	}

	@Override
	public Effect inEnvironment(Resolve rs, Env<AttrContext> env, 
		boolean pruneLocalEffects) {
	    Effects newEffects = withEffects.inEnvironment(rs, env, pruneLocalEffects);
	    return (newEffects == withEffects) ?
		    this : new InvocationEffect(rpls, methSym, newEffects);
	}
	
	@Override
	public int hashCode() {
	    return 5 * this.methSym.hashCode() + this.withEffects.hashCode();
	}
	
	public boolean equals(Object o) {
	    if (!(o instanceof InvocationEffect))
		return false;
	    InvocationEffect ie = (InvocationEffect) o;
	    return this.methSym == ie.methSym && 
		    this.withEffects.equals(ie.withEffects);
	}
	
	public String toString() {
	    return "invokes " + methSym + " with " + withEffects;
	}

    }
    
}
