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

package com.sun.tools.javac.tree;

import static com.sun.tools.javac.code.Flags.FINAL;
import static com.sun.tools.javac.code.Flags.PUBLIC;
import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.code.Kinds.MTH;
import static com.sun.tools.javac.code.Kinds.TYP;
import static com.sun.tools.javac.code.Kinds.VAR;
import static com.sun.tools.javac.code.TypeTags.ARRAY;
import static com.sun.tools.javac.code.TypeTags.BOOLEAN;
import static com.sun.tools.javac.code.TypeTags.BYTE;
import static com.sun.tools.javac.code.TypeTags.CHAR;
import static com.sun.tools.javac.code.TypeTags.CLASS;
import static com.sun.tools.javac.code.TypeTags.DOUBLE;
import static com.sun.tools.javac.code.TypeTags.ERROR;
import static com.sun.tools.javac.code.TypeTags.FLOAT;
import static com.sun.tools.javac.code.TypeTags.INT;
import static com.sun.tools.javac.code.TypeTags.LONG;
import static com.sun.tools.javac.code.TypeTags.SHORT;
import static com.sun.tools.javac.code.TypeTags.TYPEVAR;
import static com.sun.tools.javac.code.TypeTags.VOID;
import static com.sun.tools.javac.code.TypeTags.WILDCARD;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Permission.EnvPerm.CopyPerm;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree.DPJRegionDecl;
import com.sun.tools.javac.tree.JCTree.DPJRegionParameter;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathListElt;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.JRGCopyPerm;
import com.sun.tools.javac.tree.JCTree.JRGDerefSet;
import com.sun.tools.javac.tree.JCTree.JRGEffectPerm;
import com.sun.tools.javac.tree.JCTree.JRGForLoop;
import com.sun.tools.javac.tree.JCTree.JRGMethodPerms;
import com.sun.tools.javac.tree.JCTree.JRGParamInfo;
import com.sun.tools.javac.tree.JCTree.JRGPardo;
import com.sun.tools.javac.tree.JCTree.JRGRefGroupDecl;
import com.sun.tools.javac.tree.JCTree.JRGRefPerm;
import com.sun.tools.javac.tree.JCTree.JRGRefPerm.PermKind;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;
import com.sun.tools.javac.util.Position;

