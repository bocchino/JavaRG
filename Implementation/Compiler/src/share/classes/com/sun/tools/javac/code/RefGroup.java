package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.RefGroupNameSymbol;
import com.sun.tools.javac.code.Symbol.RefGroupParameterSymbol;
import com.sun.tools.javac.code.Symbol.RefGroupSymbol;
import com.sun.tools.javac.util.List;

public abstract class RefGroup {
    
    public abstract RefGroupSymbol getSymbol();
    
    public RefGroup subst(List<RefGroup> from, List<RefGroup> to) {
	while (from.nonEmpty() && to.nonEmpty()) {
	    if (from.head.equals(this)) return to.head;
	    from = from.tail;
	    to = to.tail;
	}
	return this;
    }
    
    public static class RefGroupName extends RefGroup {
	
	RefGroupNameSymbol sym;
	
	public RefGroupName(RefGroupNameSymbol sym) {
	    this.sym = sym;
	}

	@Override public RefGroupSymbol getSymbol() {
	    return this.sym;
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
	
	@Override public boolean equals(Object obj) {
	    if (!(obj instanceof RefGroupName)) return false;
	    return sym.equals(((RefGroupName) obj).sym);
	}
	
    }

    public static class RefGroupParameter extends RefGroup {
	
	RefGroupParameterSymbol sym;
	
	public RefGroupParameter(RefGroupParameterSymbol sym) {
	    this.sym = sym;
	}

	@Override public RefGroupSymbol getSymbol() {
	    return this.sym;
	}

	@Override public String toString() {
	    return sym.toString();
	}
	
	@Override public boolean equals(Object obj) {
	    if (!(obj instanceof RefGroupParameter)) return false;
	    return sym.equals(((RefGroupParameter) obj).sym);
	}

    }
    
}