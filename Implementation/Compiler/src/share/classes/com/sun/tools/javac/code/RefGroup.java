package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.RefGroupNameSymbol;
import com.sun.tools.javac.code.Symbol.RefGroupParameterSymbol;

public abstract class RefGroup {
    
    public static class RefGroupName extends RefGroup {
	
	RefGroupNameSymbol sym;
	
	@Override public String toString() {
	    return sym.toString();
	}
	
    }

    public static class RefGroupParameter extends RefGroup {
	
	RefGroupParameterSymbol sym;
	
	public RefGroupParameter(RefGroupParameterSymbol sym) {
	    this.sym = sym;
	}

	@Override public String toString() {
	    return sym.toString();
	}
	
    }
    
}