/** Factory class for trees.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class TreeMaker implements JCTree.Factory {

    /** The context key for the tree factory. */
    protected static final Context.Key<TreeMaker> treeMakerKey =
        new Context.Key<TreeMaker>();

    /** Get the TreeMaker instance. */
    public static TreeMaker instance(Context context) {
        TreeMaker instance = context.get(treeMakerKey);
        if (instance == null)
            instance = new TreeMaker(context);
        return instance;
    }

    /** The position at which subsequent trees will be created.
     */
    public int pos = Position.NOPOS;

    /** The toplevel tree to which created trees belong.
     */
    public JCCompilationUnit toplevel;

    /** The current name table. */
    Name.Table names;

    Types types;

    /** The current symbol table. */
    Symtab syms;

    /** Create a tree maker with null toplevel and NOPOS as initial position.
     */
    protected TreeMaker(Context context) {
        context.put(treeMakerKey, this);
        this.pos = Position.NOPOS;
        this.toplevel = null;
        this.names = Name.Table.instance(context);
        this.syms = Symtab.instance(context);
        this.types = Types.instance(context);
    }

    /** Create a tree maker with a given toplevel and FIRSTPOS as initial position.
     */
    TreeMaker(JCCompilationUnit toplevel, Name.Table names, Types types, Symtab syms) {
        this.pos = Position.FIRSTPOS;
        this.toplevel = toplevel;
        this.names = names;
        this.types = types;
        this.syms = syms;
    }

    /** Create a new tree maker for a given toplevel.
     */
    public TreeMaker forToplevel(JCCompilationUnit toplevel) {
        return new TreeMaker(toplevel, names, types, syms);
    }

    /** Reassign current position.
     */
    public TreeMaker at(int pos) {
        this.pos = pos;
        return this;
    }

    /** Reassign current position.
     */
    public TreeMaker at(DiagnosticPosition pos) {
        this.pos = (pos == null ? Position.NOPOS : pos.getStartPosition());
        return this;
    }

    /**
     * Create given tree node at current position.
     * @param defs a list of ClassDef, Import, and Skip
     */
    public JCCompilationUnit TopLevel(List<JCAnnotation> packageAnnotations,
				      JCExpression pid,
				      List<JCTree> defs) {
        assert packageAnnotations != null;
        for (JCTree node : defs)
            assert node instanceof JCClassDecl
		|| node instanceof JCImport
		|| node instanceof JCSkip
                || node instanceof JCErroneous
		|| (node instanceof JCExpressionStatement
		    && ((JCExpressionStatement)node).expr instanceof JCErroneous)
                 : node.getClass().getSimpleName();
        JCCompilationUnit tree = new JCCompilationUnit(packageAnnotations, pid, defs,
                                     null, null, null, null);
        tree.pos = pos;
        return tree;
    }

    public JCImport Import(JCTree qualid, boolean importStatic) {
        JCImport tree = new JCImport(qualid, importStatic);
        tree.pos = pos;
        return tree;
    }

    public JCClassDecl ClassDef(JCModifiers mods,
				Name name,
				JRGParamInfo rgnparamInfo,
				List<JCTypeParameter> typarams,
				JCTree extending,
				List<JCExpression> implementing,
				List<JCTree> defs)
    {
        JCClassDecl tree = new JCClassDecl(mods,
				     name,
				     rgnparamInfo,
				     typarams,
				     extending,
				     implementing,
				     defs,
				     null);
        tree.pos = pos;
        return tree;
    }

    public JCMethodDecl MethodDef(JCModifiers mods,
                               Name name,
                               JRGRefPerm resperm,
                               JCExpression restype,
                               JRGParamInfo rgnParamInfo,
                               List<JCTypeParameter> typarams,
                               List<JCVariableDecl> params,
                               JRGMethodPerms perms,
                               List<JCExpression> thrown,
                               JCBlock body,
                               JCExpression defaultValue)
    {
        JCMethodDecl tree = new JCMethodDecl(mods,
                                       name,
                                       resperm,
                                       restype,
                                       rgnParamInfo,
                                       typarams,
                                       params,
                                       perms,
                                       thrown,
                                       body,
                                       defaultValue,
                                       null);
        tree.pos = pos;
        return tree;
    }

    public JCVariableDecl VarDef(JCModifiers mods, Name name, 
	    JRGRefPerm refPerm, JCExpression vartype, 
	    DPJRegionPathList rpl, JCExpression init) {
	if (refPerm == null) refPerm = RefPerm();
        JCVariableDecl tree = new JCVariableDecl(mods, name, 
        	refPerm, vartype, rpl, init, null);
        tree.pos = pos;
        return tree;
    }

    public JCSkip Skip() {
        JCSkip tree = new JCSkip();
        tree.pos = pos;
        return tree;
    }

    public JCBlock Block(long flags, List<JCStatement> stats) {
        JCBlock tree = new JCBlock(flags, stats);
        tree.pos = pos;
        return tree;
    }

    public JCDoWhileLoop DoLoop(JCStatement body, JCExpression cond) {
        JCDoWhileLoop tree = new JCDoWhileLoop(body, cond);
        tree.pos = pos;
        return tree;
    }

    public JCWhileLoop WhileLoop(JCExpression cond, JCStatement body) {
        JCWhileLoop tree = new JCWhileLoop(cond, body);
        tree.pos = pos;
        return tree;
    }

    public JCForLoop ForLoop(List<JCStatement> init,
                           JCExpression cond,
                           List<JCExpressionStatement> step,
                           JCStatement body)
    {
        JCForLoop tree = new JCForLoop(init, cond, step, body);
        tree.pos = pos;
        return tree;
    }

    public JCEnhancedForLoop ForeachLoop(JCVariableDecl var, JCExpression expr, JCStatement body) {
        JCEnhancedForLoop tree = new JCEnhancedForLoop(var, expr, body);
        tree.pos = pos;
        return tree;
    }

    public JCLabeledStatement Labelled(Name label, JCStatement body) {
        JCLabeledStatement tree = new JCLabeledStatement(label, body);
        tree.pos = pos;
        return tree;
    }

    public JCSwitch Switch(JCExpression selector, List<JCCase> cases) {
        JCSwitch tree = new JCSwitch(selector, cases, false);
        tree.pos = pos;
        return tree;
    }

    public JCSwitch InstanceOfSwitch(JCExpression selector, List<JCCase> cases) {
        JCSwitch tree = new JCSwitch(selector, cases, true);
        tree.pos = pos;
        return tree;
    }
    
    public JCCase Case(JCExpression pat, List<JCStatement> stats) {
        JCCase tree = new JCCase(pat, stats);
        tree.pos = pos;
        return tree;
    }

    public JCSynchronized Synchronized(JCExpression lock, JCBlock body) {
        JCSynchronized tree = new JCSynchronized(lock, body);
        tree.pos = pos;
        return tree;
    }

    public JCTry Try(JCBlock body, List<JCCatch> catchers, JCBlock finalizer) {
        JCTry tree = new JCTry(body, catchers, finalizer);
        tree.pos = pos;
        return tree;
    }

    public JCCatch Catch(JCVariableDecl param, JCBlock body) {
        JCCatch tree = new JCCatch(param, body);
        tree.pos = pos;
        return tree;
    }

    public JCConditional Conditional(JCExpression cond,
                                   JCExpression thenpart,
                                   JCExpression elsepart)
    {
        JCConditional tree = new JCConditional(cond, thenpart, elsepart);
        tree.pos = pos;
        return tree;
    }

    public JCIf If(JCExpression cond, JCStatement thenpart, JCStatement elsepart) {
        JCIf tree = new JCIf(cond, thenpart, elsepart);
        tree.pos = pos;
        return tree;
    }

    public JCExpressionStatement Exec(JCExpression expr) {
        JCExpressionStatement tree = new JCExpressionStatement(expr);
        tree.pos = pos;
        return tree;
    }

    public JCBreak Break(Name label) {
        JCBreak tree = new JCBreak(label, null);
        tree.pos = pos;
        return tree;
    }

    public JCContinue Continue(Name label) {
        JCContinue tree = new JCContinue(label, null);
        tree.pos = pos;
        return tree;
    }

    public JCReturn Return(JCExpression expr) {
        JCReturn tree = new JCReturn(expr);
        tree.pos = pos;
        return tree;
    }

    public JCThrow Throw(JCTree expr) {
        JCThrow tree = new JCThrow(expr);
        tree.pos = pos;
        return tree;
    }

    public JCAssert Assert(JCExpression cond, JCExpression detail) {
        JCAssert tree = new JCAssert(cond, detail);
        tree.pos = pos;
        return tree;
    }

    public JCMethodInvocation Apply(List<DPJRegionPathList> regionArgs,
	               List<JCExpression> typeargs,
	               List<JCIdent> groupArgs,
		       JCExpression fn,
		       List<JCExpression> args)
    {
        JCMethodInvocation tree = 
            new JCMethodInvocation(regionArgs, typeargs, groupArgs, fn, args);
        tree.pos = pos;
        return tree;
    }

    public JCNewClass NewClass(JCExpression encl,
	    	             List<DPJRegionPathList> regionArgs,
                             List<JCExpression> typeargs,
                             List<JCIdent> groupArgs,
                             JCExpression clazz,
                             List<JCExpression> args,
                             JCClassDecl def)
    {
        JCNewClass tree = new JCNewClass(encl, regionArgs, typeargs, 
        	                         groupArgs, clazz, args, def);
        tree.pos = pos;
        return tree;
    }

    public JCNewArray NewArray(JCExpression elemtype,
			     List<JCExpression> dims,
			     List<JCExpression> elems)
    {
        JCNewArray tree = new JCNewArray(elemtype, dims, elems);
        tree.pos = pos;
        return tree;
    }

    public JCParens Parens(JCExpression expr) {
        JCParens tree = new JCParens(expr);
        tree.pos = pos;
        return tree;
    }

    public JCAssign Assign(JCExpression lhs, JCExpression rhs) {
        JCAssign tree = new JCAssign(lhs, rhs);
        tree.pos = pos;
        return tree;
    }

    public JCAssignOp Assignop(int opcode, JCTree lhs, JCTree rhs) {
        JCAssignOp tree = new JCAssignOp(opcode, lhs, rhs, null);
        tree.pos = pos;
        return tree;
    }

    public JCUnary Unary(int opcode, JCExpression arg) {
        JCUnary tree = new JCUnary(opcode, arg);
        tree.pos = pos;
        return tree;
    }

    public JCBinary Binary(int opcode, JCExpression lhs, JCExpression rhs) {
        JCBinary tree = new JCBinary(opcode, lhs, rhs, null);
        tree.pos = pos;
        return tree;
    }

    public JCTypeCast TypeCast(JCTree clazz, JCExpression expr) {
        JCTypeCast tree = new JCTypeCast(clazz, expr);
        tree.pos = pos;
        return tree;
    }

    public JCInstanceOf TypeTest(JCExpression expr, JCTree clazz) {
        JCInstanceOf tree = new JCInstanceOf(expr, clazz);
        tree.pos = pos;
        return tree;
    }

    public JCArrayAccess Indexed(JCExpression indexed, JCExpression index) {
        JCArrayAccess tree = new JCArrayAccess(indexed, index);
        tree.pos = pos;
        return tree;
    }

    public JCFieldAccess Select(JCExpression selected, Name selector) {
        JCFieldAccess tree = new JCFieldAccess(selected, selector, null);
        tree.pos = pos;
        return tree;
    }

    public JCIdent Ident(Name name) {
        JCIdent tree = new JCIdent(name, null);
        tree.pos = pos;
        return tree;
    }

    public JCLiteral Literal(int tag, Object value) {
        JCLiteral tree = new JCLiteral(tag, value);
        tree.pos = pos;
        return tree;
    }

    public JCPrimitiveTypeTree TypeIdent(int typetag) {
        JCPrimitiveTypeTree tree = new JCPrimitiveTypeTree(typetag);
        tree.pos = pos;
        return tree;
    }

    public JCArrayTypeTree TypeArray(JCExpression elemtype) {
        JCArrayTypeTree tree = new JCArrayTypeTree(elemtype);
        tree.pos = pos;
        return tree;
    }

    public JCTypeApply TypeApply(JCExpression clazz, List<JCExpression> arguments,
	    List<DPJRegionPathList> rplArgs, List<JCIdent> groupArgs) {
        JCTypeApply tree = new JCTypeApply(clazz, arguments, rplArgs, groupArgs);
        tree.pos = pos;
        return tree;
    }
    
    public JCTypeParameter TypeParameter(Name name, List<DPJRegionParameter> rplparams,
	    List<JCExpression> bounds) {
        JCTypeParameter tree = new JCTypeParameter(name, rplparams, bounds);
        tree.pos = pos;
        return tree;
    }
    
    public DPJRegionParameter RegionParameter(Name name, DPJRegionPathList bound,
	    boolean isAtomic) {
	DPJRegionParameter tree = new DPJRegionParameter(name, bound, isAtomic);
	tree.pos = pos;
	return tree;
    }

    public JCWildcard Wildcard(TypeBoundKind kind, JCTree type) {
        JCWildcard tree = new JCWildcard(kind, type);
        tree.pos = pos;
        return tree;
    }

    public TypeBoundKind TypeBoundKind(BoundKind kind) {
        TypeBoundKind tree = new TypeBoundKind(kind);
        tree.pos = pos;
        return tree;
    }

    public JCAnnotation Annotation(JCTree annotationType, List<JCExpression> args) {
        JCAnnotation tree = new JCAnnotation(annotationType, args);
        tree.pos = pos;
        return tree;
    }

    public JCModifiers Modifiers(long flags, List<JCAnnotation> annotations) {
        JCModifiers tree = new JCModifiers(flags, annotations);
        boolean noFlags = (flags & Flags.StandardFlags) == 0;
        tree.pos = (noFlags && annotations.isEmpty()) ? Position.NOPOS : pos;
        return tree;
    }

    public JCModifiers Modifiers(long flags) {
        return Modifiers(flags, List.<JCAnnotation>nil());
    }

    public JCErroneous Erroneous() {
        return Erroneous(List.<JCTree>nil());
    }

    public JCErroneous Erroneous(List<? extends JCTree> errs) {
        JCErroneous tree = new JCErroneous(errs);
        tree.pos = pos;
        return tree;
    }

    public LetExpr LetExpr(List<JCVariableDecl> defs, JCTree expr) {
        LetExpr tree = new LetExpr(defs, expr);
        tree.pos = pos;
        return tree;
    }

    public DPJRegionDecl RegionDecl(JCModifiers mods, Name name) { // DPJ
        DPJRegionDecl tree = new DPJRegionDecl(mods, name, null);
        tree.pos = pos;
        return tree;
    }
    
    public JRGRefGroupDecl RefGroupDecl(Name name) {
	JRGRefGroupDecl tree = new JRGRefGroupDecl(name);
	tree.pos = pos;
	return tree;
    }
    
    public DPJRegionPathListElt RegionPathListElt(JCExpression exp, int t) {
	DPJRegionPathListElt tree = new DPJRegionPathListElt(exp, t);
	tree.pos = pos;
	return tree;
    }

    public DPJRegionPathList RegionPathList(List<DPJRegionPathListElt> elts) { // DPJ
        DPJRegionPathList tree = new DPJRegionPathList(elts);
        tree.pos = pos;
        return tree;
    }
    
    public JRGRefPerm RefPerm(PermKind permKind, JCIdent group) {
	JRGRefPerm tree = new JRGRefPerm(permKind, group);
	tree.pos = pos;
	return tree;
    }
    
    public JRGRefPerm RefPerm() {
	JRGRefPerm tree = new JRGRefPerm(PermKind.SHARED, null);
	tree.pos = pos;
	return tree;
    }
    
    public JRGMethodPerms MethodPerms(JRGRefPerm refPerm, List<JCIdent> freshGroups,
	    List<JRGCopyPerm> copyPerms, boolean defaultEffectPerms,
	    List<JRGEffectPerm> readEffectPerms, List<JRGEffectPerm> writeEffectPerms,
	    List<JCIdent> preservedGroups, List<JCIdent> switchedGroups) {
	JRGMethodPerms tree = new JRGMethodPerms(refPerm, freshGroups, copyPerms, 
		defaultEffectPerms, readEffectPerms, writeEffectPerms,
		preservedGroups, switchedGroups);
	tree.pos = pos;
	return tree;
    }
    
    public JRGDerefSet DerefSet(JCExpression root, JCIdent group) {
	JRGDerefSet tree = new JRGDerefSet(root, group);
	tree.pos = pos;
	return tree;
    }

    public JRGCopyPerm CopyPerm(JRGDerefSet derefSet, JCIdent group) {
	JRGCopyPerm tree = new JRGCopyPerm(derefSet, group, null);
	tree.pos = pos;
	return tree;
    }

    public JRGCopyPerm CopyPerm(JRGDerefSet derefSet, JCIdent group,
	    CopyPerm copyPerm) {
	JRGCopyPerm tree = new JRGCopyPerm(derefSet, group, copyPerm);
	tree.pos = pos;
	return tree;
    }

    public JRGEffectPerm EffectPerm(DPJRegionPathList rpl, JRGDerefSet derefSet) {
	JRGEffectPerm tree = new JRGEffectPerm(rpl, derefSet);
	tree.pos = pos;
	return tree;
    }
    
    public JRGParamInfo ParamInfo(List<DPJRegionParameter> params,
	    			  List<Pair<DPJRegionPathList,DPJRegionPathList>> rplConstraints,
	    			  List<JCIdent> groupParams) {
	JRGParamInfo tree = new JRGParamInfo(params, rplConstraints, groupParams);
	tree.pos = pos;
	return tree;
    }
    
    public JRGPardo Pardo(JCBlock body) {
	JRGPardo tree = new JRGPardo(body);
	tree.pos = pos;
	return tree;
    }

    public JRGForLoop JRGForLoop(JCVariableDecl indexVar, JCExpression array, 
	    JCStatement body, boolean isParallel) {
	JRGForLoop tree = new JRGForLoop(indexVar, array, body, isParallel);
	tree.pos = pos;
	return tree;
    }
    
