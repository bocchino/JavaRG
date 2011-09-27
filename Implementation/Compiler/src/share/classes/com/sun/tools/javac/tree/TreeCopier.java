/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.HashSet;
import java.util.Set;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.AtomicTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CobeginTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DPJForLoopTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EffectTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.FinishTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.NonintTree;
import com.sun.source.tree.ParamInfoTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.RPLEltTree;
import com.sun.source.tree.RPLTree;
import com.sun.source.tree.RegionParamTypeTree;
import com.sun.source.tree.RegionParameterTree;
import com.sun.source.tree.RegionTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SpawnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.DPJAtomic;
import com.sun.tools.javac.tree.JCTree.DPJCobegin;
import com.sun.tools.javac.tree.JCTree.DPJEffect;
import com.sun.tools.javac.tree.JCTree.DPJFinish;
import com.sun.tools.javac.tree.JCTree.DPJForLoop;
import com.sun.tools.javac.tree.JCTree.DPJNonint;
import com.sun.tools.javac.tree.JCTree.DPJParamInfo;
import com.sun.tools.javac.tree.JCTree.DPJRegionParameter;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathListElt;
import com.sun.tools.javac.tree.JCTree.DPJSpawn;
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
import com.sun.tools.javac.tree.JCTree.DPJRegionDecl;
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
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Pair;

