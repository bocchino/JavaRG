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

import static com.sun.tools.javac.code.Flags.ANNOTATION;
import static com.sun.tools.javac.code.Flags.ARRAYCONSTR;
import static com.sun.tools.javac.code.Flags.DPJStandardFlags;
import static com.sun.tools.javac.code.Flags.ENUM;
import static com.sun.tools.javac.code.Flags.INTERFACE;
import static com.sun.tools.javac.code.Flags.SYNTHETIC;
import static com.sun.tools.javac.code.Flags.VARARGS;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree.JRGForLoop;
import com.sun.tools.javac.tree.JCTree.DPJNegationExpression;
import com.sun.tools.javac.tree.JCTree.DPJParamInfo;
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
import com.sun.tools.javac.tree.JCTree.JRGPardo;
import com.sun.tools.javac.tree.JCTree.JRGRefGroupDecl;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Pair;

/** Prints out a tree as an indented Java source program.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class Pretty extends JCTree.Visitor {

    /**
     * No code gen, just pretty print the AST
     */
    public static final int NONE = 0;

    /**
     * Sequential code gen
     */
    public static final int SEQ = 1;
    
    /**
     * Sequential code gen with instrumentation
     */
    public static final int SEQ_INST = 2;
    
    /**
     * Parallel code gen
     */
    public static final int PAR = 3;
    
    /**
     * Code generation mode.  We are using the Pretty printer as a code generator, 
     * so we have to keep track of what kind of code generation we are doing.
     */
    public int codeGenMode = NONE;
    
    /**
     * Set if we need to compile to sequential code.
     */
    public boolean sequential = false;
    
    private Log log;
    
    public Pretty(Writer out, boolean sourceOutput,
	          int codeGenMode) {
        this.out = out;
        this.sourceOutput = sourceOutput;
	this.codeGenMode = codeGenMode;
	JCTree.codeGenMode = codeGenMode;
	switch (codeGenMode) {
	case SEQ_INST:
	case SEQ:
	    this.sequential = true;
	default:
	    break;	
	}
	// Don't print out DPJ type annotations if we are generating code
	Types.printDPJ = (codeGenMode == NONE);
    }
    
    /** We need to rewrite variable symbols during code generation
     *  for instanceof switch.
     */
    Symbol variableToMangle;
    String prefix = "__jrg";
    int suffix;    
    
    // Note:  The following two methods are a HACK to work around
    // the fact that the javac implementation of enums is broken! Without
    // these flags, we get strange output that is not legal
    // Java code.
    //
    /** Set if we are in an enum block
     */
    public boolean inEnumBlock = false;

    /** Set if we are in an enum variable declaration
     */
    public boolean inEnumVarDecl = false;
    
    /**
     * Set if we need to write out 'this' as '__jrg_this'
     */
    private boolean needDPJThis = false;
    
    /** Set when we are producing source output.  If we're not
     *  producing source output, we can sometimes give more detail in
     *  the output even though that detail would not be valid java
     *  source.
     */
    private final boolean sourceOutput;

    /** The output stream on which trees are printed.
     */
    Writer out;

    /** Indentation width (can be reassigned from outside).
     */
    public int width = 4;

    /** The current left margin.
     */
    int lmargin = 0;

    /** The enclosing class name.
     */
    Name enclClassName;

    /** A hashtable mapping trees to their documentation comments
     *  (can be null)
     */
    Map<JCTree, String> docComments = null;

    /** Align code to be indented to left margin.
     */
    void align() throws IOException {
        for (int i = 0; i < lmargin; i++) out.write(" ");
    }

    /** Increase left margin by indentation width.
     */
    void indent() {
        lmargin = lmargin + width;
    }

    /** Decrease left margin by indentation width.
     */
    void undent() {
        lmargin = lmargin - width;
    }

    /** Enter a new precedence level. Emit a `(' if new precedence level
     *  is less than precedence level so far.
     *  @param contextPrec    The precedence level in force so far.
     *  @param ownPrec        The new precedence level.
     */
    void open(int contextPrec, int ownPrec) throws IOException {
        if (ownPrec < contextPrec) out.write("(");
    }

    /** Leave precedence level. Emit a `(' if inner precedence level
     *  is less than precedence level we revert to.
     *  @param contextPrec    The precedence level we revert to.
     *  @param ownPrec        The inner precedence level.
     */
    void close(int contextPrec, int ownPrec) throws IOException {
        if (ownPrec < contextPrec) out.write(")");
    }

    /** Print string, replacing all non-ascii character with unicode escapes.
     */
    public void print(Object s) throws IOException {
        out.write(Convert.escapeUnicode(s.toString()));
    }
    public void printAligned(Object s) throws IOException {
	align();
	print(s);
    }

    /** Print new line.
     */
    public void println() throws IOException {
        out.write(lineSep);
    }

    String lineSep = System.getProperty("line.separator");
    
    /** Mangle a variable name
     */
    private String mangle(Name name) {
	return mangle(name.toString(), this.suffix);
    }
    private String mangle(String name, int suffix) {
	return prefix + "_" + name + "_" + suffix;
    }

    /**************************************************************************
     * Traversal methods
     *************************************************************************/

    /** Exception to propagate IOException through visitXXX methods */
    protected static class UncheckedIOException extends Error {
	static final long serialVersionUID = -4032692679158424751L;
        UncheckedIOException(IOException e) {
            super(e.getMessage(), e);
        }
    }

    /** Visitor argument: the current precedence level.
     */
    int prec;

    /** Visitor method: print expression tree.
     *  @param prec  The current precedence level.
     */
    public void printExpr(JCTree tree, int prec) throws IOException {
        int prevPrec = this.prec;
        try {
            this.prec = prec;
            if (tree == null) print("/*missing*/");
            else {
                tree.accept(this);
            }
        } catch (UncheckedIOException ex) {
            IOException e = new IOException(ex.getMessage());
            e.initCause(ex);
            throw e;
        } finally {
            this.prec = prevPrec;
        }
    }

    /** Derived visitor method: print expression tree at minimum precedence level
     *  for expression.
     */
    public void printExpr(JCTree tree) throws IOException {
        printExpr(tree, TreeInfo.noPrec);
    }

    /** Derived visitor method: print statement tree.
     */
    public void printStat(JCTree tree) throws IOException {
        printExpr(tree, TreeInfo.notExpression);
    }

    /** Derived visitor method: print list of expression trees, separated by given string.
     *  @param sep the separator string
     */
    public <T extends JCTree> void printExprs(List<T> trees, String sep) throws IOException {
        if (trees.nonEmpty()) {
            printExpr(trees.head);
            for (List<T> l = trees.tail; l.nonEmpty(); l = l.tail) {
                print(sep);
                printExpr(l.head);
            }
        }
    }

    /** Derived visitor method: print list of expression trees, separated by commas.
     */
    public <T extends JCTree> void printExprs(List<T> trees) throws IOException {
        printExprs(trees, ", ");
    }

    /** Derived visitor method: print list of statements, each on a separate line.
     */
    public void printStats(List<? extends JCTree> trees) throws IOException {
	for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
            if ((codeGenMode == NONE) || 
        	    (!(l.head instanceof DPJRegionDecl) &&
        		    !(l.head instanceof JRGRefGroupDecl))) {
        	align();
        	printStat(l.head);
        	println();
            }
        }
    }

    public void printInstrumentedPardoStats(List<? extends JCTree> trees) throws IOException {
	int count = 0;
	for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
            if ((!(l.head instanceof DPJRegionDecl) &&
        		    !(l.head instanceof JRGRefGroupDecl))) {
        	align();
        	printStat(l.head);
        	println();
        	if (++count < l.size()) {
        	    align();
        	    print("JRGRuntime.Instrument.pardoSeparator();");
        	    println();
        	}
            }
        }
    }

    /** Print a set of modifiers.
     */
    public void printFlags(long flags) throws IOException {
	if ((flags & SYNTHETIC) != 0) print("/*synthetic*/ ");
        print(TreeInfo.flagNames(flags));
        if ((flags & DPJStandardFlags) != 0) print(" ");
        if ((flags & ANNOTATION) != 0) print("@");
    }

    public void printAnnotations(List<JCAnnotation> trees) throws IOException {
        for (List<JCAnnotation> l = trees; l.nonEmpty(); l = l.tail) {
            printStat(l.head);
            println();
            align();
        }
    }

    /** Print documentation comment, if it exists
     *  @param tree    The tree for which a documentation comment should be printed.
     */
    public void printDocComment(JCTree tree) throws IOException {
        if (docComments != null) {
            String dc = docComments.get(tree);
            if (dc != null) {
                print("/**"); println();
                int pos = 0;
                int endpos = lineEndPos(dc, pos);
                while (pos < dc.length()) {
                    align();
                    print(" *");
                    if (pos < dc.length() && dc.charAt(pos) > ' ') print(" ");
                    print(dc.substring(pos, endpos)); println();
                    pos = endpos + 1;
                    endpos = lineEndPos(dc, pos);
                }
                align(); print(" */"); println();
                align();
            }
        }
    }