/* ***************************************************************************
 * Derived building blocks.
 ****************************************************************************/

    public JCClassDecl AnonymousClassDef(JCModifiers mods,
					 List<JCTree> defs)
    {
	return ClassDef(mods,
			names.empty,
			null,
			List.<JCTypeParameter>nil(),
			null,
			List.<JCExpression>nil(),
			defs);
    }

    public LetExpr LetExpr(JCVariableDecl def, JCTree expr) {
        LetExpr tree = new LetExpr(List.of(def), expr);
        tree.pos = pos;
        return tree;
    }

    /** Create an identifier from a symbol.
     */
    public JCIdent Ident(Symbol sym) {
        return (JCIdent)new JCIdent((sym.name != names.empty)
				? sym.name
				: sym.flatName(), sym)
            .setPos(pos)
            .setType(sym.type);
    }

    /** Create a selection node from a qualifier tree and a symbol.
     *  @param base   The qualifier tree.
     */
    public JCExpression Select(JCExpression base, Symbol sym) {
        return new JCFieldAccess(base, sym.name, sym).setPos(pos).setType(sym.type);
    }

    /** Create a qualified identifier from a symbol, adding enough qualifications
     *  to make the reference unique.
     */
    public JCExpression QualIdent(Symbol sym) {
        return isUnqualifiable(sym)
            ? Ident(sym)
            : Select(QualIdent(sym.owner), sym);
    }

    /** Create an identifier that refers to the variable declared in given variable
     *  declaration.
     */
    public JCExpression Ident(JCVariableDecl param) {
        return Ident(param.sym);
    }

    /** Create a list of identifiers referring to the variables declared
     *  in given list of variable declarations.
     */
    public List<JCExpression> Idents(List<JCVariableDecl> params) {
        ListBuffer<JCExpression> ids = new ListBuffer<JCExpression>();
        for (List<JCVariableDecl> l = params; l.nonEmpty(); l = l.tail)
            ids.append(Ident(l.head));
        return ids.toList();
    }

    /** Create a tree representing `this', given its type.
     */
    public JCExpression This(Type t) {
        return Ident(new VarSymbol(FINAL, names._this, t, t.tsym));
    }

    /** Create a tree representing a class literal.
     */
    public JCExpression ClassLiteral(ClassSymbol clazz) {
        return ClassLiteral(clazz.type);
    }

    /** Create a tree representing a class literal.
     */
    public JCExpression ClassLiteral(Type t) {
        VarSymbol lit = new VarSymbol(STATIC | PUBLIC | FINAL,
                                      names._class,
                                      t,
                                      t.tsym);
        return Select(Type(t), lit);
    }

    /** Create a tree representing `super', given its type and owner.
     */
    public JCIdent Super(Type t, TypeSymbol owner) {
        return Ident(new VarSymbol(FINAL, names._super, t, owner));
    }

    /**
     * Create a method invocation from a method tree and a list of
     * argument trees.
     */
    public JCMethodInvocation App(JCExpression meth, List<JCExpression> args) {
        return Apply(null, null, null, meth, args).setType(meth.type.getReturnType());
    }

    /**
     * Create a no-arg method invocation from a method tree
     */
    public JCMethodInvocation App(JCExpression meth) {
        return Apply(null, null, null, meth, 
        	List.<JCExpression>nil()).setType(meth.type.getReturnType());
    }

    /** Create a method invocation from a method tree and a list of argument trees.
     */
    public JCExpression Create(Symbol ctor, List<JCExpression> args) {
        Type t = ctor.owner.erasure(types);
        JCNewClass newclass = NewClass(null, null, null, null, 
        	Type(t), args, null);
        newclass.constructor = ctor;
        newclass.setType(t);
        return newclass;
    }

    /** Create a tree representing given type.
     */
    public JCExpression Type(Type t) {
        if (t == null) return null;
        JCExpression tp;
        switch (t.tag) {
        case BYTE: case CHAR: case SHORT: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN: case VOID:
            tp = TypeIdent(t.tag);
            break;
        case TYPEVAR:
            tp = Ident(t.tsym);
            break;
        case WILDCARD: {
            WildcardType a = ((WildcardType) t);
            tp = Wildcard(TypeBoundKind(a.kind), Type(a.type));
            break;
        }
        case CLASS:
            Type outer = t.getEnclosingType();
            JCExpression clazz = outer.tag == CLASS && t.tsym.owner.kind == TYP
                ? Select(Type(outer), t.tsym)
                : QualIdent(t.tsym);
            tp = t.getTypeArguments().isEmpty()
                ? clazz
                : TypeApply(clazz, Types(t.getTypeArguments()), 
                	List.<DPJRegionPathList>nil(), List.<JCIdent>nil());
            break;
        case ARRAY:
            tp = TypeArray(Type(types.elemtype(t)));
            break;
        case ERROR:
            tp = TypeIdent(ERROR);
            break;
        default:
            throw new AssertionError("unexpected type: " + t);
        }
        return tp.setType(t);
    }
