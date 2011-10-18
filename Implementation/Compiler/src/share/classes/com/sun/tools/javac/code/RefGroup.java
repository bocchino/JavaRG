package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Mappings.AsMemberOf;
import com.sun.tools.javac.code.Mappings.SubstRefGroups;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.RefGroupNameSymbol;
import com.sun.tools.javac.code.Symbol.RefGroupParameterSymbol;
import com.sun.tools.javac.code.Symbol.RefGroupSymbol;
import com.sun.tools.javac.util.List;

public abstract class RefGroup 
	implements SubstRefGroups<RefGroup>, AsMemberOf<RefGroup> 
{
    
    public RefGroupSymbol getSymbol() { return null; }
    
    public RefGroup substRefGroups(List<RefGroup> from, List<RefGroup> to) {
	return this;
    }
    
    public RefGroup asMemberOf(Types types, Type t) {
	return this;
    }
    
    public static final RefGroup NO_GROUP = new RefGroup() {
	@Override public String toString() {
	    return "[no group]";
	}
    };
    
    public static class RefGroupName extends RefGroup {
	
	private final RefGroupNameSymbol sym;
	
	public RefGroupName(RefGroupNameSymbol sym) {
	    this.sym = sym;
	}

	@Override public RefGroupSymbol getSymbol() {
	    return this.sym;
	}

	@Override public String toString() {
	    return sym.toString();
	}
	
	@Override public RefGroup substRefGroups(List<RefGroup> from, 
		List<RefGroup> to) {
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
	
	@Override public int hashCode() {
	    return sym.hashCode();
	}
	
    }

    public static class RefGroupParameter extends RefGroup {
	
	private final RefGroupParameterSymbol sym;
	
	public RefGroupParameter(RefGroupParameterSymbol sym) {
	    this.sym = sym;
	}

	@Override public RefGroupSymbol getSymbol() {
	    return this.sym;
	}

	@Override public RefGroup substRefGroups(List<RefGroup> from, 
		List<RefGroup> to) {
	    while (from.nonEmpty() && to.nonEmpty()) {
		if (from.head.equals(this)) return to.head;
		from = from.tail;
		to = to.tail;
	    }
	    return this;
	}

	@Override public RefGroup asMemberOf(Types types, Type t) {
	    RefGroup result = this;
	    Symbol enclosingClass = this.sym.owner;
	    if (enclosingClass instanceof MethodSymbol) {
		enclosingClass = sym.owner;
	    }
	    if (enclosingClass.type.hasRefGroupParams()) {
		Type baseClass = types.asOuterSuper(t, enclosingClass);
		if (baseClass != null) {
		    List<RefGroup> from = enclosingClass.type.allRefGroups();
		    List<RefGroup> to = baseClass.allRefGroups();
		    result = result.substRefGroups(from, to);
		}
	    }
	    return result;
	}
	
	@Override public String toString() {
	    return sym.toString();
	}
	
	@Override public boolean equals(Object obj) {
	    if (!(obj instanceof RefGroupParameter)) return false;
	    return sym.equals(((RefGroupParameter) obj).sym);
	}

	@Override public int hashCode() {
	    return sym.hashCode();
	}

    }
    
}