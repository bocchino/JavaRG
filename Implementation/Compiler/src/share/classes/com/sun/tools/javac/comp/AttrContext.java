/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.comp;

import java.util.HashSet;
import java.util.Set;

import com.sun.tools.javac.code.Constraints;
import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.code.Permission.EnvPerm;
import com.sun.tools.javac.code.Permission.RefPerm;
import com.sun.tools.javac.code.RPL;
import com.sun.tools.javac.code.RefGroup;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

/** Contains information specific to the attribute and enter
 *  passes, to be used in place of the generic field in environments.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class AttrContext {

    /** The scope of local symbols.
     */
    public Scope scope = null;

    /** The number of enclosing `static' modifiers.
     */
    int staticLevel = 0;

    /** Is this an environment for a this(...) or super(...) call?
     */
    boolean isSelfCall = false;

    /** Are we evaluating the selector of a `super' or type name?
     */
    boolean selectSuper = false;

    /** Are arguments to current function applications boxed into an array for varargs?
     */
    boolean varArgs = false;

    /** A list of type variables that are all-quantifed in current context.
     */
    List<Type> tvars = List.nil();
    
    /** A list of RPL variables that are all-quantifed in current context.
     */
    List<RPL> rvars = List.nil();
    
    /** Info about the method call site, used in method resolution
     */
    VarSymbol siteVar; // TODO: Is this necessary?
    JCExpression siteExp;
    List<JCExpression> actualArgs;
    
    
    /** RPL constraints active in the environment
     */
    List<Pair<RPL,RPL>> constraintsOld = List.nil();
    public Constraints constraints = new Constraints();
    
    /** All the index variables of the JRG for loops we are currently inside
     */
    public List<VarSymbol> forIndexVars = List.nil();
    
    /** A record of the lint/SuppressWarnings currently in effect
     */
    Lint lint;
    
    /** Duplicate this context, replacing scope field and copying all others.
     */
    AttrContext dup(Scope scope) {
	AttrContext info = new AttrContext();
	info.scope = scope;
	info.staticLevel = staticLevel;
	info.isSelfCall = isSelfCall;
	info.selectSuper = selectSuper;
	info.varArgs = varArgs;
	info.tvars = tvars;
	info.rvars = rvars;
	info.constraints = constraints;
	info.forIndexVars = forIndexVars;
	info.lint = lint;
	return info;
    }
    
    /** Variables that have been borrowed and will be restored
     *  at the end of the current environment.
     */
    public final HashSet<VarSymbol> borrowedVars =
	    new HashSet<VarSymbol>();
    
    public RefPerm getRefPermFor(VarSymbol varSym) {
	if (borrowedVars.contains(varSym))
	    return RefPerm.NONE;
	RefPerm refPerm = scope.downgradedPerms.get(varSym);
	if (refPerm != null)
	    return refPerm;
	return varSym.refPerm;
    }
    
    
    /** Duplicate this context, copying all fields.
     */
    AttrContext dup() {
	return dup(scope);
    }
    
    public Iterable<Symbol> getLocalElements() {
        if (scope == null)
            return List.nil();
        return scope.getElements();
    }
    
    public String toString() {
        return "AttrContext[" + scope.toString() + "]";
    }
    
}