//where
        private JCExpression Selectors(JCExpression base, Symbol sym, Symbol limit) {
            if (sym == limit) return base;
            else return Select(Selectors(base, sym.owner, limit), sym);
        }

    /** Create a list of trees representing given list of types.
     */
    public List<JCExpression> Types(List<Type> ts) {
        ListBuffer<JCExpression> types = new ListBuffer<JCExpression>();
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            types.append(Type(l.head));
        return types.toList();
    }
    
    /** Create a variable definition from a variable symbol and an initializer
     *  expression.
     */
    public JCVariableDecl VarDef(VarSymbol v, JCExpression init) {
	return (JCVariableDecl)
            new JCVariableDecl(
                Modifiers(v.flags(), Annotations(v.getAnnotationMirrors())),
                v.name,
                RefPerm(), // FIXME
                Type(v.type),
                null, // FIXME
                init,
                v).setPos(pos).setType(v.type);
    }

    /** Create annotation trees from annotations.
     */
    public List<JCAnnotation> Annotations(List<Attribute.Compound> attributes) {
        if (attributes == null) return List.nil();
        ListBuffer<JCAnnotation> result = new ListBuffer<JCAnnotation>();
        for (List<Attribute.Compound> i = attributes; i.nonEmpty(); i=i.tail) {
            Attribute a = i.head;
            result.append(Annotation(a));
        }
        return result.toList();
    }

    public JCLiteral Literal(Object value) {
        JCLiteral result = null;
        if (value instanceof String) {
            result = Literal(CLASS, value).
                setType(syms.stringType.constType(value));
        } else if (value instanceof Integer) {
            result = Literal(INT, value).
                setType(syms.intType.constType(value));
        } else if (value instanceof Long) {
            result = Literal(LONG, value).
                setType(syms.longType.constType(value));
        } else if (value instanceof Byte) {
            result = Literal(BYTE, value).
                setType(syms.byteType.constType(value));
        } else if (value instanceof Character) {
            result = Literal(CHAR, value).
                setType(syms.charType.constType(value));
        } else if (value instanceof Double) {
            result = Literal(DOUBLE, value).
                setType(syms.doubleType.constType(value));
        } else if (value instanceof Float) {
            result = Literal(FLOAT, value).
                setType(syms.floatType.constType(value));
        } else if (value instanceof Short) {
            result = Literal(SHORT, value).
                setType(syms.shortType.constType(value));
        } else {
            throw new AssertionError(value);
        }
        return result;
    }

    class AnnotationBuilder implements Attribute.Visitor {
        JCExpression result = null;
        public void visitConstant(Attribute.Constant v) {
            result = Literal(v.value);
        }
        public void visitClass(Attribute.Class clazz) {
            result = ClassLiteral(clazz.type).setType(syms.classType);
        }
        public void visitEnum(Attribute.Enum e) {
            result = QualIdent(e.value);
        }
        public void visitError(Attribute.Error e) {
            result = Erroneous();
        }
        public void visitCompound(Attribute.Compound compound) {
            result = visitCompoundInternal(compound);
        }
        public JCAnnotation visitCompoundInternal(Attribute.Compound compound) {
            ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
            for (List<Pair<Symbol.MethodSymbol,Attribute>> values = compound.values; values.nonEmpty(); values=values.tail) {
                Pair<MethodSymbol,Attribute> pair = values.head;
                JCExpression valueTree = translate(pair.snd);
                args.append(Assign(Ident(pair.fst), valueTree).setType(valueTree.type));
            }
            return Annotation(Type(compound.type), args.toList());
        }
        public void visitArray(Attribute.Array array) {
            ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
            for (int i = 0; i < array.values.length; i++)
                elems.append(translate(array.values[i]));
            result = NewArray(null, List.<JCExpression>nil(), 
        	              elems.toList()).setType(array.type);
        }
        JCExpression translate(Attribute a) {
            a.accept(this);
            return result;
        }
        JCAnnotation translate(Attribute.Compound a) {
            return visitCompoundInternal(a);
        }
    }
    AnnotationBuilder annotationBuilder = new AnnotationBuilder();

    /** Create an annotation tree from an attribute.
     */
    public JCAnnotation Annotation(Attribute a) {
        return annotationBuilder.translate((Attribute.Compound)a);
    }

    /** Create a method definition from a method symbol and a method body.
     */
    public JCMethodDecl MethodDef(MethodSymbol m, JCBlock body) {
        return MethodDef(m, m.type, body);
    }

    /** Create a method definition from a method symbol, method type
     *  and a method body.
     */
    public JCMethodDecl MethodDef(MethodSymbol m, Type mtype, JCBlock body) {
        return (JCMethodDecl)
            new JCMethodDecl(
                Modifiers(m.flags(), Annotations(m.getAnnotationMirrors())),
                m.name,
                RefPerm(),
                Type(mtype.getReturnType()),
                null, // DPJ FIXME
                TypeParams(mtype.getTypeArguments()),
                Params(mtype.getParameterTypes(), m),
                null, // DPJ FIXME
                Types(mtype.getThrownTypes()),
                body,
                null, 
                m).setPos(pos).setType(mtype);
    }

    /** Create a type parameter tree from its name and type.
     */
    public JCTypeParameter TypeParam(Name name, TypeVar tvar) {
        return (JCTypeParameter)
            TypeParameter(name, null, // DPJ FIXME
        	    Types(types.getBounds(tvar))).setPos(pos).setType(tvar);
    }

    /** Create a list of type parameter trees from a list of type variables.
     */
    public List<JCTypeParameter> TypeParams(List<Type> typarams) {
        ListBuffer<JCTypeParameter> tparams = new ListBuffer<JCTypeParameter>();
        int i = 0;
        for (List<Type> l = typarams; l.nonEmpty(); l = l.tail)
            tparams.append(TypeParam(l.head.tsym.name, (TypeVar)l.head));
        return tparams.toList();
    }

    /** Create a value parameter tree from its name, type, and owner.
     */
    public JCVariableDecl Param(Name name, Type argtype, Symbol owner) {
        return VarDef(new VarSymbol(0, name, argtype, owner), null);
    }

    /** Create a a list of value parameter trees x0, ..., xn from a list of
     *  their types and an their owner.
     */
    public List<JCVariableDecl> Params(List<Type> argtypes, Symbol owner) {
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        MethodSymbol mth = (owner.kind == MTH) ? ((MethodSymbol)owner) : null;
        if (mth != null && mth.params != null && argtypes.length() == mth.params.length()) {
            for (VarSymbol param : ((MethodSymbol)owner).params)
                params.append(VarDef(param, null));
        } else {
            int i = 0;
            for (List<Type> l = argtypes; l.nonEmpty(); l = l.tail)
                params.append(Param(paramName(i++), l.head, owner));
        }
        return params.toList();
    }

    /** Wrap a method invocation in an expression statement or return statement,
     *  depending on whether the method invocation expression's type is void.
     */
    public JCStatement Call(JCExpression apply) {
        return apply.type.tag == VOID ? Exec(apply) : Return(apply);
    }

    /** Construct an assignment from a variable symbol and a right hand side.
     */
    public JCStatement Assignment(Symbol v, JCExpression rhs) {
        return Exec(Assign(Ident(v), rhs).setType(v.type));
    }

    /** Construct an index expression from a variable and an expression.
     */
    public JCArrayAccess Indexed(Symbol v, JCExpression index) {
        JCArrayAccess tree = new JCArrayAccess(QualIdent(v), index);
        tree.type = ((ArrayType)v.type).elemtype;
        return tree;
    }

    /** Make an attributed type cast expression.
     */
    public JCTypeCast TypeCast(Type type, JCExpression expr) {
        return (JCTypeCast)TypeCast(Type(type), expr).setType(type);
    }

