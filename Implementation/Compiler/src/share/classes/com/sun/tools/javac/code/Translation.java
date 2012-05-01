package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * Interfaces for translation via type, region, and ref group 
 * substitution.
 * 
 * @author Rob Bocchino
 */
public class Translation {

    /** Interface for representing 'as member of' translation */
    public interface AsMemberOf<T extends AsMemberOf<T>> {
	
	/** 'this' as a member of type t */
	public T asMemberOf(Types types, Type t);
	
    }

    /** Apply 'as member of' to a list of things */
    public static <T extends AsMemberOf<T>> List<T>asMemberOf(List<T> list,
	    Types types, Type t) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.asMemberOf(types, t));
	return lb.toList();
    }

    /** Interface for translating things at method call sites */
    public interface AtCallSite<T extends AtCallSite<T>> {
	
	/** 'this' at the call site */
	public T atCallSite(Resolve rs, Env<AttrContext> env,
		JCMethodInvocation site);
	
    }

    /** Apply 'at call site' to a list of things */
    public static <T extends AtCallSite<T>> List<T>atCallSite(List<T> list,
	    Resolve rs, Env<AttrContext> env, JCMethodInvocation site) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.atCallSite(rs, env, site));
	return lb.toList();
    }
        
    /** Perform 'as member of' implied by a selection */
    public static <T extends AsMemberOf<T>> T accessElt(T elt, Types types, 
	    JCExpression tree) {
	if (tree instanceof JCFieldAccess) {
	    JCFieldAccess fa = (JCFieldAccess) tree;
	    return elt.asMemberOf(types, fa.selected.type);
	}
	return elt;
    }
    
    /** Perform 'accessElt' on a list of things */
    public static <T extends AsMemberOf<T>> List<T>accessElts(List<T> elts,
	    Types types, JCExpression tree) {
	if (tree instanceof JCFieldAccess) {
	    ListBuffer<T> lb = ListBuffer.lb();
	    for (T elt : elts) lb.append(accessElt(elt, types, tree));
	    return lb.toList();
	}
	return elts;
    }
    
    /** Interface for representing type substitutions */
    public interface SubstTypes<T extends SubstTypes<T>> {
	
	/** 'this' after substituting 'to' types for 'from' types */
	public T substTypes(List<Type> from, List<Type> to);
    }

    /** Apply type substitution to a list of things */
    public static <T extends SubstTypes<T>> List<T>substTypes(List<T> list,
	    List<Type> from, List<Type> to) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.substTypes(from, to));
	return lb.toList();
    }

    /** Interface for representing RPL substitutions */
    public interface SubstRPLs<T extends SubstRPLs<T>> {
	
	/** 'this' after substituting 'to' RPLs for 'from' RPLs */
	public T substRPLs(List<RPL> from, List<RPL> to);
	
    }

    /** Apply RPL substitution to a list of things */
    public static <T extends SubstRPLs<T>> List<T>substRPLs(List<T> list,
	    List<RPL> from, List<RPL> to) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.substRPLs(from, to));
	return lb.toList();
    }

    /** Interface for representing ref group substitutions */
    public interface SubstRefGroups<T extends SubstRefGroups<T>> {

	/** 'this' after substituting 'to' groups for 'from' groups */
	public T substRefGroups(List<RefGroup> from, List<RefGroup> to);
	
    }

    /** Apply ref group substitution to a list of things */
    public static <T extends SubstRefGroups<T>> List<T>substRefGroups(List<T> list,
	    List<RefGroup> from, List<RefGroup> to) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.substRefGroups(from, to));
	return lb.toList();
    }
    
    /** Interface for representing variable substitutions */
    public interface SubstVars<T extends SubstVars<T>> {
	
	/** 'this' after substituting 'to' expressions for 'from' vars */
	public T substVars(Permissions permissions, 
		List<VarSymbol> from, List<JCExpression> to);
	
    }
    
    /** Apply var-expr substitution to a list of things */
    public static <T extends SubstVars<T>> List<T>substVars(List<T> list,
	    Permissions permissions, List<VarSymbol> from,
	    List<JCExpression> to) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(elt.substVars(permissions, from, to));
	return lb.toList();
    }
    
    /** 'this' after substituting 'to' vars for 'from' vars */
    public static <T extends SubstVars<T>> T substVarSymbols(T elt,
	    Permissions permissions, List<VarSymbol> from, 
	    List<VarSymbol> to) {
	ListBuffer<JCExpression> lb = ListBuffer.lb();
	for (VarSymbol var : to) {
	    lb.append(permissions.maker.Ident(var));
	}
	return elt.substVars(permissions, from, lb.toList());
    }

    public static <T extends SubstVars<T>> List<T>substVarSymbols(List<T> list,
	    Permissions permissions, List<VarSymbol> from,
	    List<VarSymbol> to) {
	ListBuffer<T> lb = ListBuffer.lb();
	for (T elt : list) lb.append(substVarSymbols(elt, permissions, from, to));
	return lb.toList();
    }
    
    /**
     * Substitute for 'this' at method invocation     
     */
    public static <T extends SubstVars<T>> T substForThis(T elt, 
	    JCExpression selectExp, Resolve rs, Env<AttrContext> env,
	    Symbol thisSym) {
	// Compute the implied argument to 'this'
        JCExpression thisArg = null;
        if (selectExp instanceof JCFieldAccess) {
            thisArg = ((JCFieldAccess) selectExp).selected;
        }
        else {
            thisArg = rs.getTreeMaker().Ident(rs.findThis(env));
        }
        // Substitute it
	if (thisSym instanceof VarSymbol)
	    return elt.substVars(rs.getPermissions(),
		    List.of((VarSymbol) thisSym), List.of(thisArg));
	return elt;
    }
}
    
