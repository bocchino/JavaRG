package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.RefGroupSymbol;

public abstract class Permission {
    
    public static abstract class RefPerm extends Permission {
	
	/**
	 * The reference permission 'this' as a member of type t
	 */
	public RefPerm asMemberOf(Types types, Type t) {
	    return this;
	}
	
    }
    
    public static final RefPerm SHARED = new RefPerm() {
        @Override public String toString() {
            return "shared";
        }
    };
    
    public static class LocallyUnique extends RefPerm {
	
	RefGroup refGroup;

	public LocallyUnique(RefGroup refGroup) {
	    this.refGroup = refGroup;
	}

	@Override public RefPerm asMemberOf(Types types, Type t) {
	    RefGroup refGroup = this.refGroup.asMemberOf(types, t);
	    return (this.refGroup == refGroup) ?
		    this: new LocallyUnique(refGroup);
	}
	
	@Override public String toString() {
	    return "unique(" + refGroup + ")";
	}
	
	@Override public boolean equals(Object obj) {
	    if (!(obj instanceof LocallyUnique)) return false;
	    return this.refGroup.equals(((LocallyUnique) obj).refGroup);
	}
	
    }
    
    public static abstract class CopyPermission extends Permission {
	
    }
    
    public static abstract class EffectPermission extends Permission {
	
    }
    
    public static abstract class UpdatePermission extends Permission {
	
    }
    
}