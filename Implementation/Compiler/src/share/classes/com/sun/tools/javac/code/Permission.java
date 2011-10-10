package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.RefGroupSymbol;

public abstract class Permission {
    
    public static abstract class ReferencePermission extends Permission {
	
    }
    
    public static final ReferencePermission SHARED = new ReferencePermission() {
        @Override public String toString() {
            return "shared";
        }
    };
    
    public static class LocallyUnique extends ReferencePermission {
	
	RefGroupSymbol refGroup;
	
	@Override public String toString() {
	    return "unique(" + refGroup.name + ")";
	}
	
    }
    
    public static abstract class CopyPermission extends Permission {
	
    }
    
    public static abstract class EffectPermission extends Permission {
	
    }
    
    public static abstract class UpdatePermission extends Permission {
	
    }
    
}