/* ***************************************************************************
 * Helper methods.
 ****************************************************************************/

    /** Can given symbol be referred to in unqualified form?
     */
    public boolean isUnqualifiable(Symbol sym) {
        if (sym.name == names.empty ||
            sym.owner == null ||
            sym.owner.kind == MTH || sym.owner.kind == VAR ||
            (sym.owner instanceof PackageSymbol && ((PackageSymbol)(sym.owner)).isUnnamed())) {
            return true;
        } else if (sym.kind == TYP && toplevel != null) {
            Scope.Entry e;
            e = toplevel.namedImportScope.lookup(sym.name);
            if (e.scope != null) {
                return
                  e.sym == sym &&
                  e.next().scope == null;
            }
            e = toplevel.packge.members().lookup(sym.name);
            if (e.scope != null) {
                return
                  e.sym == sym &&
                  e.next().scope == null;
            }
            e = toplevel.starImportScope.lookup(sym.name);
            if (e.scope != null) {
                return
                  e.sym == sym &&
                  e.next().scope == null;
            }
        }
        return false;
    }

    /** The name of synthetic parameter number `i'.
     */
    public Name paramName(int i)   { return names.fromString("x" + i); }

    /** The name of synthetic type parameter number `i'.
     */
    public Name typaramName(int i) { return names.fromString("A" + i); }
}
    