/**
 * Creates a copy of a tree, using a given TreeMaker.
 * Names, literal values, etc are shared with the original.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class TreeCopier<P> implements TreeVisitor<JCTree,P> {
    private TreeMaker M;
    
    /** Creates a new instance of TreeCopier */
    public TreeCopier(TreeMaker M) {
        this.M = M;
    }
    
    public <T extends JCTree> T copy(T tree) {
        return copy(tree, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends JCTree> T copy(T tree, P p) {
        if (tree == null)
            return null;
        T result = (T) (tree.accept(this, p));
        // DPJ STM: Make tree copying preserve types
        if (result.type == null)
            result.type = tree.type;
        return result;
    }
    
    public <T extends JCTree> List<T> copy(List<T> trees) {
        return copy(trees, null);
    }
    
    public <T extends JCTree> List<T> copy(List<T> trees, P p) {
        if (trees == null)
            return null;
        ListBuffer<T> lb = new ListBuffer<T>();
        for (T tree: trees)
            lb.append(copy(tree, p));
        return lb.toList();
    }

    public JCTree visitAnnotation(AnnotationTree node, P p) {
        JCAnnotation t = (JCAnnotation) node;
        JCTree annotationType = copy(t.annotationType, p);
        List<JCExpression> args = copy(t.args, p);
        return M.at(t.pos).Annotation(annotationType, args);
    }

    public JCTree visitAssert(AssertTree node, P p) {
        JCAssert t = (JCAssert) node;
        JCExpression cond = copy(t.cond, p);
        JCExpression detail = copy(t.detail, p);
        return M.at(t.pos).Assert(cond, detail);
    }

    public JCTree visitAssignment(AssignmentTree node, P p) {
        JCAssign t = (JCAssign) node;
        JCExpression lhs = copy(t.lhs, p);
        JCExpression rhs = copy(t.rhs, p);
        return M.at(t.pos).Assign(lhs, rhs);
    }

    public JCTree visitCompoundAssignment(CompoundAssignmentTree node, P p) {
        JCAssignOp t = (JCAssignOp) node;
        JCTree lhs = copy(t.lhs, p);
        JCTree rhs = copy(t.rhs, p);
        return M.at(t.pos).Assignop(t.getTag(), lhs, rhs);
    }

    public JCTree visitBinary(BinaryTree node, P p) {
        JCBinary t = (JCBinary) node;
        JCExpression lhs = copy(t.lhs, p);
        JCExpression rhs = copy(t.rhs, p);
        return M.at(t.pos).Binary(t.getTag(), lhs, rhs);
    }

    public JCTree visitBlock(BlockTree node, P p) {
        JCBlock t = (JCBlock) node;
        List<JCStatement> stats = copy(t.stats, p);
        return M.at(t.pos).Block(t.flags, stats);
    }

    public JCTree visitBreak(BreakTree node, P p) {
        JCBreak t = (JCBreak) node;
        return M.at(t.pos).Break(t.label);
    }

    public JCTree visitCase(CaseTree node, P p) {
        JCCase t = (JCCase) node;
        JCExpression pat = copy(t.pat, p);
        List<JCStatement> stats = copy(t.stats, p);
        return M.at(t.pos).Case(pat, stats);
    }

    public JCTree visitCatch(CatchTree node, P p) {
        JCCatch t = (JCCatch) node;
        JCVariableDecl param = copy(t.param, p);
        JCBlock body = copy(t.body, p);
        return M.at(t.pos).Catch(param, body);
    }

    public JCTree visitClass(ClassTree node, P p) {
        JCClassDecl t = (JCClassDecl) node;
        JCModifiers mods = copy(t.mods, p);
        DPJParamInfo rgnparamInfo = copy(t.paramInfo, p);
        List<JCTypeParameter> typarams = copy(t.typarams, p);
        JCTree extending = copy(t.extending, p);
        List<JCExpression> implementing = copy(t.implementing, p);
        List<JCTree> defs = copy(t.defs, p);
        JCClassDecl result = M.at(t.pos).ClassDef(mods, t.name, rgnparamInfo, 
        	typarams, extending, implementing, defs);
        result.sym = t.sym;
        return result;
    }

    public JCTree visitConditionalExpression(ConditionalExpressionTree node, P p) {
        JCConditional t = (JCConditional) node;
        JCExpression cond = copy(t.cond, p);
        JCExpression truepart = copy(t.truepart, p);
        JCExpression falsepart = copy(t.falsepart, p);
        return M.at(t.pos).Conditional(cond, truepart, falsepart);
    }

    public JCTree visitContinue(ContinueTree node, P p) {
        JCContinue t = (JCContinue) node;
        return M.at(t.pos).Continue(t.label);
    }

    public JCTree visitDoWhileLoop(DoWhileLoopTree node, P p) {
        JCDoWhileLoop t = (JCDoWhileLoop) node;
        JCStatement body = copy(t.body, p);
        JCExpression cond = copy(t.cond, p);
        return M.at(t.pos).DoLoop(body, cond);
    }

    public JCTree visitErroneous(ErroneousTree node, P p) {
        JCErroneous t = (JCErroneous) node;
        List<? extends JCTree> errs = copy(t.errs, p);
        return M.at(t.pos).Erroneous(errs);
    }

    public JCTree visitExpressionStatement(ExpressionStatementTree node, P p) {
        JCExpressionStatement t = (JCExpressionStatement) node;
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).Exec(expr);
    }

    public JCTree visitEnhancedForLoop(EnhancedForLoopTree node, P p) {
        JCEnhancedForLoop t = (JCEnhancedForLoop) node;
        JCVariableDecl var = copy(t.var, p);
        JCExpression expr = copy(t.expr, p);
        JCStatement body = copy(t.body, p);
        return M.at(t.pos).ForeachLoop(var, expr, body);
    }

    public JCTree visitForLoop(ForLoopTree node, P p) {
        JCForLoop t = (JCForLoop) node;
        List<JCStatement> init = copy(t.init, p);
        JCExpression cond = copy(t.cond, p);
        List<JCExpressionStatement> step = copy(t.step, p);
        JCStatement body = copy(t.body, p);
        return M.at(t.pos).ForLoop(init, cond, step, body);
    }

    public JCTree visitIdentifier(IdentifierTree node, P p) {
        JCIdent t = (JCIdent) node;
        JCIdent result = M.at(t.pos).Ident(t.name);
        result.sym = t.sym;
        result.rpl = t.rpl;
        return result;
    }

    public JCTree visitIf(IfTree node, P p) {
        JCIf t = (JCIf) node;
        JCExpression cond = copy(t.cond, p);
        JCStatement thenpart = copy(t.thenpart, p);
        JCStatement elsepart = copy(t.elsepart, p);
        return M.at(t.pos).If(cond, thenpart, elsepart);
    }

    public JCTree visitImport(ImportTree node, P p) {
        JCImport t = (JCImport) node;
        JCTree qualid = copy(t.qualid, p);
        return M.at(t.pos).Import(qualid, t.staticImport);
    }

    public JCTree visitArrayAccess(ArrayAccessTree node, P p) {
        JCArrayAccess t = (JCArrayAccess) node;
        JCExpression indexed = copy(t.indexed, p);
        JCExpression index = copy(t.index, p);
        JCArrayAccess result = M.at(t.pos).Indexed(indexed, index);
        result.rpl = t.rpl;
        return result;
    }

    public JCTree visitLabeledStatement(LabeledStatementTree node, P p) {
        JCLabeledStatement t = (JCLabeledStatement) node;
        JCStatement body = copy(t.body, p);
        return M.at(t.pos).Labelled(t.label, t.body);
    }

    public JCTree visitLiteral(LiteralTree node, P p) {
        JCLiteral t = (JCLiteral) node;
        return M.at(t.pos).Literal(t.typetag, t.value);
    }

    public JCTree visitMethod(MethodTree node, P p) {
        JCMethodDecl t  = (JCMethodDecl) node;
        JCModifiers mods = copy(t.mods, p);
        JCExpression restype = copy(t.restype, p);
        DPJParamInfo rgnParamInfo = copy(t.paramInfo, p);
        List<JCTypeParameter> typarams = copy(t.typarams, p);
        List<JCVariableDecl> params = copy(t.params, p);
        List<JCExpression> thrown = copy(t.thrown, p);
        JCBlock body = copy(t.body, p);
        JCExpression defaultValue = copy(t.defaultValue, p);
        DPJEffect effects = t.effects;
        JCMethodDecl result = M.at(t.pos).MethodDef(mods, t.name, restype, rgnParamInfo,
        	typarams, params, thrown, body, defaultValue, effects);
        result.sym = t.sym;
        return result;
    }

    public JCTree visitMethodInvocation(MethodInvocationTree node, P p) {
        JCMethodInvocation t = (JCMethodInvocation) node;
        List<DPJRegionPathList> regionArgs = copy(t.regionArgs, p);
        List<JCExpression> typeargs = copy(t.typeargs, p);
        List<DPJEffect> effectargs = copy(t.effectargs, p);
        JCExpression meth = copy(t.meth, p);
        List<JCExpression> args = copy(t.args, p);
        return M.at(t.pos).Apply(regionArgs, typeargs, effectargs, meth, args);
    }

    public JCTree visitModifiers(ModifiersTree node, P p) {
        JCModifiers t = (JCModifiers) node;
        List<JCAnnotation> annotations = copy(t.annotations, p);
        return M.at(t.pos).Modifiers(t.flags, annotations);
    }

    public JCTree visitNewArray(NewArrayTree node, P p) {
        JCNewArray t = (JCNewArray) node;
        JCExpression elemtype = copy(t.elemtype, p);
        List<JCExpression> dims = copy(t.dims, p);
        List<DPJRegionPathList> rpls = copy(t.rpls, p);
        List<JCExpression> elems = copy(t.elems, p);
        List<JCIdent> indexVars = copy(t.indexVars, p);
        JCNewArray result = M.at(t.pos).NewArray(elemtype, dims, rpls, elems);
        result.indexVars = indexVars;
        return result;
    }

    public JCTree visitNewClass(NewClassTree node, P p) {
        JCNewClass t = (JCNewClass) node;
        JCExpression encl = copy(t.encl, p);
        List<DPJRegionPathList> regionArgs = copy(t.regionArgs, p);
        List<JCExpression> typeargs = copy(t.typeargs, p);
        List<DPJEffect> effectargs = copy(t.effectargs, p);
        JCExpression clazz = copy(t.clazz, p);
        List<JCExpression> args = copy(t.args, p);
        JCClassDecl def = copy(t.def, p);
        JCNewClass result = M.at(t.pos).NewClass(encl, regionArgs, typeargs, 
        	effectargs, clazz, args, def);
        result.constructor = t.constructor;
        return result;
    }

    public JCTree visitParenthesized(ParenthesizedTree node, P p) {
        JCParens t = (JCParens) node;
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).Parens(expr);
    }

    public JCTree visitReturn(ReturnTree node, P p) {
        JCReturn t = (JCReturn) node;
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).Return(expr);
    }

    public JCTree visitMemberSelect(MemberSelectTree node, P p) {
        JCFieldAccess t = (JCFieldAccess) node;
        JCExpression selected = copy(t.selected, p);
        JCFieldAccess result = M.at(t.pos).Select(selected, t.name);
        result.sym = t.sym;
        result.rpl = t.rpl;
        return result;
    }

    public JCTree visitEmptyStatement(EmptyStatementTree node, P p) {
        JCSkip t = (JCSkip) node;
        return M.at(t.pos).Skip();
    }

    public JCTree visitSwitch(SwitchTree node, P p) {
        JCSwitch t = (JCSwitch) node;
        JCExpression selector = copy(t.selector, p);
        List<JCCase> cases = copy(t.cases, p);
        return M.at(t.pos).Switch(selector, cases);
    }

    public JCTree visitSynchronized(SynchronizedTree node, P p) {
        JCSynchronized t = (JCSynchronized) node;
        JCExpression lock = copy(t.lock, p);
        JCBlock body = copy(t.body, p);
        return M.at(t.pos).Synchronized(lock, body);
    }

    public JCTree visitThrow(ThrowTree node, P p) {
        JCThrow t = (JCThrow) node;
        JCTree expr = copy(t.expr, p);
        return M.at(t.pos).Throw(expr);
    }

    public JCTree visitCompilationUnit(CompilationUnitTree node, P p) {
        JCCompilationUnit t = (JCCompilationUnit) node;
        List<JCAnnotation> packageAnnotations = copy(t.packageAnnotations, p);
        JCExpression pid = copy(t.pid, p);
        List<JCTree> defs = copy(t.defs, p);
        return M.at(t.pos).TopLevel(packageAnnotations, pid, defs);
    }

    public JCTree visitTry(TryTree node, P p) {
        JCTry t = (JCTry) node;
        JCBlock body = copy(t.body, p);
        List<JCCatch> catchers = copy(t.catchers, p);
        JCBlock finalizer = copy(t.finalizer, p);
        return M.at(t.pos).Try(body, catchers, finalizer);
    }

    public JCTree visitParameterizedType(ParameterizedTypeTree node, P p) {
        JCTypeApply t = (JCTypeApply) node;
        JCExpression clazz = copy(t.functor, p);
        List<JCExpression> arguments = copy(t.typeArgs, p);
        List<DPJRegionPathList> rplArgs = copy(t.rplArgs, p);
        List<DPJEffect> effectArgs = copy(t.effectArgs, p);
        return M.at(t.pos).TypeApply(clazz, arguments, rplArgs, effectArgs);
    }

    public JCTree visitArrayType(ArrayTypeTree node, P p) {
        JCArrayTypeTree t = (JCArrayTypeTree) node;
        JCExpression elemtype = copy(t.elemtype, p);
        return M.at(t.pos).TypeArray(elemtype, null, null);
    }

    public JCTree visitTypeCast(TypeCastTree node, P p) {
        JCTypeCast t = (JCTypeCast) node;
        JCTree clazz = copy(t.clazz, p);
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).TypeCast(clazz, expr);
    }

    public JCTree visitPrimitiveType(PrimitiveTypeTree node, P p) {
        JCPrimitiveTypeTree t = (JCPrimitiveTypeTree) node;
        return M.at(t.pos).TypeIdent(t.typetag);
    }

    public JCTree visitTypeParameter(TypeParameterTree node, P p) {
        JCTypeParameter t = (JCTypeParameter) node;
        List<DPJRegionParameter> rplparams = copy(t.rplparams, p);
        List<JCExpression> bounds = copy(t.bounds, p);
        return M.at(t.pos).TypeParameter(t.name, rplparams, bounds);
    }

    public JCTree visitInstanceOf(InstanceOfTree node, P p) {
        JCInstanceOf t = (JCInstanceOf) node;
        JCExpression expr = copy(t.expr, p);
        JCTree clazz = copy(t.clazz, p);
        return M.at(t.pos).TypeTest(expr, clazz);
    }

    public JCTree visitUnary(UnaryTree node, P p) {
        JCUnary t = (JCUnary) node;
        JCExpression arg = copy(t.arg, p);
        return M.at(t.pos).Unary(t.getTag(), arg);
    }

    public JCTree visitVariable(VariableTree node, P p) {
        JCVariableDecl t = (JCVariableDecl) node;
        JCModifiers mods = copy(t.mods, p);
        JCExpression vartype = copy(t.vartype, p);
        JCExpression init = copy(t.init, p);
        JCVariableDecl result = M.at(t.pos).VarDef(mods, t.name, t.rpl, vartype, init); // DPJ
        result.sym = t.sym;
        return result;
    }

    public JCTree visitWhileLoop(WhileLoopTree node, P p) {
        JCWhileLoop t = (JCWhileLoop) node;
        JCStatement body = copy(t.body, p);
        JCExpression cond = copy(t.cond, p);
        return M.at(t.pos).WhileLoop(cond, body);
    }

    public JCTree visitWildcard(WildcardTree node, P p) {
        JCWildcard t = (JCWildcard) node;
        TypeBoundKind kind = M.at(t.kind.pos).TypeBoundKind(t.kind.kind);
        JCTree inner = copy(t.inner, p);
        return M.at(t.pos).Wildcard(kind, inner);
    }

    public JCTree visitOther(com.sun.source.tree.Tree node, P p) {
        JCTree tree = (JCTree) node;
        switch (tree.getTag()) {
            case JCTree.LETEXPR: {
                LetExpr t = (LetExpr) node;
                List<JCVariableDecl> defs = copy(t.defs, p);
                JCTree expr = copy(t.expr, p);
                return M.at(t.pos).LetExpr(defs, expr);
            }
            default:
                throw new AssertionError("unknown tree tag: " + tree.getTag());
        }
    }

    public JCTree visitRegion(RegionTree node, P p) {
        DPJRegionDecl t = (DPJRegionDecl) node;
        JCModifiers mods = copy(t.mods, p);
        DPJRegionDecl result = M.at(t.pos).RegionDecl(mods, t.name, t.isAtomic);
        result.sym = t.sym;
        return result;
    }
    
    public JCTree visitRPLElt(RPLEltTree node, P p) {
	DPJRegionPathListElt t = (DPJRegionPathListElt) node;
	JCExpression exp = copy(t.exp, p);
	return M.at(t.pos).RegionPathListElt(t.exp, t.type);
    }

    public JCTree visitRPL(RPLTree node, P p) {
	DPJRegionPathList t = (DPJRegionPathList) node;
	List<DPJRegionPathListElt> elts = copy(t.elts, p);
	return M.at(t.pos).RegionPathList(elts);
    }
    
    public JCTree visitMethEffects(EffectTree node, P p) {
	DPJEffect t = (DPJEffect) node;
	List<DPJRegionPathList> readEffects = copy(t.readEffects, p);
	List<DPJRegionPathList> writeEffects = copy(t.writeEffects, p);
	List<JCIdent> variableEffects = copy(t.variableEffects, p);
	return M.at(t.pos).Effect(t.isPure, readEffects, writeEffects,
		variableEffects);
    }
    
    public JCTree visitRegionParameter(RegionParameterTree node, P p) {
	DPJRegionParameter t = (DPJRegionParameter) node;
	DPJRegionPathList bound = copy(t.bound, p);
	DPJRegionParameter result = M.at(t.pos).RegionParameter(t.name, bound, t.isAtomic);
	result.sym = t.sym;
	return result;
    }
    
    public JCTree visitParamInfo(ParamInfoTree node, P p) {
	DPJParamInfo t = (DPJParamInfo) node;
        List<DPJRegionParameter> params = copy(t.rplParams, p);
        ListBuffer<Pair<DPJRegionPathList,DPJRegionPathList>> rplConstraints = 
            ListBuffer.lb();
        for (Pair<DPJRegionPathList,DPJRegionPathList> pair : t.rplConstraints) {
            rplConstraints.append(new Pair<DPJRegionPathList,DPJRegionPathList>(pair.fst, 
        	    pair.snd));
        }
        List<JCIdent> effectParams = copy(t.effectParams, p);
        ListBuffer<Pair<DPJEffect,DPJEffect>> effectConstraints = ListBuffer.lb();
        if (t.effectConstraints != null) {
            for (Pair<DPJEffect,DPJEffect> pair : t.effectConstraints) {
                effectConstraints.append(new Pair<DPJEffect,DPJEffect>(pair.fst,pair.snd));
            }
        }
        return M.at(t.pos).ParamInfo(params, rplConstraints.toList(), 
        	effectParams, effectConstraints.toList());
    }
    
    public JCTree visitSpawn(SpawnTree node, P p) {
	DPJSpawn t = (DPJSpawn) node;
	JCStatement body = copy(t.body, p);
	return M.at(t.pos).Spawn(body);
    }
    
    public JCTree visitFinish(FinishTree node, P p) {
	DPJFinish t = (DPJFinish) node;
	JCStatement body = copy(t.body, p);
	return M.at(t.pos).Finish(body);
    }
    
    public JCTree visitCobegin(CobeginTree node, P p) {
	DPJCobegin t = (DPJCobegin) node;
	JCStatement body = copy(t.body, p);
	DPJCobegin result = M.at(t.pos).Cobegin(body, t.isNondet);
	
	result.bodySize = t.bodySize;
	result.declaredVars = new Set[t.bodySize];
	result.definedVars = new Set[t.bodySize];
	result.usedVars = new Set[t.bodySize];
	for (int i = 0; i < t.bodySize; i++) {
	    result.declaredVars[i] = new HashSet<VarSymbol>(t.declaredVars[i]);
	    result.definedVars[i] = new HashSet<VarSymbol>(t.definedVars[i]);
	    result.usedVars[i] = new HashSet<VarSymbol>(t.usedVars[i]);
	}
	
	return result;
    }
    
    public JCTree visitAtomic(AtomicTree node, P p) {
	DPJAtomic t = (DPJAtomic) node;
	JCStatement body = copy(t.body, p);
	DPJAtomic result = M.at(t.pos).Atomic(body);
	
	result.declaredVars = new HashSet<VarSymbol>(t.declaredVars);
	result.definedVars = new HashSet<VarSymbol>(t.definedVars);
	result.usedVars = new HashSet<VarSymbol>(t.usedVars);
	result.aliveAtEnd = t.aliveAtEnd;
	
	return result;
    }
    
    public JCTree visitNonint(NonintTree node, P p) {
	DPJNonint t = (DPJNonint) node;
	JCStatement body = copy(t.body, p);
	DPJNonint result = M.at(t.pos).Nonint(body);
	return result;	
    }

    public JCTree visitDPJForLoop(DPJForLoopTree node, P p) {
	DPJForLoop t = (DPJForLoop) node;
	JCExpression start = copy(t.start, p);
	JCExpression length = copy(t.length, p);
	JCExpression stride = copy(t.stride, p);
	JCStatement body = copy(t.body, p);
	DPJForLoop result = M.at(t.pos).DPJForLoop(t.var, start, length, stride, body, t.isNondet);
	
	result.declaredVars = new HashSet<VarSymbol>(t.declaredVars);
	result.definedVars = new HashSet<VarSymbol>(t.definedVars);
	result.usedVars = new HashSet<VarSymbol>(t.usedVars);
	
	return result;
    }
    
 }
