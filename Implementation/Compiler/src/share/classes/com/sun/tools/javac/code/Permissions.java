package com.sun.tools.javac.code;

import java.util.HashSet;

import com.sun.tools.javac.code.Substitutions.SubstRefGroups;
import com.sun.tools.javac.code.Permission.EnvPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.FreshGroupPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.PreservedGroupPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.UpdatedGroupPerm;
import com.sun.tools.javac.code.Permission.RefPerm;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

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
    
    public HashSet<EnvPerm> addPreservedGroupPerm(HashSet<EnvPerm> oldSet, 
	    PreservedGroupPerm newPerm) {
	HashSet<EnvPerm> newSet = new HashSet();
	newSet.add(newPerm);
	for (EnvPerm oldPerm : oldSet) {
	    if (!oldPerm.updatesGroup(newPerm.refGroup))
		newSet.add(oldPerm);
	}
	return newSet;
    }
    
    public HashSet<EnvPerm> addUpdatedGroupPerm(HashSet<EnvPerm> oldSet, 
	    UpdatedGroupPerm newPerm) {
	HashSet<EnvPerm> newSet = new HashSet();
	newSet.add(newPerm);
	for (EnvPerm oldPerm : oldSet) {
	    if (!oldPerm.preservesGroup(newPerm.refGroup)) {
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
    
}