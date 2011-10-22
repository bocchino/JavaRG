package com.sun.tools.javac.code;

import java.util.HashSet;

import com.sun.tools.javac.code.Permission.EnvPerm;
import com.sun.tools.javac.code.Permission.EnvPerm.CopyPerm;
import com.sun.tools.javac.code.Permission.RefPerm;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

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
    public Check chk;
    public Attr attr;
    
    protected Permissions(Context context) {
	maker = TreeMaker.instance(context);
	chk   = Check.instance(context);
	attr  = Attr.instance(context);
    }
    
    /**
     * Split left out of right and return the remainder.
     * Return RefPerm.ERROR if left can't be split out of right.
     */
    public RefPerm split(RefPerm left, RefPerm right) {
	if (left.equals(RefPerm.SHARED))
	    return right;
	if (left.equals(right))
	    return RefPerm.SHARED;
	return RefPerm.ERROR;
    }
    
    /**
     * Try to split a permission.  If that doesn't work, try 
     * to use a copy permission from env.  
     * 
     * Return the remainder in the original expression if either 
     * attempt succeeds.  Return RefPerm.ERROR if both attempts fail.
     */
    public RefPerm splitOrCopy(Types types, RefPerm leftPerm, 
	    RefPerm rightPerm, JCExpression rightExpr, Env<AttrContext> env) {
	RefPerm remainder = split(leftPerm, rightPerm);
	if (remainder != RefPerm.ERROR) {
	    // OK, we got what we wanted by splitting
	    return remainder;
	}
	// We didn't get it, so create a copy permission for what we want,
	// if legal to do so
	if (!isValidDerefExp(rightExpr, types)) {
	    // We can only do this if the right-hand expression is a valid
	    // deref expression
	    return RefPerm.ERROR;
	}
	if (env.info.scope.inParallelBlock && 
		env.info.forIndexVars.nonEmpty()) {
	    // We're in a parallel for loop.  We can only do this
	    // if the right-hand expression uses the innermost loop index 
	    // var to index into an array.
	    boolean ok = false;
	    if (rightExpr instanceof JCArrayAccess) {
		JCArrayAccess aa = (JCArrayAccess) rightExpr;
		ok = env.info.forIndexVars.head.equals(aa.index.getSymbol());
	    }
	    if (!ok) {
		return RefPerm.ERROR;
	    }	    
	}
	RefGroup targetGroup = leftPerm.getRefGroup();
	CopyPerm copyPerm = CopyPerm.simplePerm(rightExpr, targetGroup);
	if (chk.consumeEnvPerms(rightExpr.pos(),
		List.<EnvPerm>of(copyPerm), env)) {
	    // OK, we got the copy permission and used it
	    return rightPerm;
	}	
	// We didn't get it
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
    public boolean isValidDerefExp(JCExpression e, Types types) {
	if (e instanceof JCIdent)
	    return true;
	if (e instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) e;
	    return isValidDerefExp(fa.selected, types);
	}
	if (e instanceof JCArrayAccess) {
	    JCArrayAccess aa = (JCArrayAccess) e;
	    // Indexed expression must be JRG array class, not Java array
	    if (!types.isArrayClass(aa.indexed.type))
		return false;
	    // Index expression must be a local variable
	    if (!(aa.index instanceof JCIdent))
		return false;
	    if (!(aa.index.getSymbol() instanceof VarSymbol))
		return false;
	    // Indexed expression must be recursively valid
	    return isValidDerefExp(aa.indexed, types);
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
		result.sym = fa.sym;
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
    
    /**
     * Merge two env perms sets into one
     */
    public HashSet<EnvPerm> mergeEnvPerms(HashSet<EnvPerm> set1, 
	    HashSet<EnvPerm> set2, Env<AttrContext> env) {
	HashSet<EnvPerm> result = new HashSet<EnvPerm>();
	for (EnvPerm perm : set1) {
	    if (set2.contains(perm)) result.add(perm);
	    else if (perm instanceof CopyPerm) {
		CopyPerm copyPerm = (CopyPerm) perm;
		if (copyPerm.isMultipleTreePerm()) {
		    // Look for the corresponding multiple tree perm in set 2
		    for (EnvPerm set2Perm : set2) {
			if (copyPerm.equals(set2Perm)) {
			    // Found it.
			    // Take the union of consumed fields of the two perms.
			    CopyPerm set2CopyPerm = (CopyPerm) set2Perm;
			    HashSet<Name> names = new HashSet<Name>();
			    for (Name name : copyPerm.consumedFields) {
				names.add(name);
			    }
			    for (Name name : set2CopyPerm.consumedFields) {
				names.add(name);
			    }
			    ListBuffer<Name> lb = ListBuffer.lb();
			    for (Name name : names) {
				lb.append(name);
			    }
			    // Add a new copy perm with the union of the consumed
			    // fields.
			    CopyPerm newPerm = CopyPerm.multipleTreePerm(copyPerm.exp, 
				    lb.toList(), copyPerm.sourceGroup, 
				    copyPerm.targetGroup);
			    result.add(newPerm);
			    break;
			}
		    }
		}
	    }
	}
	// Find all the non-multiple tree copy perms in set2 that are not 
	// directly in set1 but are covered by a multiple perm in set 1.
	for (EnvPerm perm : set2) {
	    if (perm instanceof CopyPerm) {
		CopyPerm copyPerm = (CopyPerm) perm;
		if (copyPerm.isTreePerm() && !copyPerm.isMultipleTreePerm()) {
		    if (!result.contains(copyPerm)) {
			for (EnvPerm set1Perm : set1) {
			    if (set1Perm instanceof CopyPerm) {
				CopyPerm set1CopyPerm = (CopyPerm) set1Perm;
				if (set1CopyPerm.representsPerm(copyPerm,
					attr, env)) {
				    result.add(copyPerm);
				}
			    }
			}
		    }
		}
	    }
	}
	return result;
    }


    
}