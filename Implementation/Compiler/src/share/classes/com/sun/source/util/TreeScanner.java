/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.source.util;

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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.CopyPermTree;
import com.sun.source.tree.JRGForLoopTree;
import com.sun.source.tree.DerefSetTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EffectPermTree;
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
import com.sun.source.tree.MethodPermsTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.NonintTree;
import com.sun.source.tree.ParamInfoTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PardoTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.RPLEltTree;
import com.sun.source.tree.RPLTree;
import com.sun.source.tree.RefGroupDeclTree;
import com.sun.source.tree.RefPermTree;
import com.sun.source.tree.RegionDeclTree;
import com.sun.source.tree.RegionParamTypeTree;
import com.sun.source.tree.RegionParameterTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SpawnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;

/**
 * A TreeVisitor that visits all the child tree nodes.
 * To visit nodes of a particular type, just override the 
 * corresponding visitXYZ method.
 * Inside your method, call super.visitXYZ to visit descendant
 * nodes.
 *
 * <p>The default implementation of the visitXYZ methods will determine
 * a result as follows:
 * <ul>
 * <li>If the node being visited has no children, the result will be null.
 * <li>If the node being visited has one child, the result will be the
 * result of calling {@code scan} on that child. The child may be a simple node
 * or itself a list of nodes.
 * <li> If the node being visited has more than one child, the result will
 * be determined by calling {@code scan} each child in turn, and then combining the
 * result of each scan after the first with the cumulative result
 * so far, as determined by the {@link #reduce} method. Each child may be either
 * a simple node of a list of nodes. The default behavior of the {@code reduce}
 * method is such that the result of the visitXYZ method will be the result of 
 * the last child scanned.
 * </ul>
 *
 * <p>Here is an example to count the number of identifier nodes in a tree:
 * <pre>
 *   class CountIdentifiers extends TreeScanner<Integer,Void> {
 *	{@literal @}Override
 *	public Integer visitIdentifier(IdentifierTree node, Void p) { 
 *	    return 1; 
 *	}
 *	{@literal @}Override
 *	public Integer reduce(Integer r1, Integer r2) { 
 *	    return (r1 == null ? 0 : r1) + (r2 == null ? 0 : r2); 
 *	}
 *   }
 * </pre>
 *
 * @author Peter von der Ah&eacute;
 * @author Jonathan Gibbons
 * @since 1.6
 */
public class TreeScanner<R,P> implements TreeVisitor<R,P> {

    /** Scan a single node.
     */
    public R scan(Tree node, P p) {
	return (node == null) ? null : node.accept(this, p);
    }
    
    private R scanAndReduce(Tree node, P p, R r) {
        return reduce(scan(node, p), r);
    }

    /** Scan a list of nodes.
     */
    public R scan(Iterable<? extends Tree> nodes, P p) {
	R r = null;
	if (nodes != null) {
            boolean first = true;
            for (Tree node : nodes) {
                r = (first ? scan(node, p) : scanAndReduce(node, p, r));
                first = false;
            }
        }
        return r;
    }
    
    private R scanAndReduce(Iterable<? extends Tree> nodes, P p, R r) {
        return reduce(scan(nodes, p), r);
    }
    
    /**
     * Reduces two results into a combined result.
     * The default implementation is to return the first parameter.
     * The general contract of the method is that it may take any action whatsoever.
     */
    public R reduce(R r1, R r2) {
        return r1;
    }


/* ***************************************************************************
 * Visitor methods
 ****************************************************************************/

    public R visitCompilationUnit(CompilationUnitTree node, P p) {
        R r = scan(node.getPackageAnnotations(), p);
	r = scanAndReduce(node.getPackageName(), p, r);
        r = scanAndReduce(node.getImports(), p, r);
	r = scanAndReduce(node.getTypeDecls(), p, r);
        return r;
    }

    public R visitImport(ImportTree node, P p) {
	return scan(node.getQualifiedIdentifier(), p);
    }

    public R visitClass(ClassTree node, P p) {
	R r = scan(node.getModifiers(), p);
	r = scanAndReduce(node.getTypeParameters(), p, r);
	r = scanAndReduce(node.getExtendsClause(), p, r);
	r = scanAndReduce(node.getImplementsClause(), p, r);
	r = scanAndReduce(node.getMembers(), p, r);
        return r;
    }

    public R visitMethod(MethodTree node, P p) {
	R r = scan(node.getModifiers(), p);
	r = scanAndReduce(node.getReturnType(), p, r);
	r = scanAndReduce(node.getTypeParameters(), p, r);
	r = scanAndReduce(node.getParameters(), p, r);
	r = scanAndReduce(node.getThrows(), p, r);
	r = scanAndReduce(node.getBody(), p, r);
        return r;
    }
	
