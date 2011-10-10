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

    public static class RefGroupParam extends RefGroup {
	
	RefGroupParameterSymbol sym;

	@Override public String toString() {
	    return sym.toString();
	}
	
    }
    
}