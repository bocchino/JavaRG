package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Permission.EnvPerm.FreshGroupPerm;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * Interfaces for mapping via type, region, and ref group substitution.
 * 
 * @author Rob Bocchino
 *
 * @param <T>
 */
public class Mappings {

    /** Interface for representing 'as member of' transformations */
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
    
}
    
