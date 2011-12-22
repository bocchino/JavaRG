

package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.RefGroupParameterSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.RegionParameterSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

/** A class to represent a DPJ effect
 */
public abstract class Effect {
    
    /** Is this an atomic effect? */
    protected boolean isAtomic;
    public boolean isAtomic() { return isAtomic && !isNonint; }
    
    /** Is this a nonint effect? */
    protected boolean isNonint;
    public boolean isNonint() { return isNonint; }
    
    public RPLs rpls;
    
    protected Effect(RPLs rpls, boolean isAtomic, boolean isNonint) {
	this.rpls = rpls;
	this.isNonint = isNonint;
	this.isAtomic = isAtomic && !isNonint;
    }
    

    /**
     * Do all the RPL and effect parameter substitutions implied by the bindings of t
     */
    public Effects substForAllParams(Type t) {
	Effect e = this.substForParams(t.getRegionParams(), 
		t.getRegionActuals());
	e = e.substForTRParams(t.tsym.type.getTypeArguments(),
		t.getTypeArguments());
	/*
    	return e.substForEffectVars(t.tsym.type.getEffectArguments(), 
    		t.getEffectArguments());
    		*/
	return new Effects(e);
    }

    public Effect substForParams(List<RegionParameterSymbol> from, List<RPL> to) {
	return this;
    }
    
    public Effect substForTRParams(List<Type> from, List<Type> to) {
	return this;
    }
    
    public Effects substForEffectVars(List<Effects> from, List<Effects> to) {
	return new Effects(this);
    }
    
    public Effect substForThis(RPL rpl) {
	return this;
    }
    
    public Effect substForVars(List<VarSymbol> from, List<VarSymbol> to) {
	return this;
    }
    
    public Effect substExpsForVars(List<VarSymbol> from, List<JCExpression> to) {
	return this;
    }
    
    public Effect inEnvironment(Resolve rs, Env<AttrContext> env, 
	    boolean pruneLocalEffects) {
	return this;
    }
    
    public Effects asMemberOf(Types types, Type t, Symbol owner) {
	return new Effects(this);
    }

    /**
     * Subeffects -- See Section 1.4.2 of the DPJ Tech Report
     */
    public abstract boolean isSubeffectOf(Effect e);
    
    public boolean isSubeffectOf(Effects effects) {
	// SE-UNION-1
	if (effects.isEmpty()) return false;
	Effect e = effects.first();
	if (this.isSubeffectOf(e)) return true;
	return this.isSubeffectOf(effects.without(e));
    }
    
    /**
     * SE-ATOMIC-1, SE-ATOMIC 2
     */
    public static boolean isSubeffectAtomic(Effect e1, Effect e2) {
	return e1.isAtomic() || !e2.isAtomic();
    }
    
    /**
     * SE-NONINT-1, SE-NONINT-2
     */
    public static boolean isSubeffectNonint(Effect e1, Effect e2) {
	return e2.isNonint() || !e1.isNonint();
    }
    
    /**
     * NONDET-ATOMIC
     */
    public static boolean isNondetAtomic(Effect e1, Effect e2) {
	return e1.isAtomic() && e2.isAtomic();
    }
    
    /**
     * Noninterfering effects -- See section 3.3 of Tech Report
     */
    public abstract boolean isNoninterferingWith(Effect e, 
	    Constraints constraints, boolean atomicOK);
    
    public boolean isNoninterferingWith(Effects effects,
	    Constraints constraints, boolean atomicOK) { 
	// NI-EMPTY
	if (effects.isEmpty()) return true;
	// NI-UNION
	Effect e = effects.first();
	return this.isNoninterferingWith(e, constraints, atomicOK) && 
		this.isNoninterferingWith(effects.without(e), constraints, atomicOK);
    }
    
    /**
     * Return this effect as it appears in an atomic statement
     */
    public  Effect inAtomic() {
	return this;
    }
    
    /**
     * Return this effect as it appears in a nonint statement
     */
    public Effect inNonint() {
	return this;
    }
	    
    /**
     * Capture the effect
     */
    public Effect capture() { return new CapturedEffect(this); }
    
    /** A class for read effects
     */
    public static class ReadEffect extends Effect {
	public RPL rpl;
	public ReadEffect(RPLs rpls, RPL rpl, boolean isAtomic,
		boolean isNonint) {
	    super(rpls, isAtomic, isNonint);
	    this.rpl = rpl;
	}
	
