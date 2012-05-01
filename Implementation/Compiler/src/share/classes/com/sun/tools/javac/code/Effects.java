package com.sun.tools.javac.code;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.tools.javac.code.Effect.MemoryEffect;
import com.sun.tools.javac.code.Permission.EnvPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.EffectPerm;
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
import com.sun.tools.javac.tree.JCTree.JCTreeWithEffects;
import com.sun.tools.javac.util.List;

/**
 * A collection class representing a set of effects.
 */
public class Effects implements 
	Iterable<Effect>,
	SubstRPLs<Effects>,
	SubstRefGroups<Effects>,
	AsMemberOf<Effects>,
	AtCallSite<Effects>,
	SubstVars<Effects>
{
    private Set<Effect> effects = new HashSet<Effect>();
    
    public static final Effects UNKNOWN = new Effects();
    
    public Effects() {}
    
    public Effects(Effect effect) {
	add(effect);
    }
    
    public static Effects makeEffectsFrom(RPLs rpls, List<EffectPerm> perms) {
	Effects effects = new Effects();
	for (EffectPerm perm : perms) {
	    effects.add(MemoryEffect.makeEffectFrom(rpls, perm));
	}
	return effects;
    }
    
    public void add(Effect effect) {
	effects.add(effect);
    }

    public void addAll(Effects otherEffects) {
	for (Effect e : otherEffects)
	    this.add(e);
    }

    public void addAllEffects(List<? extends JCTreeWithEffects> list) {
	for (JCTreeWithEffects tree : list)
	    this.addAll(tree.effects);
    }

    public boolean isEmpty() {
	return effects.isEmpty();
    }
    
    public Effects substRPLs(List<RPL> from, List<RPL> to) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    result.add(e.substRPLs(from, to));
	}
	return result;
    }
    
    public Effects substRefGroups(List<RefGroup> from, List<RefGroup> to) {
	Effects result = new Effects();
	for (Effect e : effects)
	    result.add(e.substRefGroups(from, to));
	return result;
    }
    
    public Effects asMemberOf(Types types, Type t) {
	Effects memberEffects = new Effects();
	for (Effect e : effects) {
	    memberEffects.add(e.asMemberOf(types, t));
	}
	return memberEffects;
    }
    
    public Effects atCallSite(Types types, Permissions perms,
	    JCMethodInvocation site) {
	Effects result = new Effects();
	for (Effect e : effects)
	    result.add(e.atCallSite(types, perms, site));
	return result;
    }
    
    public Effects substVars(Permissions perms, List<VarSymbol> from,
	    List<JCExpression> to) {
	Effects result = new Effects();
	for (Effect e : effects)
	    result.add(e.substVars(perms, from, to));
	return result;
    }

    public Iterator<Effect> iterator() {
	return effects.iterator();
    }
    
    @Override public String toString() {	
	return effects.toString();
    }
    
    @Override
    public int hashCode() {
	return this.effects.hashCode();
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Effects))
	    return false;
	return effects.equals(((Effects)o).effects);
    }

    /** @return true iff every Effect in this set is a subeffect of at least one 
     * Effect in the given set 
     */
    public boolean areSubeffectsOf(Effects otherEffects,
	    Attr attr, Env<AttrContext> env) {
        if (effects.isEmpty()) return true;
        if (effects == UNKNOWN || otherEffects == UNKNOWN) return true;

        for (Effect e : this.effects) {
            if (!e.isSubeffectOf(otherEffects, attr, env)) {
        	return false;
            }
        }
        return true;
    }

    /** @return a set of Effects in this set that are <b>not</b> subeffects 
     * of at least one Effect in the given set 
     */
    public Effects missingFrom(Effects otherEffects, Attr attr,
	    Env<AttrContext> env) {
	Effects result = new Effects();
	for (Effect e : effects)
	    if (!e.isSubeffectOf(otherEffects, attr, env))
	        result.add(e);
	return result;
    }
    
    /**
     * The effect set as it appears in the environment env:
     * - RPLs that refer to out-of-scope variables get converted to 
     *   more general RPLs
     * - RPLs that refer to out-of-scope local region names get
     *   deleted
     * - Stack regions corresponding to out-of-scope local variables
     *   get deleted
     */
    public Effects inEnvironment(Resolve rs, Env<AttrContext> env,
	    boolean pruneLocalEffects) {
	Effects newEffects = new Effects();
	boolean changed = false;
	for (Effect e : effects) {
	    Effect newEffect = e.inEnvironment(rs, env, pruneLocalEffects);
	    if (newEffect == null) {
		changed = true;
	    } else {
		newEffects.add(newEffect);
		if (newEffect != e) changed = true;
	    }
	}
	return changed ? newEffects : this;
    }
    
    /**
     * Check whether two effect sets are noninterfering
     */
    public static boolean noninterferingEffects(Effects effects1, 
	    Effects effects2, Constraints constraints) {
	for (Effect e: effects1) {
	    if (!e.isNoninterferingWith(effects2, constraints))
		return false;
	}
	return true;
    }
    
    /** Trim effects to minimal set
     */
    public Effects trim(Attr attr, Env<AttrContext> env) {
	Effects newEffects = new Effects();
	newEffects.effects.addAll(this.effects);
	boolean changed = false;
	for (Effect e : effects) {
	    newEffects.effects.remove(e);
	    if (e.isSubeffectOf(newEffects, attr, env)) {
		changed = true;
	    } else {
		newEffects.effects.add(e);
	    }
	}
	return changed ? newEffects : this;
    }

}
