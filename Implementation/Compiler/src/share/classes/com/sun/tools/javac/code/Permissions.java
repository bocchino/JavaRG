package com.sun.tools.javac.code;

import java.util.HashSet;

import com.sun.tools.javac.code.Permission.EnvPerm;
import com.sun.tools.javac.code.Permission.RefPerm;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

public class Permissions {
    protected static final Context.Key<Permissions> permissionsKey =
	        new Context.Key<Permissions>();

    public static Permissions instance(Context context) {
	Permissions instance = context.get(permissionsKey);
	if (instance == null)
	    instance = new Permissions(context);
	        return instance;
    }

    public TreeMaker maker;
    
    protected Permissions(Context context) {
	maker = TreeMaker.instance(context);
    }
    
    /**
     * Split left out of right and return the remainder.
     * Return RefPerm.ERROR if left can't be split out of right.
     */
    public RefPerm split(RefPerm left, RefPerm right) {
	if (left.equals(RefPerm.SHARED))
	    return right;
	else if (left.equals(right))
	    return RefPerm.SHARED;
	return RefPerm.ERROR;
    }
    
    /**
     * Add an env perm to set of env perms, producing a new set.
     * Omit any perms from the old set that are inconsistent with the 
     * new perm.
     */    
    public HashSet<EnvPerm> addEnvPerm(HashSet<EnvPerm> oldSet,
	    EnvPerm newPerm) {
	HashSet<EnvPerm> newSet = new HashSet();
	newSet.add(newPerm);
	for (EnvPerm oldPerm : oldSet) {
	    // If the new perm requires perm 'updates G',
	    // then kill all old perms requiring 'preserves G'
	    if (newPerm.updatedGroup != RefGroup.NO_GROUP) {
		if (!oldPerm.preservesGroup(newPerm.updatedGroup)) {
		    newSet.add(oldPerm);
		}
	    }  
	    // If the new perm requires perm 'preserves G',
	    // then kill all old perms requiring 'updates G'
	    if (newPerm.preservedGroup != RefGroup.NO_GROUP){
		if (!oldPerm.updatesGroup(newPerm.preservedGroup))
		    newSet.add(oldPerm);
	    }
	}
	return newSet;
    }
    
    public HashSet<EnvPerm> killPermsByAssigningTo(HashSet<EnvPerm> oldSet,
	    VarSymbol var) {
	HashSet<EnvPerm> newSet = new HashSet();
	for (EnvPerm oldPerm : oldSet) {
	    if (!oldPerm.isKilledByAssigningTo(var)) {
		newSet.add(oldPerm);
	    }
	}
	return newSet;
    }
    

    
    /**
     * e must be chain of field or array access
     */
    public boolean isValidDerefExp(JCExpression e) {
	if (e instanceof JCIdent)
	    return true;
	if (e instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) e;
	    return isValidDerefExp(fa.selected);
	}
	if (e instanceof JCArrayAccess) {
	    JCArrayAccess aa = (JCArrayAccess) e;
	    return isValidDerefExp(aa.indexed);
	}
	return false;
    }
    
    /**
     * Substitute an expression for the variable in a deref expression
     */
    public JCExpression substVars(JCExpression e, 
	    List<VarSymbol> from, List<JCExpression> to) {
	if (e instanceof JCIdent) {
	    while (from.nonEmpty() && to.nonEmpty()) {
		if (e.getSymbol().equals(from.head)) {
		    return to.head;
		}
		from = from.tail;
		to = to.tail;
	    }
	}
	else if (e instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) e;
	    JCExpression newSelected =
		    substVars(fa.selected, from, to);
	    if (newSelected != fa.selected) {
		JCFieldAccess result = maker.Select(newSelected, fa.name);
		result.pos = e.pos;
		result.type = e.type;
		return result;
	    }
	}
	else if (e instanceof JCArrayAccess) {
	    JCArrayAccess aa = (JCArrayAccess) e;
	    JCExpression newIndexed =
		    substVars(aa.indexed, from, to);
	    if (newIndexed != aa.indexed) {
		JCArrayAccess result = maker.Indexed(newIndexed,
			aa.index);
		result.pos = e.pos;
		result.type = e.type;
		return result;
	    }
	}
	return e;
    }
    
    /**
     * Compare access chains for equality
     */
    public static boolean matchingExprs(JCExpression e1,
	    JCExpression e2) {
	if (e1 instanceof JCIdent) {
	    if (!(e2 instanceof JCIdent))
		return false;
	    return e1.getSymbol().equals(e2.getSymbol());
	}
	if (e1 instanceof JCFieldAccess) {
		    if (!(e2 instanceof JCFieldAccess))
			return false;
		    JCFieldAccess fa1 = (JCFieldAccess) e1;
		    JCFieldAccess fa2 = (JCFieldAccess) e2;
		    return matchingExprs(fa1.selected, fa2.selected) &&
			    fa1.name.equals(fa2.name);
	}
	if (e1 instanceof JCArrayAccess) {
	    if (!(e2 instanceof JCArrayAccess))
		return false;
	    JCArrayAccess aa1 = (JCArrayAccess) e1;
	    JCArrayAccess aa2 = (JCArrayAccess) e2;
	    return matchingExprs(aa1.index, aa2.index) &&
		    matchingExprs(aa1.indexed, aa2.indexed);
	}
	return false;
    }

    
}