	public boolean isSubeffectOf(Effect e) {
	    if (!isSubeffectAtomic(this, e)) return false;
	    if (!isSubeffectNonint(this, e)) return false;
	    if (e instanceof ReadEffect) {
		// SE-READS
		if (this.rpl.isIncludedIn(((ReadEffect) e).rpl))
		    return true;
	    }
	    if (e instanceof WriteEffect) {
		// SE-READS-WRITES
		if (this.rpl.isIncludedIn(((WriteEffect) e).rpl))
		    return true;
	    }	    
	    return false;
	}
	
	@Override
	public boolean isNoninterferingWith(Effect e, Constraints constraints,
		boolean atomicOK) {
	    if (atomicOK && isNondetAtomic(this, e)) {
		return true;
	    }
	    if (e instanceof ReadEffect) {
		// NI-READ
		return true;
	    }
	    if (e instanceof WriteEffect) {
		// NI-WRITE
		if (rpls.areDisjoint(this.rpl, ((WriteEffect) e).rpl, 
			constraints.disjointRPLs))
		    return true;
	    }
	    if (e instanceof InvocationEffect) {
		// NI-INVOKES-1
		if (this.isNoninterferingWith(((InvocationEffect) e).withEffects,
			constraints, atomicOK))
		    return true;
	    }
	    return false;
	}
	
	@Override
	public Effect substForParams(List<RegionParameterSymbol> from, List<RPL> to) {
	    return new ReadEffect(rpls, rpl.substForParams(from, to), 
		    this.isAtomic(), this.isNonint());
	}
	
	@Override
	public Effects asMemberOf(Types types, Type t, Symbol owner) {
	    RPL memberRPL = rpl.asMemberOf(types, t, owner);
	    return new Effects(memberRPL.equals(rpl) ? this : 
		new ReadEffect(rpls, memberRPL, this.isAtomic(),
			this.isNonint()));
	}
	
	@Override
	public Effect inEnvironment(Resolve rs, Env<AttrContext> env, 
		boolean pruneLocalEffects) {
	    RPL newRPL = rpl.inEnvironment(rs, env, pruneLocalEffects);
	    if (newRPL == null) return null;
	    return newRPL.equals(rpl) ? this : 
		new ReadEffect(rpls, newRPL, this.isAtomic(),
			this.isNonint());
	}

	@Override
	public Effect inAtomic() {
	    if (this.isAtomic() || this.isNonint()) return this;
	    return new ReadEffect(this.rpls, this.rpl, 
		    true, false);
	}
	
	@Override
	public Effect inNonint() {
	    if (this.isNonint()) return this;
	    return new ReadEffect(this.rpls, this.rpl,
		   false, true);
	}
	
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    sb.append("reads ");
	    if (this.isAtomic())
		sb.append("atomic ");
	    sb.append(rpl);
	    return sb.toString();
	}
	
	@Override
	public int hashCode() {
	    return 3 * this.rpl.hashCode();
	}
	
