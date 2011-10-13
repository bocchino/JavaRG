package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Permission.RefPerm;
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
     * Return 'null' if left can't be split out of right.
     */
    public RefPerm split(RefPerm left, RefPerm right) {
	if (left.equals(RefPerm.SHARED))
	    return right;
	else if (left.equals(right))
	    return RefPerm.SHARED;
	return null;
    }
    
}