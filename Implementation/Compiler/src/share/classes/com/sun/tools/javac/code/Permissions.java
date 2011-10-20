package com.sun.tools.javac.code;

import java.util.HashSet;

import com.sun.tools.javac.code.Permission.EnvPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.CopyPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.PreservedGroupPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.UpdatedGroupPerm;
import com.sun.tools.javac.code.Permission.RefPerm;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.Context;

public class Permissions {
    protected static final Context.Key<Permissions> permissionsKey =
	        new Context.Key<Permissions>();

    public static Permissions instance(Context context) {
	Permissions instance = context.get(permissionsKey);
	if (instance == null)
	    instance = new Permissions(context);
	        return instance;
    }

    protected Permissions(Context context) {

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
		    newSet.add(newPerm);
		}
	    }  
	    // If the new perm requires perm 'preserves G',
	    // then kill all old perms requiring 'updates G'
	    if (newPerm.preservedGroup != RefGroup.NO_GROUP){
		if (!oldPerm.updatesGroup(newPerm.preservedGroup))
		    newSet.add(newPerm);
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
    
}