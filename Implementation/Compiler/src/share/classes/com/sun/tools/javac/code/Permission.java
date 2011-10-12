package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.RefGroupSymbol;

public abstract class Permission {
    
    public static abstract class RefPerm extends Permission {
	
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
	
	@Override public String toString() {
	    return "unique(" + refGroup + ")";
	}
	
    }
    
    public static abstract class CopyPermission extends Permission {
	
    }
    
    public static abstract class EffectPermission extends Permission {
	
    }
    
    public static abstract class UpdatePermission extends Permission {
	
    }
    
}