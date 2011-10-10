package com.sun.tools.javac.code;

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

/** A class for representing constraints
 */
public class Constraints {
    public Constraints(List<Pair<RPL,RPL>> disjointRPLs) {
	this.disjointRPLs = disjointRPLs;
    }
    public Constraints() {}
    public List<Pair<RPL,RPL>> disjointRPLs = List.nil();
    // We can add others here as necessary, like inclusion or subeffects
}