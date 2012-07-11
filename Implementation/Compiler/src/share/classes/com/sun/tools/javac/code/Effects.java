package com.sun.tools.javac.code;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.tools.javac.code.Effect.MemoryEffect;
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
import com.sun.tools.javac.tree.JCTree.JCNewClass;
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
    
    public boolean remove(Effect effect) {
	return effects.remove(effect);
    }

    public boolean contains(Effect effect) {
	return effects.contains(effect);
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
    
    public Effects atCallSite(Resolve rs, 
	    Env<AttrContext> env, JCMethodInvocation site) {
	Effects result = new Effects();
	for (Effect e : effects)
	    result.add(e.atCallSite(rs, env, site));
	return result;
    }
    
    public Effects atNewClass(Resolve rs, 
	    Env<AttrContext> env, JCNewClass site) {
	Effects result = new Effects();
	for (Effect e : effects)
	    result.add(e.atNewClass(rs, env, site));
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
	    Env<AttrContext> env, Resolve rs) {
        if (effects == UNKNOWN || otherEffects == UNKNOWN) return true;
        for (Effect e : this.effects) {
            if (!e.isSubeffectOf(otherEffects, env, rs)) {
        	return false;
            }
        }
        return true;
    }

    /** @return a set of Effects in this set that are <b>not</b> subeffects 
     * of at least one Effect in the given set 
     */
    public Effects missingFrom(Effects otherEffects, 
	    Env<AttrContext> env, Resolve rs) {
	Effects result = new Effects();
	for (Effect e : effects) {
	    if (!e.isSubeffectOf(otherEffects, env, rs))
	        result.add(e);
	}
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
	    boolean pruneLocalEffects, boolean pruneEffectsOnThis) {
	Effects newEffects = new Effects();
	boolean changed = false;
	for (Effect e : effects) {
	    Effect newEffect = e.inEnvironment(rs, env, pruneLocalEffects,
		    pruneEffectsOnThis);
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
     * Mask effects via unique permissions
     */
    public Effects maskEffectsViaUnique(Attr attr, Env<AttrContext> env) {
	Effects newEffects = new Effects();
	boolean changed = false;
	for (Effect e : effects) {
	    Effect newEffect = e.maskEffectViaUnique(attr, env);
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
	    Effects effects2, Env<AttrContext> env, RPLs rpls, 
	    Constraints constraints) {
	for (Effect e: effects1) {
	    if (!e.isNoninterferingWith(effects2, env, rpls, constraints))
		return false;
	}
	return true;
    }
    
    /** Trim effects to minimal set
     */
    public Effects trim(Env<AttrContext> env, Resolve rs) {
	Effects newEffects = new Effects();
	for (Effect e : effects) {
	    if (!e.isSubeffectOf(newEffects, env, rs))
		newEffects.effects.add(e);
	}
	return newEffects;
    }

    /** Coarsen effects with updated groups
     */
    public Effects coarsenWith(Set<RefGroup> updatedGroups,
	    Attr attr, Env<AttrContext> env) {
	Effects newEffects = new Effects();
	for (Effect e : effects) {
	    newEffects.effects.add(e.coarsenWith(updatedGroups,
		    attr, env));
	}
	return newEffects;
    }

}