    public R visitVariable(VariableTree node, P p) {
	R r = scan(node.getModifiers(), p);
	r = scanAndReduce(node.getType(), p, r);
	r = scanAndReduce(node.getInitializer(), p, r);
        return r;
    }
	
    public R visitEmptyStatement(EmptyStatementTree node, P p) {
        return null;
    }

    public R visitBlock(BlockTree node, P p) {
	return scan(node.getStatements(), p);
    }

    public R visitDoWhileLoop(DoWhileLoopTree node, P p) {
	R r = scan(node.getStatement(), p);
	r = scanAndReduce(node.getCondition(), p, r);
        return r;
    }

    public R visitWhileLoop(WhileLoopTree node, P p) {
	R r = scan(node.getCondition(), p);
	r = scanAndReduce(node.getStatement(), p, r);
        return r;
    }

    public R visitForLoop(ForLoopTree node, P p) {
	R r = scan(node.getInitializer(), p);
	r = scanAndReduce(node.getCondition(), p, r);
	r = scanAndReduce(node.getUpdate(), p, r);
	r = scanAndReduce(node.getStatement(), p, r);
        return r;
    }

    public R visitEnhancedForLoop(EnhancedForLoopTree node, P p) {
	R r = scan(node.getVariable(), p);
	r = scanAndReduce(node.getExpression(), p, r);
	r = scanAndReduce(node.getStatement(), p, r);
        return r;
    }

    public R visitLabeledStatement(LabeledStatementTree node, P p) {
	return scan(node.getStatement(), p);
    }

    public R visitSwitch(SwitchTree node, P p) {
	R r = scan(node.getExpression(), p);
	r = scanAndReduce(node.getCases(), p, r);
        return r;
    }

    public R visitCase(CaseTree node, P p) {
	R r = scan(node.getExpression(), p);
	r = scanAndReduce(node.getStatements(), p, r);
        return r;
    }

    public R visitSynchronized(SynchronizedTree node, P p) {
	R r = scan(node.getExpression(), p);
	r = scanAndReduce(node.getBlock(), p, r);
        return r;
    }

    public R visitTry(TryTree node, P p) {
	R r = scan(node.getBlock(), p);
	r = scanAndReduce(node.getCatches(), p, r);
	r = scanAndReduce(node.getFinallyBlock(), p, r);
        return r;
    }

    public R visitCatch(CatchTree node, P p) {
	R r = scan(node.getParameter(), p);
	r = scanAndReduce(node.getBlock(), p, r);
        return r;
    }

    public R visitConditionalExpression(ConditionalExpressionTree node, P p) {
	R r = scan(node.getCondition(), p);
	r = scanAndReduce(node.getTrueExpression(), p, r);
	r = scanAndReduce(node.getFalseExpression(), p, r);
        return r;
    }

    public R visitIf(IfTree node, P p) {
	R r = scan(node.getCondition(), p);
	r = scanAndReduce(node.getThenStatement(), p, r);
	r = scanAndReduce(node.getElseStatement(), p, r);
        return r;
    }

    public R visitExpressionStatement(ExpressionStatementTree node, P p) {
	return scan(node.getExpression(), p);
    }

    public R visitBreak(BreakTree node, P p) {
        return null;
    }

    public R visitContinue(ContinueTree node, P p) {
        return null;
    }

    public R visitReturn(ReturnTree node, P p) {
	return scan(node.getExpression(), p);
    }

    public R visitThrow(ThrowTree node, P p) {
	return scan(node.getExpression(), p);
    }

    public R visitAssert(AssertTree node, P p) {
	R r = scan(node.getCondition(), p);
	r = scanAndReduce(node.getDetail(), p, r);
        return r;
    }

    public R visitMethodInvocation(MethodInvocationTree node, P p) {
	R r = scan(node.getTypeArguments(), p);
	r = scanAndReduce(node.getMethodSelect(), p, r);
	r = scanAndReduce(node.getArguments(), p, r);
        return r;
    }

    public R visitNewClass(NewClassTree node, P p) {
	R r = scan(node.getEnclosingExpression(), p);
	r = scanAndReduce(node.getIdentifier(), p, r);
	r = scanAndReduce(node.getTypeArguments(), p, r);
        r = scanAndReduce(node.getArguments(), p, r);
	r = scanAndReduce(node.getClassBody(), p, r);
        return r;
    }

    public R visitNewArray(NewArrayTree node, P p) {
	R r = scan(node.getType(), p);
	r = scanAndReduce(node.getDimensions(), p, r);
	r = scanAndReduce(node.getInitializers(), p, r);
        return r;
    }

