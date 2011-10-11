package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.RefGroupNameSymbol;
import com.sun.tools.javac.code.Symbol.RefGroupParameterSymbol;
import com.sun.tools.javac.util.List;

public abstract class RefGroup {
    
    public RefGroup subst(List<RefGroup> from, List<RefGroup> to) {
	return this;
    }
    
    public static class RefGroupName extends RefGroup {
	
	RefGroupNameSymbol sym;
	
	public RefGroupName(RefGroupNameSymbol sym) {
	    this.sym = sym;
	}
	
	@Override public String toString() {
	    return sym.toString();
	}
	
	@Override public RefGroup subst(List<RefGroup> from, List<RefGroup> to) {
	    while (from.nonEmpty()) {
		if (from.head.equals(this))
		    return to.head;
		from = from.tail;
		to = to.tail;
	    }
	    return this;
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