//where
    static int lineEndPos(String s, int start) {
        int pos = s.indexOf('\n', start);
        if (pos < 0) pos = s.length();
        return pos;
    }

    /** Print region parameter constraints
     */
    public void printConstraints(List<Pair<DPJRegionPathList,DPJRegionPathList>> constraints) 
    throws IOException {
        if (constraints.nonEmpty()) {
            print(" | ");
            int count = 0;
            for (Pair<DPJRegionPathList,DPJRegionPathList> pair : constraints) {
        	if (count++ > 0) print(", ");
        	print(pair.fst);
                print(" # ");
                print(pair.snd);        	
            }
        }
    }
    
    /** If parameter list is non-empty, print it enclosed in "<...>" brackets.
     */
    public void printTypeRegionEffectParams(List<JCTypeParameter> typeParams,
    	DPJParamInfo paramInfo) throws IOException {
        if (typeParams.nonEmpty()) {
            print("<");
            printExprs(typeParams);
            print(">");
        }
    }

    /** If there are type or region parameters, print both
     */
    public void printParams(List<JCTypeParameter> typarams, DPJParamInfo rgnparaminfo)
    	throws IOException {
	boolean printRegionParams = ((codeGenMode == NONE) && rgnparaminfo != null && 
		rgnparaminfo.rplParams.nonEmpty()); 
	if (typarams.nonEmpty() || printRegionParams) {
	    print("<");
	    printExprs(typarams);
	    if (printRegionParams) {
		if (typarams.nonEmpty()) print(", ");
		print("region ");
		printExprs(rgnparaminfo.rplParams);
		printConstraints(rgnparaminfo.rplConstraints);
	    }
	    print(">");
	}
    }
    /** Print a block.
     */
    public void printBlock(List<? extends JCTree> stats) throws IOException {
        print("{");
        println();
        indent();
        printStats(stats);
        undent();
        align();
        print("}");
    }

    /** Print a block.
     */
    public void printEnumBody(List<JCTree> stats) throws IOException {
        print("{");
        println();
        indent();
        boolean first = true;
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (isEnumerator(l.head)) {
                if (!first) {
                    print(",");
                    println();
                }
                align();
                printStat(l.head);
                first = false;
            }
        }
        print(";");
        println();
        inEnumBlock = true;
        for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (!isEnumerator(l.head)) {
        	align();
                printStat(l.head);
                println();
            }
        }
        inEnumBlock = false;
        undent();
        align();
        print("}");
    }

    /** Is the given tree an enumerator definition? */
    boolean isEnumerator(JCTree t) {
        return t.getTag() == JCTree.VARDEF && (((JCVariableDecl) t).mods.flags & ENUM) != 0;
    }

    /** Print unit consisting of package clause and import statements in toplevel,
     *  followed by class definition. if class definition == null,
     *  print all definitions in toplevel.
     *  @param tree     The toplevel tree
     *  @param cdef     The class definition, which is assumed to be part of the
     *                  toplevel tree.
     */
    public void printUnit(JCCompilationUnit tree, JCClassDecl cdef) throws IOException {
        docComments = tree.docComments;
        printDocComment(tree);
        if (tree.pid != null) {
            print("package ");
            printExpr(tree.pid);
            print(";");
            println();
        }
        boolean firstImport = true;
        for (List<JCTree> l = tree.defs;
        l.nonEmpty() && (cdef == null || l.head.getTag() == JCTree.IMPORT);
        l = l.tail) {
            if (l.head.getTag() == JCTree.IMPORT) {
                JCImport imp = (JCImport)l.head;
                Name name = TreeInfo.name(imp.qualid);
                if (name == name.table.asterisk ||
                        cdef == null ||
                        isUsed(TreeInfo.symbol(imp.qualid), cdef)) {
                    if (firstImport) {
                        firstImport = false;
                        println();
                    }
                    printStat(imp);
                }
            } else {
                printStat(l.head);
            }
        }
        if (cdef != null) {
            printStat(cdef);
            println();
        }
    }
    // where
    boolean isUsed(final Symbol t, JCTree cdef) {
        class UsedVisitor extends TreeScanner {
            public void scan(JCTree tree) {
                if (tree!=null && !result) tree.accept(this);
            }
            boolean result = false;
            public void visitIdent(JCIdent tree) {
                if (tree.sym == t) result = true;
            }
        }
        UsedVisitor v = new UsedVisitor();
        v.scan(cdef);
        return v.result;
    }

    /**************************************************************************
     * Visitor methods
     *************************************************************************/

    public void visitTopLevel(JCCompilationUnit tree) {
        try {
            printUnit(tree, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitImport(JCImport tree) {
        try {
            print("import ");
            if (tree.staticImport) print("static ");
            printExpr(tree.qualid);
            print(";");
            println();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitClassDef(JCClassDecl tree) {
        try {
            println(); align();
            printDocComment(tree);
            printAnnotations(tree.mods.annotations);
            printFlags(tree.mods.flags & ~INTERFACE);
            Name enclClassNamePrev = enclClassName;
            enclClassName = tree.name;
            if ((tree.mods.flags & INTERFACE) != 0) {
                print("interface " + tree.name);
                printParams(tree.typarams, tree.paramInfo);
                if (tree.implementing.nonEmpty()) {
                    print(" extends ");
                    printExprs(tree.implementing);
                }
            } else {
                if ((tree.mods.flags & ENUM) != 0)
                    print("enum " + tree.name);
                else
                    print("class " + tree.name);
                printParams(tree.typarams, tree.paramInfo);
                if (tree.extending != null) {
                    print(" extends ");
                    printExpr(tree.extending);
                }
                if (tree.implementing.nonEmpty()) {
                    print(" implements ");
                    printExprs(tree.implementing);
                }
            }
            print(" ");
            if ((tree.mods.flags & ENUM) != 0) {
                printEnumBody(tree.defs);
            } else {
                printBlock(tree.defs);
            }
            enclClassName = enclClassNamePrev;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitMethodDef(JCMethodDecl tree) {
        try {
            // when producing source output, omit anonymous constructors
            if (tree.name == tree.name.table.init &&
                    (enclClassName == null ||
                	    enclClassName.toString().equals("")) &&
                    sourceOutput) return;
            println(); align();
            printDocComment(tree);
            printExpr(tree.mods);
            printParams(tree.typarams, tree.paramInfo);
            if (tree.name == tree.name.table.init) {
        	print(enclClassName != null ? enclClassName : tree.name);
            } else {
                printExpr(tree.restype);
                print(" " + tree.name);
            }
            print("(");
            printExprs(tree.params);
            print(")");
            /*
            if (tree.effects != null && (codeGenMode == NONE)) {
        	if (tree.effects.isPure) {
        	    print(" pure");          
        	} else {        	    
        	    if (tree.effects.readEffectPerms.nonEmpty()) {
        		print(" reads ");
        		printExprs(tree.effects.readEffectPerms);
        	    }
        	    if (tree.effects.writeEffectPerms.nonEmpty()) {
        		print(" writes ");
        		printExprs(tree.effects.writeEffectPerms);
        	    }
        	}
            }
            */
            if (tree.thrown.nonEmpty()) {
                print(" throws ");
                printExprs(tree.thrown);
            }
            if (tree.body != null) {
                print(" ");
                printStat(tree.body);
            } else {
                print(";");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitVarDef(JCVariableDecl tree) {
        try {
            if (docComments != null && docComments.get(tree) != null) {
                println(); align();
            }
            printDocComment(tree);
            if ((tree.mods.flags & ENUM) != 0) {
        	//print("/*public static final*/ ");
                print(tree.name);
                if (tree.init != null) {
                    //print(" /* = ");
                    inEnumVarDecl = true;
                    printExpr(tree.init);
                    inEnumVarDecl = false;
                    //print(" */");
                }
            } else {
                printExpr(tree.mods);
                if ((tree.mods.flags & VARARGS) != 0) {
                    printExpr(((JCArrayTypeTree) tree.vartype).elemtype);
                    print("... " + tree.name);
                } else {
                    printExpr(tree.vartype);
                    print(" " + tree.name);
                }
                if ((codeGenMode == NONE) && tree.rpl != null && !tree.rpl.elts.isEmpty()) {
                    print(" in ");
                    print(tree.rpl);
                }
                if (tree.init != null) {
                    print(" = ");
                    printExpr(tree.init);
                }
                if (prec == TreeInfo.notExpression) print(";");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitRegionDecl(DPJRegionDecl tree) { 
        try {
            if (docComments != null && docComments.get(tree) != null) {
                println(); align();
            }
            printDocComment(tree);
            printExpr(tree.mods);
            print("region ");
            print(tree.name);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitRefGroupDecl(JRGRefGroupDecl tree) {
        try {
            if (docComments != null && docComments.get(tree) != null) {
                println(); align();
            }
            printDocComment(tree);
            print("refgroup ");
            print(tree.name);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }	
    }
    
    public void visitRPLElt(DPJRegionPathListElt tree) { 
	try {
	    switch (tree.type) {
	    case DPJRegionPathListElt.STAR:
		print("*");
		break;
	    case DPJRegionPathListElt.NAME:
		print(tree.exp);
		break;
	    default:
		print("Unknown RPL element");
	    	break;
	    }
	} catch (IOException e) {	    
	    throw new UncheckedIOException(e);
	}
    }	

    public void visitRPL(DPJRegionPathList tree) { 
	try {
	    int size = tree.elts.size();
	    int idx = 0;
	    for (Iterator<DPJRegionPathListElt> I = tree.elts.iterator();
        	I.hasNext(); )  {
		visitRPLElt(I.next());
		if (++idx < size) print(":");
	    }
	} catch (IOException e) {	    
	    throw new UncheckedIOException(e);
	}
    }	

    public void visitSkip(JCSkip tree) {
        try {
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitBlock(JCBlock tree) {
        try {
            printFlags(tree.flags);
            printBlock(tree.stats);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
        try {
            print("do ");
            printStat(tree.body);
            align();
            print(" while ");
            if (tree.cond.getTag() == JCTree.PARENS) {
                printExpr(tree.cond);
            } else {
                print("(");
                printExpr(tree.cond);
                print(")");
            }
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitWhileLoop(JCWhileLoop tree) {
        try {
            print("while ");
            if (tree.cond.getTag() == JCTree.PARENS) {
                printExpr(tree.cond);
            } else {
                print("(");
                printExpr(tree.cond);
                print(")");
            }
            print(" ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitForLoop(JCForLoop tree) {
        try {
            print("for (");
            if (tree.init.nonEmpty()) {
                if (tree.init.head.getTag() == JCTree.VARDEF) {
                    printExpr(tree.init.head);
                    for (List<JCStatement> l = tree.init.tail; l.nonEmpty(); l = l.tail) {
                        JCVariableDecl vdef = (JCVariableDecl)l.head;
                        print(", " + vdef.name + " = ");
                        printExpr(vdef.init);
                    }
                } else {
                    printExprs(tree.init);
                }
            }
            print("; ");
            if (tree.cond != null) printExpr(tree.cond);
            print("; ");
            printExprs(tree.step);
            print(") ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
        try {
            print("for (");
            printExpr(tree.var);
            print(" : ");
            printExpr(tree.expr);
            print(") ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void seqJRGForLoop(JRGForLoop tree) {
	try {
            if (codeGenMode == SEQ_INST) {
        	print("JRGRuntime.Instrument.enterForeach(");
        	print(tree.array + ".length");
        	print(");");
        	println();
        	align();
            }
            int depth = lmargin / width;
            print("for (");
            long flags = tree.indexVar.mods.flags;
            tree.indexVar.mods.flags &= ~Flags.FINAL;
            printExpr(tree.indexVar);
            tree.indexVar.mods.flags = flags;
            print(" = 0; ");
            print(tree.indexVar.name);
            print(" < ");
            printExpr(tree.array);
            print(".length; ");
            print(tree.indexVar.name);
            print("++");
            print(") ");
	    if (codeGenMode == SEQ_INST) {
		print("{");
		println();
		indent();
		align();
		print("JRGRuntime.Instrument.enterForeachIter();\n");
		align();
	    }
	    printStat(tree.body);
	    if(codeGenMode == SEQ_INST) {
		println();
		align();
		print("JRGRuntime.Instrument.exitForeachIter();\n");
		undent();
		align();
		print("}"); 
		println();
		align();
		print("JRGRuntime.Instrument.exitForeach();");
	    }
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    private void parJRGForLoop(JRGForLoop tree) {
	try {
	    println();
	    
	    // Define a class to run the loop iteration tasks
	    String taskName = mangle("Task", jrg_tname++);
	    printAligned("class " + taskName + " extends RecursiveAction {\n");
	    indent();
	    
	    // Define fields for start and length of iteration
	    printAligned("int __jrg_start;\n");
	    printAligned("int __jrg_length;\n");
	    Set<VarSymbol> copyIn = new HashSet(tree.usedVars);
	    copyIn.removeAll(tree.declaredVars);
	    Set<VarSymbol> copyOut = new HashSet(tree.definedVars);
	    copyOut.removeAll(tree.declaredVars);
	    // Check there are no assignments to local variables visible across iterations
	    if(copyOut.size() > 0) {
		// Ideally this error should be caught by the JRG type checker, prior to
		// Java code generation.  However, since the type checker doesn't do this yet, 
		// we just print out some text here that identifies the error and causes 
		// the Java compiler to choke.
		print("Error: Assignment inside foreach to local variable declared prior to foreach\n");
	    }
	    	    
	    // Define fields to hold local vars copied in/out
	    Set<VarSymbol> copyAll = new HashSet(copyIn);
	    copyAll.addAll(copyOut);
	    for(VarSymbol var : copyAll) {
		align();
		printCellType(var.type);
		print(" " + varString(var) + ";");
		println();
	    }
	    
	    // Generate constructor for class
	    printAligned(taskName);
	    print("(int __jrg_start, int __jrg_length");
	    for(VarSymbol var : copyIn) {
		print (", ");
		printCellType(var.type);
		print(" " + varString(var));
	    }
	    print(") {\n");
	    indent();
	    printAligned("this.__jrg_start = __jrg_start;");
	    println();
	    printAligned("this.__jrg_length = __jrg_length;");
	    println();
	    for(VarSymbol var : copyIn) {
		printAligned("this."+varString(var));
		print(" = ");
		print(varString(var));
		print(";");
		println();
	    }
	    undent();
	    align();
	    print("}\n");
	    
	    // Generate compute() method
	    printAligned("protected void compute() {\n");
	    indent();
	    printAligned("int __jrg_cutoff = DPJRuntime.RuntimeState.dpjForeachCutoff;\n");
	    printAligned("int __jrg_split = DPJRuntime.RuntimeState.dpjForeachSplit;\n");
	    printAligned("if (this.__jrg_length > __jrg_cutoff) {\n");
	    indent();
	    printAligned("RecursiveAction[] __jrg_tasks = ");
	    print("new RecursiveAction[__jrg_split];\n");
	    printAligned("int __jrg_block_size = __jrg_length / __jrg_split;\n");
	    printAligned("for(int i = 0; i < __jrg_split; i++) {\n");
	    indent();
	    printAligned("int __jrg_task_start = __jrg_start + __jrg_block_size * i;\n");
	    printAligned("int __jrg_task_length = (i >= __jrg_split - 1) ?\n");
	    printAligned("    __jrg_length - __jrg_block_size * i : __jrg_block_size;\n");
	    printAligned("__jrg_tasks[i] = new " + taskName + "(");
	    print("__jrg_task_start, __jrg_task_length");
	    for(VarSymbol var : copyIn) print(", "+varString(var));
	    print(");\n");
	    undent();
	    printAligned("}\n");
	    printAligned("RecursiveAction.forkJoin(__jrg_tasks);\n");
	    undent();
	    printAligned("}\n");
	    printAligned("else {\n");
	    indent();
	    long flags = tree.indexVar.mods.flags;
	    tree.indexVar.mods.flags &= ~Flags.FINAL;
	    printAligned("for("+tree.indexVar.toString()+" = __jrg_start; "+
		    tree.indexVar.sym.toString()+" < __jrg_start + __jrg_length; " +
		    tree.indexVar.sym.toString()+"++) ");
	    tree.indexVar.mods.flags = flags;
	    boolean savedNeedDPJThis = needDPJThis;
	    needDPJThis=true;
	    printStat(tree.body);
	    needDPJThis=savedNeedDPJThis;
	    println();
	    undent();
	    printAligned("}\n"); //end else
	    undent();
	    printAligned("}\n"); //end run
	    undent();
	    printAligned("};\n"); //close class block
	    
	    // Generate the invocation
	    printAligned("if(!DPJRuntime.RuntimeState.insideParallelTask) {\n");
	    indent();
	    printAligned("DPJRuntime.RuntimeState.insideParallelTask = true;\n");
	    printAligned("DPJRuntime.RuntimeState.pool.invoke(new " + taskName + "(0,");
	    printExpr(tree.array);
	    print(".length");
	    for(VarSymbol var : copyIn) {
		if(var.toString().equals("this") && !needDPJThis) print(", this");
		else print(", "+varString(var));
	    }
	    print("));\n");
	    printAligned("DPJRuntime.RuntimeState.insideParallelTask = false;\n");
	    undent();
	    printAligned("}\n");
	    printAligned("else {\n");
	    indent();
	    printAligned("(new " + taskName + "(0, ");
	    printExpr(tree.array);
	    print(".length");
	    for(VarSymbol var : copyIn) {
		if(var.toString().equals("this") && !needDPJThis) print(", this");
		else print(", "+varString(var));
	    }
	    print(")).forkJoin();\n");
	    undent();
	    printAligned("}\n");
	}
	catch(IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    private String varString(VarSymbol maybeThis) {
	if(maybeThis.toString().equals("this"))
	    return "__jrg_this";
	else
	    return maybeThis.toString();
    }
    
    public void visitJRGForLoop(JRGForLoop tree) {
	if (codeGenMode == NONE) {
	    try {
		print("for each ");
		print(tree.indexVar.name);
		print(" in ");
		printExpr(tree.array);
		print(" ");
		if (tree.isParallel) print("pardo ");
		printStat(tree.body);
	    }
	    catch(IOException e) {
		throw new UncheckedIOException(e);
	    }
	} else if(sequential || !tree.isParallel) {
	    seqJRGForLoop(tree);
	} else {
	    parJRGForLoop(tree);
	}
    }
    
    public void visitLabelled(JCLabeledStatement tree) {
        try {
            print(tree.label + ": ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitSwitch(JCSwitch tree) {
        try {
            if (codeGenMode != NONE && tree.isInstanceofSwitch) {
        	printInstanceofSwitch(tree);
        	return;
            }
            print("switch ");
            if (tree.selector.getTag() == JCTree.PARENS) {
                printExpr(tree.selector);
            } else {
                print("(");
                printExpr(tree.selector);
                print(")");
            }
            if (tree.isInstanceofSwitch) {
        	print(" instanceof ");
            }
            print(" {");
            println();
            printStats(tree.cases);
            align();
            print("}");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public void printInstanceofSwitch(JCSwitch tree) throws IOException {
	JCIdent selector = (JCIdent) tree.selector;
	int caseNum = 1;
	for (JCCase c : tree.cases) {
	    if (caseNum > 1) {
		println(); align();
		print("else ");
	    }
	    print("if (" + selector.name + " instanceof ");
	    if (c.pat == null) {
		print("Object");
	    }
	    else print(c.pat.type);
	    print(") {");
	    println();
	    indent(); align();
	    variableToMangle = selector.sym;
	    if (c.pat != null) {
		print(c.pat.type + " " + mangle(selector.name) + 
			" = (" + c.pat.type + ") " + selector.name + ";");
		println();
	    }
	    printStats(c.stats);
	    variableToMangle = null;
	    undent(); align();
	    print("}");
	    ++caseNum;
	    ++suffix;
	}
    }

    public void visitCase(JCCase tree) {
        try {
            if (tree.pat == null) {
                print("default");
            } else {
                print("case ");
                printExpr(tree.pat);
            }
            print(": ");
            println();
            indent();
            printStats(tree.stats);
            undent();
            align();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitSynchronized(JCSynchronized tree) {
        try {
            print("synchronized ");
            if (tree.lock.getTag() == JCTree.PARENS) {
                printExpr(tree.lock);
            } else {
                print("(");
                printExpr(tree.lock);
                print(")");
            }
            print(" ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTry(JCTry tree) {
        try {
            print("try ");
            printStat(tree.body);
            for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
                printStat(l.head);
            }
            if (tree.finalizer != null) {
                print(" finally ");
                printStat(tree.finalizer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitCatch(JCCatch tree) {
        try {
            print(" catch (");
            printExpr(tree.param);
            print(") ");
            printStat(tree.body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitConditional(JCConditional tree) {
        try {
            open(prec, TreeInfo.condPrec);
            printExpr(tree.cond, TreeInfo.condPrec);
            print(" ? ");
            printExpr(tree.truepart, TreeInfo.condPrec);
            print(" : ");
            printExpr(tree.falsepart, TreeInfo.condPrec);
            close(prec, TreeInfo.condPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitIf(JCIf tree) {
        try {
            print("if ");
            if (tree.cond.getTag() == JCTree.PARENS) {
                printExpr(tree.cond);
            } else {
                print("(");
                printExpr(tree.cond);
                print(")");
            }
            print(" ");
            printStat(tree.thenpart);
            if (tree.elsepart != null) {
                print(" else ");
                printStat(tree.elsepart);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitExec(JCExpressionStatement tree) {
	// Get rid of bogus super() invocation in enum constructor
	if (inEnumBlock && tree.expr instanceof JCMethodInvocation) {
	    JCMethodInvocation mi = (JCMethodInvocation) tree.expr;
	    if (mi.meth instanceof JCIdent) {
		JCIdent id = (JCIdent) mi.meth;
		if (id.name.toString().equals("super"))
		    return;
	    }
	}		
	try {
            printExpr(tree.expr);
            if (prec == TreeInfo.notExpression) print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitBreak(JCBreak tree) {
        try {
            print("break");
            if (tree.label != null) print(" " + tree.label);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitNegationExpression(DPJNegationExpression tree) {
	try {
	    print("~ ");
	    print(tree.negatedExpr);
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	}
    }
    
    public void visitContinue(JCContinue tree) {
        try {
            print("continue");
            if (tree.label != null) print(" " + tree.label);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitReturn(JCReturn tree) {
        try {
            print("return");
            if (tree.expr != null) {
                print(" ");
                printExpr(tree.expr);
            }
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitThrow(JCThrow tree) {
        try {
            print("throw ");
            printExpr(tree.expr);
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitAssert(JCAssert tree) {
        try {
            print("assert ");
            printExpr(tree.cond);
            if (tree.detail != null) {
                print(" : ");
                printExpr(tree.detail);
            }
            print(";");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void printArgs(List<JCExpression> typeargs, List<DPJRegionPathList> rplargs,
	    boolean printKeyword) throws IOException {
        boolean rplsToPrint = ((codeGenMode == NONE) && rplargs.nonEmpty());
        if (!typeargs.isEmpty() || rplsToPrint) {
            print("<");
            printExprs(typeargs);
            if (typeargs.nonEmpty() && rplsToPrint)
        	print(", ");
            if (rplsToPrint && printKeyword) print("region ");
            printExprs(rplargs);
            print(">");
        }
    }
    
    public void visitApply(JCMethodInvocation tree) {
	try {
            if (!tree.typeargs.isEmpty() ||
        	    !tree.regionArgs.isEmpty()) {
                if (tree.meth.getTag() == JCTree.SELECT) {
                    JCFieldAccess left = (JCFieldAccess)tree.meth;
                    printExpr(left.selected);
                    print(".");
                    printArgs(tree.typeargs, tree.regionArgs, true);
                    print(left.name);
                } else {
                    printArgs(tree.typeargs, tree.regionArgs, true);
                    printExpr(tree.meth);
                }
            } else {
                printExpr(tree.meth);
            }
            print("(");
            printExprs(tree.args);
            print(")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printArrayConstructor(Type cellType, List<JCExpression> args) 
	    throws IOException {
	if (cellType instanceof ClassType) {
	    ClassType ct = (ClassType) cellType;
	    if (ct.cellType != null) {
		printArrayConstructor(ct.cellType, args);
		print("[]");
		return;
	    }
	} 
	print("new "+ cellType + "[");
	printExprs(args);
	print("]");
    }
    
    public void visitNewClass(JCNewClass tree) {
        try {
            if (tree.encl != null) {
                printExpr(tree.encl);
                print(".");
            }
            if ((tree.constructor.flags() & ARRAYCONSTR) != 0) {
        	// Convert array constructor call to regular Java array
        	ClassType ct = (ClassType) tree.clazz.type;
        	printArrayConstructor(ct.cellType, tree.args);
        	return;
            }
            if (!inEnumVarDecl) {
        	print("new ");
        	boolean rplsToPrint = ((codeGenMode == NONE) &&
        		tree.regionArgs.nonEmpty());
        	if (rplsToPrint || !tree.typeargs.isEmpty()) {
        	    print("<");
        	    printExprs(tree.typeargs);
        	    if (rplsToPrint) {
        		if (tree.typeargs.nonEmpty()) print(", ");
        		printExprs(tree.regionArgs);
        	    }
        	    print(">");
        	}
        	printExpr(tree.clazz);
            }
            print("(");
            printExprs(tree.args);
            print(")");
            if (tree.def != null) {
                Name enclClassNamePrev = enclClassName;
                enclClassName =
                        tree.def.name != null ? tree.def.name :
                            tree.type != null && tree.type.tsym.name != tree.type.tsym.name.table.empty ? tree.type.tsym.name :
                                null;
                //if ((tree.def.mods.flags & Flags.ENUM) != 0) print("/*enum*/");
                printBlock(tree.def.defs);
                enclClassName = enclClassNamePrev;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitNewArray(JCNewArray tree) {
        try {
            if (tree.elemtype != null) {
                print("new ");
                JCTree elem = tree.elemtype;
                if (elem instanceof JCArrayTypeTree)
                    printBaseElementType((JCArrayTypeTree) elem);
                else
                    printExpr(elem);
                for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
                    print("[");
                    printExpr(l.head);
                    print("]");
                }
                if (elem instanceof JCArrayTypeTree)
                    printBrackets((JCArrayTypeTree) elem);
            }
            if (tree.elems != null) {
                if (tree.elemtype != null) print("[]");
                print("{");
                printExprs(tree.elems);
                print("}");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitParens(JCParens tree) {
        try {
            print("(");
            printExpr(tree.expr);
            print(")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private int jrg_tname = 0;    
    public void visitPardo(JRGPardo tree) {
	if (codeGenMode == NONE) {
	    try {
		print("pardo ");
		printStat(tree.body);
	    } catch (IOException e) {
		throw new UncheckedIOException(e);
	    }
	} 
	else if (codeGenMode == SEQ_INST) {
	    printInstrumentedPardoSeq(tree);
	}
	else if (sequential){
	    visitBlock(tree.body);
	} 
	else {
	    printPardoPar(tree);
	}	
    }
    
    public void printInstrumentedPardoSeq(JRGPardo tree) {
	try {
	    print("JRGRuntime.Instrument.enterPardo();\n");
	    println();
	    printFlags(tree.body.flags);
	    printBlock(tree.body.stats);
	    print("{");
	    println();
	    indent();
	    printInstrumentedPardoStats(tree.body.stats);
	    undent();
	    align();
	    print("}");
	    println();
	    print("JRGRuntime.Instrument.exitPardo();");
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    public void printPardoPar(JRGPardo tree) {
	try {
	    List<String> toCoInvoke = List.<String>nil();
	    List<String> copyOutAssign = List.<String>nil();
	    String arr = "__jrg_s"+jrg_tname++;
	    int orig_jrg_tname = jrg_tname;
	    println();
	    int i=0;
	    for(JCStatement statement : tree.body.stats)
	    {
		align();
		String stName = "__jrg_S"+jrg_tname++;
		print("class " + stName + " extends RecursiveAction {\n");
		indent();
		Set<VarSymbol> copyIn = new HashSet(tree.usedVars[i]);
		copyIn.removeAll(tree.declaredVars[i]);
		Set<VarSymbol> copyOut = new HashSet(tree.definedVars[i]);
		copyOut.removeAll(tree.declaredVars[i]);
		// TODO:  Don't put 'this' in in the first place!
		for (VarSymbol vs : copyIn) {
		     if (vs.name.toString().equals("this")) {
			 copyIn.remove(vs);
			 copyOut.remove(vs);
			 break;
		     }
		}
		Set<VarSymbol> copyAll = new HashSet(copyIn);
		copyAll.addAll(copyOut);

		
		//Declare local vars necessary for copyin/copyout
		for(VarSymbol var : copyAll)
		{
		    align();
		    print(var.type.toString()+" "+var.toString()+";\n");
		}
		
		//Generate constructor for class
		align();
		print(stName+"(");
		boolean needsComma = false;
		for(VarSymbol var : copyIn) {
		    if (needsComma)
			print(",");
		    else
			needsComma=true;
		    print(var.type.toString()+" "+var.toString());
		}
		print(") {\n");
		indent();
		for(VarSymbol var : copyIn)
		{
		    align();
		    print("this."+var.toString()+"="+var.toString()+";\n");
		}
		undent();
		align();
		print("}\n");
		
		//Generate run method
		align();
		print("protected void compute() {\n");
		indent();
		align();
		printOwner = true;
		printStat(statement);
		printOwner = false;
		println();
		undent();
		align();
		print("}\n");
		
		//close class block
		undent();
		align();
		print("};\n");
		
		//generate code for FJTask object, for use later
		String varList="";
		needsComma=false;
		for(VarSymbol var : copyIn)
		{
		    if (needsComma)
			varList+=",";
		    else
			needsComma=true;
		    varList+=var.toString();
		}
		toCoInvoke = toCoInvoke.append("new "+stName+"("+varList+")");
		
		//generate code for copy out assignment, for use later
		for(VarSymbol var : copyOut)
		{
		    String stmt=var.toString()+" = (("+stName+")("+arr+
		    	"["+(jrg_tname - 1 - orig_jrg_tname)+"]))."+
		    	var.toString()+";\n";
		    copyOutAssign = copyOutAssign.append(stmt);
		}
		
		i++;
	    }
	    
	    //Okay, now generate the actual array and coInvoke call
	    align();
	    print("RecursiveAction[] "+arr+" = {");
	    boolean needsComma=false;
	    for(String toPrint : toCoInvoke)
	    {
		if (needsComma)
		    print(",");
		else
		    needsComma=true;
		print(toPrint);
	    }
	    print("};\n");
	    align();
	    int pardo_wrapper = jrg_tname++;
	    print("class __jrg_S"+pardo_wrapper+" extends RecursiveAction {\n");
	    indent();
	    align();
	    print("RecursiveAction[] __jrg_toforkjoin;\n");
	    align();
	    print("__jrg_S"+pardo_wrapper+"(RecursiveAction[] __jrg_toforkjoin_) {\n");
	    indent();
	    align();
	    print("__jrg_toforkjoin = __jrg_toforkjoin_;\n");
	    undent();
	    align();
	    print("}\n");
	    align();
	    print("protected void compute() {\n");
	    indent();
	    align();
	    print("RecursiveAction.forkJoin(__jrg_toforkjoin);\n");
	    undent();
	    align();
	    print("}\n"); //end compute
	    undent();
	    align();
	    print("};\n"); //end pardo_wrapper class
	    align();
	    print("if(!DPJRuntime.RuntimeState.insideParallelTask) {\n");
	    indent();
	    align();
            print("DPJRuntime.RuntimeState.insideParallelTask = true;\n");
	    align();
	    print("DPJRuntime.RuntimeState.pool.invoke(new __jrg_S"+pardo_wrapper+"("+arr+"));\n");
	    undent();
	    align();
	    print("DPJRuntime.RuntimeState.insideParallelTask = false;\n");
	    align();
            print("}\n");
	    align();
	    print("else\n");
	    indent();
	    align();
	    print("RecursiveAction.forkJoin("+arr+");\n");
	    undent();
	    
	    //Generate copy out assignments
	    for(String assign : copyOutAssign)
	    {
		align();
		print(assign);
	    }
	}
	catch(IOException e) {
	    throw new UncheckedIOException(e);
	}
    }
	
    public void visitAssign(JCAssign tree) {
        try {
            open(prec, TreeInfo.assignPrec);
            printExpr(tree.lhs, TreeInfo.assignPrec + 1);
            print(" = ");
            printExpr(tree.rhs, TreeInfo.assignPrec);
            close(prec, TreeInfo.assignPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String operatorName(int tag) {
        switch(tag) {
            case JCTree.POS:     return "+";
            case JCTree.NEG:     return "-";
            case JCTree.NOT:     return "!";
            case JCTree.COMPL:   return "~";
            case JCTree.PREINC:  return "++";
            case JCTree.PREDEC:  return "--";
            case JCTree.POSTINC: return "++";
            case JCTree.POSTDEC: return "--";
            case JCTree.NULLCHK: return "<*nullchk*>";
            case JCTree.OR:      return "||";
            case JCTree.AND:     return "&&";
            case JCTree.EQ:      return "==";
            case JCTree.NE:      return "!=";
            case JCTree.LT:      return "<";
            case JCTree.GT:      return ">";
            case JCTree.LE:      return "<=";
            case JCTree.GE:      return ">=";
            case JCTree.BITOR:   return "|";
            case JCTree.BITXOR:  return "^";
            case JCTree.BITAND:  return "&";
            case JCTree.SL:      return "<<";
            case JCTree.SR:      return ">>";
            case JCTree.USR:     return ">>>";
            case JCTree.PLUS:    return "+";
            case JCTree.MINUS:   return "-";
            case JCTree.MUL:     return "*";
            case JCTree.DIV:     return "/";
            case JCTree.MOD:     return "%";
            default: throw new Error();
        }
    }

    public void visitAssignop(JCAssignOp tree) {
        try {
            open(prec, TreeInfo.assignopPrec);
            printExpr(tree.lhs, TreeInfo.assignopPrec + 1);
            print(" " + operatorName(tree.getTag() - JCTree.ASGOffset) + "= ");
            printExpr(tree.rhs, TreeInfo.assignopPrec);
            close(prec, TreeInfo.assignopPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitUnary(JCUnary tree) {
        try {
            if (tree.isDestructiveAccess) {
        	JCFieldAccess fa = (JCFieldAccess) tree.arg;
        	print("(new Object() {");
        	println();
        	indent();
        	align(); 
        	printCellType(fa.type);
        	print(" destructiveAccess(");
        	print(fa.selected.type);
        	print(" arg) {");
        	println();
        	indent();
        	align();
        	printCellType(fa.type);
        	print(" result = arg." + fa.name + ";");
        	println();
        	align(); print("arg." + fa.name + " = null;");
        	println();
        	align(); print("return result;");
        	println();
        	undent();
        	align(); print("}");
        	println();
        	undent(); 
        	align(); print("}).destructiveAccess(" + fa.selected + ")");
        	return;
            }
            int ownprec = TreeInfo.opPrec(tree.getTag());
            String opname = operatorName(tree.getTag());
            open(prec, ownprec);
            if (tree.getTag() <= JCTree.PREDEC) {
                print(opname);
                printExpr(tree.arg, ownprec);
            } else {
                printExpr(tree.arg, ownprec);
                print(opname);
            }
            close(prec, ownprec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitBinary(JCBinary tree) {
        try {
            int ownprec = TreeInfo.opPrec(tree.getTag());
            String opname = operatorName(tree.getTag());
            open(prec, ownprec);
            printExpr(tree.lhs, ownprec);
            print(" " + opname + " ");
            printExpr(tree.rhs, ownprec + 1);
            close(prec, ownprec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeCast(JCTypeCast tree) {
        try {
            open(prec, TreeInfo.prefixPrec);
            print("(");
            printExpr(tree.clazz);
            print(")");
            printExpr(tree.expr, TreeInfo.prefixPrec);
            close(prec, TreeInfo.prefixPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeTest(JCInstanceOf tree) {
        try {
            open(prec, TreeInfo.ordPrec);
            printExpr(tree.expr, TreeInfo.ordPrec);
            print(" instanceof ");
            printExpr(tree.clazz, TreeInfo.ordPrec + 1);
            close(prec, TreeInfo.ordPrec);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitIndexed(JCArrayAccess tree) {
        try {
            printExpr(tree.indexed, TreeInfo.postfixPrec);
            print("[");
            printExpr(tree.index);
            print("]");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitSelect(JCFieldAccess tree) {
        try {
            // Hack to work around generation of types starting with "." during barrier insertion
            // TODO Fix the root cause of this (in TreeMaker, I think).
            if (tree.selected instanceof JCIdent && 
        	    ((JCIdent)(tree.selected)).name.length() == 0) {
        	print(tree.name);
            } else {
        	printExpr(tree.selected, TreeInfo.postfixPrec);
        	print("." + tree.name);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean printOwner = false;

    private void printCellType(Type cellType) throws IOException {
	if (cellType instanceof ClassType) {
	    ClassType ct = (ClassType) cellType;
	    if (ct.cellType != null) {
		printCellType(ct.cellType);
		print("[]");
		return;
	    }
	}
	print(cellType);
    }
    
    public void visitIdent(JCIdent tree) {
        try {
            if (needDPJThis && tree.toString().equals("this"))
        	print("__jrg_this");
            else if (printOwner && tree.toString().equals("this")) {
        	print (tree.sym.owner.type + "." + "this");
            } 
            else if (tree.sym instanceof ClassSymbol &&
        	    tree.sym.type instanceof ClassType) {
        	ClassType ct = (ClassType) tree.sym.type;
        	if (ct.cellType != null) {
        	    // Convert array class to normal Java array
        	    printCellType(ct.cellType);
        	    print("[]");
        	} else {
        	    print(tree.name);
        	}
            }
            else {
        	if (tree.sym == variableToMangle) {
        	    print(mangle(tree.name));
        	}
        	else {
        	    print(tree.name);
        	}
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitLiteral(JCLiteral tree) {
        try {
            switch (tree.typetag) {
                case TypeTags.INT:
                    print(tree.value.toString());
                    break;
                case TypeTags.LONG:
                    print(tree.value + "L");
                    break;
                case TypeTags.FLOAT:
                    print(tree.value + "F");
                    break;
                case TypeTags.DOUBLE:
                    print(tree.value.toString());
                    break;
                case TypeTags.CHAR:
                    print("\'" +
                            Convert.quote(
                            String.valueOf((char)((Number)tree.value).intValue())) +
                            "\'");
                    break;
		case TypeTags.BOOLEAN:
		    print(((Number)tree.value).intValue() == 1 ? "true" : "false");
		    break;
		case TypeTags.BOT:
		    print("null");
		    break;
                default:
                    print("\"" + Convert.quote(tree.value.toString()) + "\"");
                    break;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
        try {
            switch(tree.typetag) {
                case TypeTags.BYTE:
                    print("byte");
                    break;
                case TypeTags.CHAR:
                    print("char");
                    break;
                case TypeTags.SHORT:
                    print("short");
                    break;
                case TypeTags.INT:
                    print("int");
                    break;
                case TypeTags.LONG:
                    print("long");
                    break;
                case TypeTags.FLOAT:
                    print("float");
                    break;
                case TypeTags.DOUBLE:
                    print("double");
                    break;
                case TypeTags.BOOLEAN:
                    print("boolean");
                    break;
                case TypeTags.VOID:
                    print("void");
                    break;
                default:
                    print("error");
                    break;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTypeArray(JCArrayTypeTree tree) {
        try {
            printBaseElementType(tree);
            printBrackets(tree);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Prints the inner element type of a nested array
    private void printBaseElementType(JCArrayTypeTree tree) throws IOException {
        JCTree elem = tree.elemtype;
        while (elem instanceof JCWildcard)
            elem = ((JCWildcard) elem).inner;
        if (elem instanceof JCArrayTypeTree)
            printBaseElementType((JCArrayTypeTree) elem);
        else
            printExpr(elem);
    }

    // prints the brackets of a nested array in reverse order
    private void printBrackets(JCArrayTypeTree tree) throws IOException {
        JCTree elem;
        while (true) {
            elem = tree.elemtype;
            print("[]");
            if ((codeGenMode == NONE) && tree.rpl != null) {
        	print("<");
        	printExpr(tree.rpl);
        	print(">");
        	if (tree.indexParam != null) {
        	    print("#");
        	    printExpr(tree.indexParam);
        	}
            }
            if (!(elem instanceof JCArrayTypeTree)) break;
            tree = (JCArrayTypeTree) elem;
        }
    }

    public void visitTypeApply(JCTypeApply tree) {
	try {
            printExpr(tree.functor);
            if (tree.functor.getSymbol() instanceof ClassSymbol &&
        	    tree.functor.type instanceof ClassType) {
        	ClassType ct = (ClassType) tree.functor.type;
        	if (ct.cellType != null) {
        	    return;
        	}
            }
            boolean rplsToPrint = (codeGenMode == NONE) && tree.rplArgs != null &&
            	tree.rplArgs.nonEmpty();
            boolean effectsToPrint = (codeGenMode == NONE) && tree.groupArgs != null &&
            	tree.groupArgs.nonEmpty();
            if (tree.typeArgs.nonEmpty() || rplsToPrint || effectsToPrint) {
        	print("<");
        	printExprs(tree.typeArgs);
        	if (rplsToPrint) {
        	    if (tree.typeArgs.nonEmpty()) {
        		print(", ");
        	    }
        	    printExprs(tree.rplArgs);
        	}
        	if (effectsToPrint) {
        	    if (tree.typeArgs.nonEmpty() || rplsToPrint) {
        		print(", ");
        	    }
        	    // printExprs(tree.effectArgs);
        	    // TODO:  WHY DOES THIS CAUSE STACK OVERFLOW???
        	}
        	print(">");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public void visitTypeParameter(JCTypeParameter tree) {
        try {
            print(tree.name);
            if (tree.bounds.nonEmpty()) {
                print(" extends ");
                printExprs(tree.bounds, " & ");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitRegionParameter(DPJRegionParameter tree) {
	try {
	    if (tree.isAtomic)
		print("atomic ");
	    print(tree.name);
	    if (tree.bound != null) {
		print(" under ");
		printExpr(tree.bound);
	    }
	} catch (IOException e) {
            throw new UncheckedIOException(e);
        }	
    }
    
    @Override
    public void visitWildcard(JCWildcard tree) {
        try {
            print(tree.kind);
            if (tree.kind.kind != BoundKind.UNBOUND)
                printExpr(tree.inner);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitTypeBoundKind(TypeBoundKind tree) {
        try {
            print(String.valueOf(tree.kind));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitErroneous(JCErroneous tree) {
        try {
            print("(ERROR)");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitLetExpr(LetExpr tree) {
        try {
            print("(let " + tree.defs + " in " + tree.expr + ")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitModifiers(JCModifiers mods) {
        try {
            printAnnotations(mods.annotations);
            if (codeGenMode == NONE)
        	printFlags(mods.flags);
            else 
        	printFlags(mods.flags & ~Flags.ISCOMMUTATIVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitAnnotation(JCAnnotation tree) {
        try {
            print("@");
            printExpr(tree.annotationType);
            print("(");
            printExprs(tree.args);
            print(")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void visitTree(JCTree tree) {
        try {
            print("(UNKNOWN: " + tree + ")");
            println();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
