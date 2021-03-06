package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Permission.EnvPerm.FreshGroupPerm;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * Utility class containing various operations on reference groups.
 * 
 * @author Rob Bocchino
 */

public class RefGroups {
    
    protected static final Context.Key<RefGroups> refGroupsKey =
        new Context.Key<RefGroups>();

    final Symtab syms;

    public static RefGroups instance(Context context) {
        RefGroups instance = context.get(refGroupsKey);
        if (instance == null)
            instance = new RefGroups(context);
        return instance;
    }

    protected RefGroups(Context context) {
	syms = Symtab.instance(context);
    }
    
    public boolean areDisjoint(List<RefGroup> groups) {
	while (groups.nonEmpty()) {
	    if (groups.tail.contains(groups.head)) {
		return false;
	    }
	    groups = groups.tail;
	}
	return true;
    }

    
}