	public boolean equals(Object o) {
	    if (!(o instanceof ReadEffect))
		return false;
	    return this.rpl.equals(((ReadEffect) o).rpl);
	}

    }
    
    /** A class for write effects
     */
    public static class WriteEffect extends Effect {
	
	public RPL rpl;
	
	public WriteEffect(RPLs rpls, RPL rpl, boolean isAtomic,
		boolean isNonint) {
	    super(rpls, isAtomic, isNonint);
	    this.rpl = rpl;
	}
	
	public boolean isSubeffectOf(Effect e) {
	    if (!isSubeffectAtomic(this, e)) return false;
	    if (!isSubeffectNonint(this, e)) return false;
	    if (e instanceof WriteEffect) {
		// SE-WRITES
		if (this.rpl.isIncludedIn(((WriteEffect) e).rpl))
		    return true;
	    }	    
	    return false;
	}
	
	@Override
	public boolean isNoninterferingWith(Effect e, 
		Constraints constraints, boolean atomicOK) {
	    if (atomicOK && isNondetAtomic(this, e)) {
		return true;
	    }
	    if (e instanceof ReadEffect) {
		// NI-READ
		if (rpls.areDisjoint(this.rpl, ((ReadEffect) e).rpl, 
			constraints.disjointRPLs))
		    return true;
	    }
	    if (e instanceof WriteEffect) {
		// NI-WRITE
		if (rpls.areDisjoint(this.rpl, ((WriteEffect) e).rpl, 
			constraints.disjointRPLs))
		    return true;
	    }
	    if (e instanceof InvocationEffect) {
		// NI-INVOKES-1
		if (this.isNoninterferingWith(((InvocationEffect) e).withEffects,
			constraints, atomicOK))
		    return true;
	    }
	    return false;
	}
	
	@Override
	public Effect substForParams(List<RegionParameterSymbol> from, List<RPL> to) {
	    return new WriteEffect(rpls, rpl.substForParams(from, to), 
		    this.isAtomic(), this.isNonint());
	}
	
	@Override
	public Effects asMemberOf(Types types, Type t, Symbol owner) {
	    RPL memberRPL = rpl.asMemberOf(types, t, owner);
	    return new Effects(memberRPL.equals(rpl) ? this : 
		new WriteEffect(rpls, memberRPL, this.isAtomic(),
			this.isNonint()));
	}
	
	@Override
	public Effect inEnvironment(Resolve rs, Env<AttrContext> env,
		boolean pruneLocalEffects) {
	    RPL newRPL = rpl.inEnvironment(rs, env, pruneLocalEffects);
	    if (newRPL == null) return null;
	    return newRPL.equals(rpl) ? this : 
		new WriteEffect(rpls, newRPL, this.isAtomic(), this.isNonint());
	}
	
	@Override
	public Effect inAtomic() {
	    if (this.isAtomic() || this.isNonint()) return this;
	    return new WriteEffect(this.rpls, this.rpl, 
		    true, false);
	}

	@Override
	public Effect inNonint() {
	    if (this.isNonint()) return this;
	    return new WriteEffect(this.rpls, this.rpl,
		    false, true);
	}
	
	@Override
	public int hashCode() {
	    return 7 * this.rpl.hashCode();
	}
	
	public boolean equals(Object o) {
	    if (!(o instanceof WriteEffect))
		return false;
	    return this.rpl.equals(((WriteEffect) o).rpl);
	}
	
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    sb.append("writes ");
	    if (this.isAtomic())
		sb.append("atomic ");
	    sb.append(rpl);
	    return sb.toString();
	}
	
    }
    
    /** A class for invocation effects
     */
    public static class InvocationEffect extends Effect {

	public MethodSymbol methSym;
	
	public Effects withEffects;
	
	public InvocationEffect (RPLs rpls, MethodSymbol methSym, Effects withEffects) {
	    super(rpls, false, false);
	    this.methSym = methSym;
	    this.withEffects = withEffects;
	}
	
	/**
	 * Invocation effects are never "automatically" atomic: they are governed
	 * by their withEffects.
	 */
	public boolean isAtomic() { return false; }

	public boolean isSubeffectOf(Effect e) {
	    if (!isSubeffectAtomic(this, e)) return false;
	    if (!isSubeffectNonint(this, e)) return false;
	    if (e instanceof InvocationEffect) {
		InvocationEffect ie = (InvocationEffect) e;
		// SE-INVOKES-1
		if (this.methSym == ie.methSym && this.withEffects.areSubeffectsOf(ie.withEffects))
		    return true;
	    }
	    Effects effects = new Effects();
	    effects.add(e);
	    if (this.withEffects.areSubeffectsOf(effects))
		return true;
	    return false;
	}
	
	@Override
	public boolean isNoninterferingWith(Effect e, Constraints constraints,
		boolean atomicOK) {
	    if (e.isNoninterferingWith(withEffects, constraints, atomicOK)) return true;
	    if (e instanceof InvocationEffect) {
		InvocationEffect ie = (InvocationEffect) e;
		if (Effects.noninterferingEffects(withEffects, ie.withEffects,
			constraints, atomicOK)) {
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
	public boolean isSubeffectOf(Effects set) {
	    if (super.isSubeffectOf(set)) return true;
	    // SE-INVOKES-2
	    return (this.withEffects.areSubeffectsOf(set));
	}
	
	@Override
	public Effect substForParams(List<RegionParameterSymbol> from, List<RPL> to) {
	    return new InvocationEffect(rpls, methSym, withEffects.substForRegionParams(from, to));
	}
	
	@Override
	public Effect substForTRParams(List<Type> from, List<Type> to) {
	    return new InvocationEffect(rpls, methSym, withEffects.substForTRParams(from, to));
	}
	
	@Override
	public Effect substForVars(List<VarSymbol> from, List<VarSymbol> to) {
	    return new InvocationEffect(rpls, methSym, withEffects.substForVars(from, to));
	}
	
	@Override
	public Effect substExpsForVars(List<VarSymbol> from, List<JCExpression> to) {
	    return new InvocationEffect(rpls, methSym, withEffects.substExpsForVars(from, to));
	}
	
	@Override
	public Effects asMemberOf(Types types, Type t, Symbol owner) {
	    Effects memberEffects = withEffects.asMemberOf(types, t, owner);
	    return new Effects((memberEffects == withEffects) ? 
		    this : new InvocationEffect(rpls, methSym, memberEffects));
	}
	
	@Override
	public Effect inEnvironment(Resolve rs, Env<AttrContext> env, boolean pruneLocalEffects) {
	    Effects newEffects = withEffects.inEnvironment(rs, env, pruneLocalEffects);
	    return (newEffects == withEffects) ?
		    this : new InvocationEffect(rpls, methSym, newEffects);
	}
	
	@Override
	public Effects substForEffectVars(List<Effects> from, List<Effects> to) {
	    Effects newEffects = withEffects.substForEffectVars(from, to);
	    return new Effects(new InvocationEffect(rpls, methSym, newEffects));
	}
	
	@Override
	public Effect inAtomic() {
	    if (this.isAtomic() || this.isNonint()) return this;
	    Effects inAtomicEffects = withEffects.inAtomic();
	    return (inAtomicEffects == withEffects) ? this :
		new InvocationEffect(rpls, methSym, inAtomicEffects);
	}
	
	@Override
	public Effect inNonint() {
	    if (this.isNonint()) return this;
	    Effects inNonintEffects = withEffects.inNonint();
	    return (inNonintEffects == withEffects) ? this :
		new InvocationEffect(rpls, methSym, inNonintEffects);
	}

	@Override
	public int hashCode() {
	    return 11 * this.methSym.hashCode() + this.withEffects.hashCode();
	}
	
	public boolean equals(Object o) {
	    if (!(o instanceof InvocationEffect))
		return false;
	    InvocationEffect ie = (InvocationEffect) o;
	    return this.methSym == ie.methSym && this.withEffects.equals(ie.withEffects);
	}
	
	public String toString() {
	    return "invokes " + methSym + " with " + withEffects;
	}
	
    }
    
    /** A class for variable effects
     */
    public static class VariableEffect extends Effect {
	public RefGroupParameterSymbol sym;
	
	public VariableEffect(RefGroupParameterSymbol sym) {
	    super(null, false, false);
	    this.sym = sym;
	}

	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof VariableEffect)) return false;
	    VariableEffect ve = (VariableEffect) o;
	    return this.sym == ve.sym;
	}

	@Override
	public int hashCode() {
	    return 13 * this.sym.hashCode();
	}

	@Override
	public boolean isNoninterferingWith(Effect e,
		Constraints constraints, boolean atomicOK) {
	    if (atomicOK && isNondetAtomic(this, e)) {
		return true;
	    }
	    return false;
	}

	public final Effect WRITES_ROOT_STAR =
	    new WriteEffect(rpls, 
		    new RPL(List.of(RPLElement.ROOT_ELEMENT, RPLElement.STAR)), 
		    false, true);
	
	@Override
	public boolean isSubeffectOf(Effect e) {
	    if (!isSubeffectAtomic(this, e)) return false;
	    if (!isSubeffectNonint(this, e)) return false;
	    // TODO: We could also allow subeffect constraints
	    if (WRITES_ROOT_STAR.isSubeffectOf(e)) return true;
	    return this.equals(e);
	}
	
	@Override
	public Effects asMemberOf(Types types, Type t, Symbol owner) {
	    Type base = types.asOuterSuper(t, owner);
            //return this.substForEffectVars(base.tsym.type.getEffectArguments(),
	    //base.getEffectArguments());
	    return new Effects(this);
	}
	
	@Override
	public Effects substForEffectVars(List<Effects> from, List<Effects> to) {
	    while (from.nonEmpty() && to.nonEmpty()) {
		VariableEffect ve = from.head.asVariableEffect();
		assert(ve != null);
		if (ve.equals(this)) return to.head;
		from = from.tail;
		to = to.tail;
	    }
	    return new Effects(this);
	}
	
	@Override
	public Effect capture() { return this; }

	public String toString() {
	    return "effect " + sym;
	}
    }
    
    /** A class for captured effects
     */
    public static class CapturedEffect extends Effect {

	public Effect upperBound;
	
	protected CapturedEffect(Effect upperBound) {
	    super(null, false, false);
	    this.upperBound = upperBound;
	}

	@Override
	public boolean isNoninterferingWith(Effect e, Constraints constraints,
		boolean atomicOK) {
	    return e.isNoninterferingWith(upperBound, constraints, atomicOK);
	}

	@Override
	public boolean isSubeffectOf(Effect e) {
	    return upperBound.isSubeffectOf(e);
	}
	
	@Override
	public Effect capture() { return this; }
	
	public String toString() {
	    return "capture of " + upperBound;
	}
    }
}