    public R visitParenthesized(ParenthesizedTree node, P p) {
	return scan(node.getExpression(), p);
    }

    public R visitAssignment(AssignmentTree node, P p) {
	R r = scan(node.getVariable(), p);
	r = scanAndReduce(node.getExpression(), p, r);
        return r;
    }

    public R visitCompoundAssignment(CompoundAssignmentTree node, P p) {
	R r = scan(node.getVariable(), p);
	r = scanAndReduce(node.getExpression(), p, r);
        return r;
    }

    public R visitUnary(UnaryTree node, P p) {
	return scan(node.getExpression(), p);
    }

    public R visitBinary(BinaryTree node, P p) {
	R r = scan(node.getLeftOperand(), p);
	r = scanAndReduce(node.getRightOperand(), p, r);
        return r;
    }

    public R visitTypeCast(TypeCastTree node, P p) {
	R r = scan(node.getType(), p);
	r = scanAndReduce(node.getExpression(), p, r);
        return r;
    }

    public R visitInstanceOf(InstanceOfTree node, P p) {
	R r = scan(node.getExpression(), p);
	r = scanAndReduce(node.getType(), p, r);
        return r;
    }

    public R visitArrayAccess(ArrayAccessTree node, P p) {
	R r = scan(node.getExpression(), p);
	r = scanAndReduce(node.getIndex(), p, r);
        return r;
    }

    public R visitMemberSelect(MemberSelectTree node, P p) {
	return scan(node.getExpression(), p);
    }

    public R visitIdentifier(IdentifierTree node, P p) {
        return null;
    }

    public R visitLiteral(LiteralTree node, P p) {
        return null;
    }

    public R visitPrimitiveType(PrimitiveTypeTree node, P p) {
        return null;
    }

    public R visitArrayType(ArrayTypeTree node, P p) {
    	return scan(node.getType(), p);
    }

    public R visitParameterizedType(ParameterizedTypeTree node, P p) {
	R r = scan(node.getType(), p);
	r = scanAndReduce(node.getTypeArguments(), p, r);
        return r;
    }

    public R visitTypeParameter(TypeParameterTree node, P p) {
	return scan(node.getBounds(), p);
    }

    public R visitWildcard(WildcardTree node, P p) {
        return scan(node.getBound(), p);
    }

    public R visitModifiers(ModifiersTree node, P p) {
	return scan(node.getAnnotations(), p);
    }

    public R visitAnnotation(AnnotationTree node, P p) {
	R r = scan(node.getAnnotationType(), p);
	r = scanAndReduce(node.getArguments(), p, r);
        return r;
    }

    public R visitOther(Tree node, P p) {
	return null;
    }

    public R visitErroneous(ErroneousTree node, P p) {
        return null;
    }

    public R visitRegion(RegionDeclTree node, P p) { // DPJ
	return scan(node.getModifiers(), p);
    }
    
    public R visitRefGroupDecl(RefGroupDeclTree node, P p) {
	return null;
    }
    
    public R visitRPLElt(RPLEltTree node, P p) { // DPJ
	return null; // DPJ FIXME
    }

    public R visitRPL(RPLTree node, P p) { // DPJ
	return null; // DPJ FIXME
    }
    
    public R visitRefPerm(RefPermTree node, P p) {
	return null;
    }
    
    public R visitMethodPerms(MethodPermsTree node, P p) {
	return null;
    }
    
    public R visitDerefSet(DerefSetTree node, P p) {
	return null;
    }
    
    public R visitCopyPerm(CopyPermTree node, P p) {
	return null;
    }

    public R visitEffectPerms(EffectPermTree node, P p) { // DPJ
	return null; // DPJ FIXME
    }
    
    public R visitRegionParameter(RegionParameterTree node, P p) {
	return null; // DPJ FIXME
    }
    
    public R visitParamInfo(ParamInfoTree node, P p) {
	return null; // DPJ FIXME
    }
    
    public R visitRegionParamType(RegionParamTypeTree node, P p) {
	return null; // DPJ FIXME
    }
    
    public R visitSpawn(SpawnTree node, P p) {
	return null; // DPJ FIXME
    }
    
    public R visitFinish(FinishTree node, P p) {
	return null; // DPJ FIXME
    }
    
    public R visitPardo(PardoTree node, P p) {
	return null; // DPJ FIXME
    }

    public R visitJRGForLoop(JRGForLoopTree node, P p) {
	return null; // DPJ FIXME
    }
    
    public R visitAtomic(AtomicTree node, P p) {
	return null; // FIXME
    }
    
    public R visitNonint(NonintTree node, P p) {
	return null; // FIXME
    }
}
