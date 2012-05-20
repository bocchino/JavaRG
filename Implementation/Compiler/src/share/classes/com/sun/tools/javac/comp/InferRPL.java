package com.sun.tools.javac.comp;

import com.sun.tools.javac.code.RPL;
import com.sun.tools.javac.code.RPLElement;
import com.sun.tools.javac.code.RPLs;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.RPLElement.RPLParameterElement;
import com.sun.tools.javac.code.RPLElement.UndetRPLParameterElement;
import com.sun.tools.javac.code.Symbol.RegionParameterSymbol;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.UndetVar;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Warner;

public class InferRPL {
    
    protected static final Context.Key<InferRPL> inferRPLKey =
	new Context.Key<InferRPL>();

    RPLs rpls;
    Types types;
    Symtab syms;

    public static InferRPL instance(Context context) {
	InferRPL instance = context.get(inferRPLKey);
	if (instance == null)
	    instance = new InferRPL(context);
	return instance;
    }

    protected InferRPL(Context context) {
	context.put(inferRPLKey, this);
	rpls = RPLs.instance(context);
	types = Types.instance(context);
	syms = Symtab.instance(context);
    }

    /**
     * Infer the RPL arguments for the method type mt, given RPL vars
     * rvars, type vars tvars, and actual arg types actualtypes
     */
    public Type instantiateMethod(List<RPL> rvars, List<Type> tvars,
	    MethodType mt, List<Type> actualtypes, boolean allowBoxing, Warner warn) {
	List<RPL> undetvars = makeParamsUndetermined(rvars);
	List<Type> formaltypes = types.substRPLs(mt.argtypes, rvars, undetvars);
	
	for (Type t : tvars) {
	    formaltypes = types.subst(formaltypes, List.of(t), List.<Type>of(new UndetVar(t)));
	}
	// Capture the actual types, so partially specified RPLs are treated consistently.
	// For example, <region R>void foo(Data<R> x, Data<R> y) should not be usable
	// at a call site foo(a,b) where a and b both have type Data<*>.  Otherwise (for
	// example) a and b could be Data<A> and Data<B>, and then inside the foo call 
	// R would simultaneously be an A and a B --- which is bad!
	ListBuffer<Type> capturedTypes = ListBuffer.lb();
	for (Type t : actualtypes)
	    capturedTypes.append(types.capture(t));	
	// Note:  We use the subtype check to resolve the undet variables.  See RPL.isIncludedIn
	// for details.  This is kind of a hack, but it makes for a very lean coding of the
	// algorithm, i.e., no extra machinery is required to do the unification.
	if (!types.isSubtypesUnchecked(capturedTypes.toList(), 
		formaltypes, allowBoxing, warn)) {
	    return null;
	}
	List<RPL> inferredRPLs = getInferredRPLs(undetvars);
	mt = (MethodType) types.substRPLs(mt, rvars, inferredRPLs);
	mt.regionActuals = inferredRPLs;
	return mt;
    }
    
    /**
     * Introduce undetermined parameters into a list of RPLs
     */
    List<RPL> makeParamsUndetermined(List<RPL> list) {
	ListBuffer<RPL> lb = ListBuffer.lb();
	for (RPL rpl : list) {
	    if (rpl.size() == 1 && rpl.elts.head instanceof RPLParameterElement)
		lb.append(new RPL(new 
			UndetRPLParameterElement(((RPLParameterElement) 
				rpl.elts.head).sym)));
	    else
		lb.append(rpl);
	}
	return lb.toList();
    }
    
    /**
     * Extract the computed bindings for the undetermined RPL params
     */
    List<RPL> getInferredRPLs(List<RPL> list) {
	ListBuffer<RPL> lb = ListBuffer.lb();
	for (RPL undetvar : list) {
	    if (undetvar.size() == 1 && 
		    undetvar.elts.head instanceof UndetRPLParameterElement) {
		UndetRPLParameterElement element = 
			(UndetRPLParameterElement) undetvar.elts.head;
		// element.includedIn holds the inferred binding
		RPL includedIn = element.includedIn;
		if (includedIn == null) {
		    // No binding occurred, so use the most conservative RPL
		    includedIn = new RPL(List.<RPLElement>of(RPLElement.ROOT_ELEMENT, 
			    RPLElement.STAR));
		}
		lb.append(includedIn);
	    } else {
		lb.append(undetvar);
	    }
	}
	return lb.toList();
    }
}