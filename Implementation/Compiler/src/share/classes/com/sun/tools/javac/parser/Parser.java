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

package com.sun.tools.javac.parser;

import static com.sun.tools.javac.parser.Token.AMP;
import static com.sun.tools.javac.parser.Token.ARRAYCLASS;
import static com.sun.tools.javac.parser.Token.ASSERT;
import static com.sun.tools.javac.parser.Token.BAR;
import static com.sun.tools.javac.parser.Token.CASE;
import static com.sun.tools.javac.parser.Token.CATCH;
import static com.sun.tools.javac.parser.Token.CLASS;
import static com.sun.tools.javac.parser.Token.COLON;
import static com.sun.tools.javac.parser.Token.COMMA;
import static com.sun.tools.javac.parser.Token.COPIES;
import static com.sun.tools.javac.parser.Token.DEFAULT;
import static com.sun.tools.javac.parser.Token.DOT;
import static com.sun.tools.javac.parser.Token.ELLIPSIS;
import static com.sun.tools.javac.parser.Token.ELSE;
import static com.sun.tools.javac.parser.Token.ENUM;
import static com.sun.tools.javac.parser.Token.EOF;
import static com.sun.tools.javac.parser.Token.EQ;
import static com.sun.tools.javac.parser.Token.ERROR;
import static com.sun.tools.javac.parser.Token.EXTENDS;
import static com.sun.tools.javac.parser.Token.FINAL;
import static com.sun.tools.javac.parser.Token.FINALLY;
import static com.sun.tools.javac.parser.Token.FRESH;
import static com.sun.tools.javac.parser.Token.GT;
import static com.sun.tools.javac.parser.Token.GTEQ;
import static com.sun.tools.javac.parser.Token.GTGT;
import static com.sun.tools.javac.parser.Token.GTGTEQ;
import static com.sun.tools.javac.parser.Token.GTGTGTEQ;
import static com.sun.tools.javac.parser.Token.IDENTIFIER;
import static com.sun.tools.javac.parser.Token.IMPLEMENTS;
import static com.sun.tools.javac.parser.Token.IMPORT;
import static com.sun.tools.javac.parser.Token.INSTANCEOF;
import static com.sun.tools.javac.parser.Token.INT;
import static com.sun.tools.javac.parser.Token.INTERFACE;
import static com.sun.tools.javac.parser.Token.INTLITERAL;
import static com.sun.tools.javac.parser.Token.LBRACE;
import static com.sun.tools.javac.parser.Token.LBRACKET;
import static com.sun.tools.javac.parser.Token.LONGLITERAL;
import static com.sun.tools.javac.parser.Token.LPAREN;
import static com.sun.tools.javac.parser.Token.LT;
import static com.sun.tools.javac.parser.Token.LTLT;
import static com.sun.tools.javac.parser.Token.MONKEYS_AT;
import static com.sun.tools.javac.parser.Token.NEW;
import static com.sun.tools.javac.parser.Token.NUMBER;
import static com.sun.tools.javac.parser.Token.PACKAGE;
import static com.sun.tools.javac.parser.Token.PARDO;
import static com.sun.tools.javac.parser.Token.PLUSEQ;
import static com.sun.tools.javac.parser.Token.PLUSPLUS;
import static com.sun.tools.javac.parser.Token.PRESERVES;
import static com.sun.tools.javac.parser.Token.QUES;
import static com.sun.tools.javac.parser.Token.RBRACE;
import static com.sun.tools.javac.parser.Token.RBRACKET;
import static com.sun.tools.javac.parser.Token.READS;
import static com.sun.tools.javac.parser.Token.REFGROUP;
import static com.sun.tools.javac.parser.Token.REGION;
import static com.sun.tools.javac.parser.Token.RPAREN;
import static com.sun.tools.javac.parser.Token.SEMI;
import static com.sun.tools.javac.parser.Token.STAR;
import static com.sun.tools.javac.parser.Token.STATIC;
import static com.sun.tools.javac.parser.Token.SUB;
import static com.sun.tools.javac.parser.Token.SUBSUB;
import static com.sun.tools.javac.parser.Token.SUPER;
import static com.sun.tools.javac.parser.Token.THROWS;
import static com.sun.tools.javac.parser.Token.TRUE;
import static com.sun.tools.javac.parser.Token.UNIQUE;
import static com.sun.tools.javac.parser.Token.UPDATES;
import static com.sun.tools.javac.parser.Token.VOID;
import static com.sun.tools.javac.parser.Token.WHILE;
import static com.sun.tools.javac.parser.Token.WRITES;
import static com.sun.tools.javac.util.ListBuffer.lb;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JRGParamInfo;
import com.sun.tools.javac.tree.JCTree.DPJRegionDecl;
import com.sun.tools.javac.tree.JCTree.DPJRegionParameter;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathList;
import com.sun.tools.javac.tree.JCTree.DPJRegionPathListElt;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JRGCopyPerm;
import com.sun.tools.javac.tree.JCTree.JRGDerefSet;
import com.sun.tools.javac.tree.JCTree.JRGEffectPerm;
import com.sun.tools.javac.tree.JCTree.JRGForLoop;
import com.sun.tools.javac.tree.JCTree.JRGMethodPerms;
import com.sun.tools.javac.tree.JCTree.JRGRefPerm;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Pair;
import com.sun.tools.javac.util.Position;

/** The parser maps a token sequence into an abstract syntax
 *  tree. It operates by recursive descent, with code derived
 *  systematically from an LL(1) grammar. For efficiency reasons, an
 *  operator precedence scheme is used for parsing binary operation
 *  expressions.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class Parser {

    public static int methodCount;
    
    /** A factory for creating parsers. */
    public static class Factory {
        /** The context key for the parser factory. */
        protected static final Context.Key<Parser.Factory> parserFactoryKey =
            new Context.Key<Parser.Factory>();

        /** Get the Factory instance for this context. */
        public static Factory instance(Context context) {
            Factory instance = context.get(parserFactoryKey);
            if (instance == null)
                instance = new Factory(context);
            return instance;
        }

        final TreeMaker F;
        final Log log;
        final Keywords keywords;
        final Source source;
        final Name.Table names;
        final Options options;

        /** Create a new parser factory. */
        protected Factory(Context context) {
            context.put(parserFactoryKey, this);
            this.F = TreeMaker.instance(context);
            this.log = Log.instance(context);
            this.names = Name.Table.instance(context);
            this.keywords = Keywords.instance(context);
            this.source = Source.instance(context);
            this.options = Options.instance(context);
        }

        /**
         * Create a new Parser.
         * @param S Lexer for getting tokens while parsing
         * @param keepDocComments true if javadoc comments should be kept
         * @param genEndPos true if end positions should be generated
         */
        public Parser newParser(Lexer S, boolean keepDocComments, boolean genEndPos) {
            if (!genEndPos)
                return new Parser(this, S, keepDocComments);
            else
                return new EndPosParser(this, S, keepDocComments);
        }
    }

    /** The number of precedence levels of infix operators.
     */
    private static final int infixPrecedenceLevels = 10;

    /** The scanner used for lexical analysis.
     */
    private Lexer S;

    /** The factory to be used for abstract syntax tree construction.
     */
    protected TreeMaker F;

    /** The log to be used for error diagnostics.
     */
    private Log log;

    /** The keyword table. */
    private Keywords keywords;

    /** The Source language setting. */
    private Source source;

    /** The name table. */
    private Name.Table names;

    /** Construct a parser from a given scanner, tree factory and log.
     */
    protected Parser(Factory fac,
                     Lexer S,
                     boolean keepDocComments) {
        this.S = S;
        S.nextToken(); // prime the pump
        this.F = fac.F;
        this.log = fac.log;
        this.names = fac.names;
        this.keywords = fac.keywords;
        this.source = fac.source;
        Options options = fac.options;
        this.allowGenerics = source.allowGenerics();
        this.allowVarargs = source.allowVarargs();
        this.allowAsserts = source.allowAsserts();
        this.allowEnums = source.allowEnums();
        this.allowForeach = source.allowForeach();
        this.allowStaticImport = source.allowStaticImport();
        this.allowAnnotations = source.allowAnnotations();
        this.keepDocComments = keepDocComments;
        if (keepDocComments) docComments = new HashMap<JCTree,String>();
        this.errorTree = F.Erroneous();
    }

    /** Switch: Should generics be recognized?
     */
    boolean allowGenerics;

    /** Switch: Should varargs be recognized?
     */
    boolean allowVarargs;

    /** Switch: should we recognize assert statements, or just give a warning?
     */
    boolean allowAsserts;

    /** Switch: should we recognize enums, or just give a warning?
     */
    boolean allowEnums;

    /** Switch: should we recognize foreach?
     */
    boolean allowForeach;

    /** Switch: should we allow static imports?
     */
    boolean allowStaticImport;

    /** Switch: should we recognize annotations?
     */
    boolean allowAnnotations;

    /** Switch: should we keep docComments?
     */
    boolean keepDocComments;

    /** When terms are parsed, the mode determines which is expected:
     *     mode = EXPR        : an expression
     *     mode = TYPE        : a type
     *     mode = NOPARAMS    : no parameters allowed for type
     *     mode = TYPEARG     : type argument
     */
    static final int EXPR = 1;
    static final int TYPE = 2;
    static final int NOPARAMS = 4;
    static final int TYPEARG = 8;

    /** The current mode.
     */
    private int mode = 0;

    /** The mode of the term that was parsed last.
     */
    private int lastmode = 0;

/* ---------- error recovery -------------- */

    private JCErroneous errorTree;

    /** Skip forward until a suitable stop token is found.
     */
    private void skip(boolean stopAtImport, boolean stopAtMemberDecl, boolean stopAtIdentifier, boolean stopAtStatement) {
         while (true) {
             switch (S.token()) {
                case SEMI:
                    S.nextToken();
                    return;
                case PUBLIC:
                case FINAL:
                case ABSTRACT:
                case MONKEYS_AT:
                case EOF:
                case CLASS:
                case ARRAYCLASS:
                case INTERFACE:
                case ENUM:
                    return;
                case IMPORT:
                    if (stopAtImport)
                        return;
                    break;
                case LBRACE:
                case RBRACE:
                case PRIVATE:
                case PROTECTED:
                case STATIC:
                case TRANSIENT:
                case NATIVE:
                case VOLATILE:
                case SYNCHRONIZED:
                case STRICTFP:
                case LT:
                case BYTE:
                case SHORT:
                case CHAR:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case VOID:
                    if (stopAtMemberDecl)
                        return;
                    break;
                case IDENTIFIER:
                   if (stopAtIdentifier)
                        return;
                    break;
                case CASE:
                case DEFAULT:
                case IF:
                case FOR:
                case WHILE:
                case DO:
                case TRY:
                case SWITCH:
                case RETURN:
                case THROW:
                case BREAK:
                case CONTINUE:
                case ELSE:
                case FINALLY:
                case CATCH:
                    if (stopAtStatement)
                        return;
                    break;
            }
            S.nextToken();
        }
    }

    private JCErroneous syntaxError(int pos, String key, Object... arg) {
        return syntaxError(pos, null, key, arg);
    }

    private JCErroneous syntaxError(int pos, List<JCTree> errs, String key, Object... arg) {
        setErrorEndPos(pos);
        reportSyntaxError(pos, key, arg);
        return toP(F.at(pos).Erroneous(errs));
    }

    private int errorPos = Position.NOPOS;
    /**
     * Report a syntax error at given position using the given
     * argument unless one was already reported at the same position.
     */
    private void reportSyntaxError(int pos, String key, Object... arg) {
        if (pos > S.errPos() || pos == Position.NOPOS) {
            if (S.token() == EOF)
                log.error(pos, "premature.eof");
            else
                log.error(pos, key, arg);
        }
        S.errPos(pos);
        if (S.pos() == errorPos)
            S.nextToken(); // guarantee progress
        errorPos = S.pos();
    }


    /** Generate a syntax error at current position unless one was already
     *  reported at the same position.
     */
    private JCErroneous syntaxError(String key) {
        return syntaxError(S.pos(), key);
    }

    /** Generate a syntax error at current position unless one was
     *  already reported at the same position.
     */
    private JCErroneous syntaxError(String key, String arg) {
        return syntaxError(S.pos(), key, arg);
    }

    /** If next input token matches given token, skip it, otherwise report
     *  an error.
     */
    public void accept(Token token) {
        if (S.token() == token) {
            S.nextToken();
        } else {
            setErrorEndPos(S.pos());
            reportSyntaxError(S.prevEndPos(), "expected", keywords.token2string(token));
        }
    }

    /** Report an illegal start of expression/type error at given position.
     */
    JCExpression illegal(int pos) {
        setErrorEndPos(S.pos());
        if ((mode & EXPR) != 0)
            return syntaxError(pos, "illegal.start.of.expr");
        else
            return syntaxError(pos, "illegal.start.of.type");

    }

    /** Report an illegal start of expression/type error at current position.
     */
    JCExpression illegal() {
        return illegal(S.pos());
    }

    /** Diagnose a modifier flag from the set, if any. */
    void checkNoMods(long mods) {
        if (mods != 0) {
            long lowestMod = mods & -mods;
            log.error(S.pos(), "mod.not.allowed.here",
                      Flags.toString(lowestMod).trim());
        }
    }

/* ---------- doc comments --------- */

    /** A hashtable to store all documentation comments
     *  indexed by the tree nodes they refer to.
     *  defined only if option flag keepDocComment is set.
     */
    Map<JCTree, String> docComments;

    /** Make an entry into docComments hashtable,
     *  provided flag keepDocComments is set and given doc comment is non-null.
     *  @param tree   The tree to be used as index in the hashtable
     *  @param dc     The doc comment to associate with the tree, or null.
     */
    void attach(JCTree tree, String dc) {
        if (keepDocComments && dc != null) {
//          System.out.println("doc comment = ");System.out.println(dc);//DEBUG
            docComments.put(tree, dc);
        }
    }

/* -------- source positions ------- */

    private int errorEndPos = -1;

    private void setErrorEndPos(int errPos) {
        if (errPos > errorEndPos)
            errorEndPos = errPos;
    }

    protected int getErrorEndPos() {
        return errorEndPos;
    }

    /**
     * Store ending position for a tree.
     * @param tree   The tree.
     * @param endpos The ending position to associate with the tree.
     */
    protected void storeEnd(JCTree tree, int endpos) {}

    /**
     * Store ending position for a tree.  The ending position should
     * be the ending position of the current token.
     * @param t The tree.
     */
    protected <T extends JCTree> T to(T t) { return t; }

    /**
     * Store ending position for a tree.  The ending position should
     * be greater of the ending position of the previous token and errorEndPos.
     * @param t The tree.
     */
    protected <T extends JCTree> T toP(T t) { return t; }

    /** Get the start position for a tree node.  The start position is
     * defined to be the position of the first character of the first
     * token of the node's source text.
     * @param tree  The tree node
     */
    public int getStartPos(JCTree tree) {
        return TreeInfo.getStartPos(tree);
    }

    /**
     * Get the end position for a tree node.  The end position is
     * defined to be the position of the last character of the last
     * token of the node's source text.  Returns Position.NOPOS if end
     * positions are not generated or the position is otherwise not
     * found.
     * @param tree  The tree node
     */
    public int getEndPos(JCTree tree) {
        return Position.NOPOS;
    }



/* ---------- parsing -------------- */

    /**
     * Ident = IDENTIFIER
     */
    JCIdent ident() {
	return toP(F.at(S.pos()).Ident(name()));
    }
    
    Name name() {
        if (S.token() == IDENTIFIER) {
            Name name = S.name();
            S.nextToken();
            return name;
        } else if (S.token() == ASSERT) {
            if (allowAsserts) {
                log.error(S.pos(), "assert.as.identifier");
                S.nextToken();
                return names.error;
            } else {
                log.warning(S.pos(), "assert.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else if (S.token() == ENUM) {
            if (allowEnums) {
                log.error(S.pos(), "enum.as.identifier");
                S.nextToken();
                return names.error;
            } else {
                log.warning(S.pos(), "enum.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else {
            accept(IDENTIFIER);
            return names.error;
        }
    }

    /**
     * Qualident = Ident { DOT Ident }
     */
    public JCExpression qualident() {
        JCExpression t = ident();
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            t = toP(F.at(pos).Select(t, name()));
        }
        return t;
    }
    
    /**
     * Literal =
     *     INTLITERAL
     *   | LONGLITERAL
     *   | FLOATLITERAL
     *   | DOUBLELITERAL
     *   | CHARLITERAL
     *   | STRINGLITERAL
     *   | TRUE
     *   | FALSE
     *   | NULL
     */
    JCExpression literal(Name prefix) {
        int pos = S.pos();
        JCExpression t = errorTree;
        switch (S.token()) {
        case INTLITERAL:
            try {
                t = F.at(pos).Literal(
                    TypeTags.INT,
                    Convert.string2int(strval(prefix), S.radix()));
            } catch (NumberFormatException ex) {
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case LONGLITERAL:
            try {
                t = F.at(pos).Literal(
                    TypeTags.LONG,
                    new Long(Convert.string2long(strval(prefix), S.radix())));
            } catch (NumberFormatException ex) {
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case FLOATLITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Float n;
            try {
                n = Float.valueOf(proper);
            } catch (NumberFormatException ex) {
                // error already repoted in scanner
                n = Float.NaN;
            }
            if (n.floatValue() == 0.0f && !isZero(proper))
                log.error(S.pos(), "fp.number.too.small");
            else if (n.floatValue() == Float.POSITIVE_INFINITY)
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.FLOAT, n);
            break;
        }
        case DOUBLELITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Double n;
            try {
                n = Double.valueOf(proper);
            } catch (NumberFormatException ex) {
                // error already reported in scanner
                n = Double.NaN;
            }
            if (n.doubleValue() == 0.0d && !isZero(proper))
                log.error(S.pos(), "fp.number.too.small");
            else if (n.doubleValue() == Double.POSITIVE_INFINITY)
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.DOUBLE, n);
            break;
        }
        case CHARLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CHAR,
                S.stringVal().charAt(0) + 0);
            break;
        case STRINGLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CLASS,
                S.stringVal());
            break;
        case TRUE: case FALSE:
            t = F.at(pos).Literal(
                TypeTags.BOOLEAN,
                (S.token() == TRUE ? 1 : 0));
            break;
        case NULL:
            t = F.at(pos).Literal(
                TypeTags.BOT,
                null);
            break;
        default:
            assert false;
        }
        if (t == errorTree)
            t = F.at(pos).Erroneous();
        storeEnd(t, S.endPos());
        S.nextToken();
        return t;
    }
//where
        boolean isZero(String s) {
            char[] cs = s.toCharArray();
            int base = ((Character.toLowerCase(s.charAt(1)) == 'x') ? 16 : 10);
            int i = ((base==16) ? 2 : 0);
            while (i < cs.length && (cs[i] == '0' || cs[i] == '.')) i++;
            return !(i < cs.length && (Character.digit(cs[i], base) > 0));
        }

        String strval(Name prefix) {
            String s = S.stringVal();
            return (prefix.len == 0) ? s : prefix + s;
        }

    /** terms can be either expressions or types.
     */
    public JCExpression expression() {
        return term(EXPR);
    }

    public JCExpression type() {
        return term(TYPE);
    }

    JCExpression term(int newmode) {
        int prevmode = mode;
        mode = newmode;
        JCExpression t = term();
        lastmode = mode;
        mode = prevmode;
        return t;
    }

    /**
     *  Expression = Expression1 [ExpressionRest]
     *  ExpressionRest = [AssignmentOperator Expression1]
     *  AssignmentOperator = "=" | "+=" | "-=" | "*=" | "/=" |
     *                       "&=" | "|=" | "^=" |
     *                       "%=" | "<<=" | ">>=" | ">>>="
     *  Type = Type1
     *  TypeNoParams = TypeNoParams1
     *  StatementExpression = Expression
     *  ConstantExpression = Expression
     */
    JCExpression term() {
        JCExpression t = term1();
        if ((mode & EXPR) != 0 &&
            S.token() == EQ || PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0)
            return termRest(t);
        else
            return t;
    }

    JCExpression termRest(JCExpression t) {
        switch (S.token()) {
        case EQ: {
            int pos = S.pos();
            S.nextToken();
            mode = EXPR;
            JCExpression t1 = term();
            return toP(F.at(pos).Assign(t, t1));
        }
        case PLUSEQ:
        case SUBEQ:
        case STAREQ:
        case SLASHEQ:
        case PERCENTEQ:
        case AMPEQ:
        case BAREQ:
        case CARETEQ:
        case LTLTEQ:
        case GTGTEQ:
        case GTGTGTEQ:
            int pos = S.pos();
            Token token = S.token();
            S.nextToken();
            mode = EXPR;
            JCExpression t1 = term();
            return F.at(pos).Assignop(optag(token), t, t1);
        default:
            return t;
        }
    }

    /** Expression1   = Expression2 [Expression1Rest]
     *  Type1         = Type2
     *  TypeNoParams1 = TypeNoParams2
     */
    JCExpression term1() {
        JCExpression t = term2();
        if ((mode & EXPR) != 0 && S.token() == QUES) {
            mode = EXPR;
            return term1Rest(t);
        } else {
            return t;
        }
    }

    /** Expression1Rest = ["?" Expression ":" Expression1]
     */
    JCExpression term1Rest(JCExpression t) {
        if (S.token() == QUES) {
            int pos = S.pos();
            S.nextToken();
            JCExpression t1 = term();
            accept(COLON);
            JCExpression t2 = term1();
            return F.at(pos).Conditional(t, t1, t2);
        } else {
            return t;
        }
    }

    /** Expression2   = Expression3 [Expression2Rest]
     *  Type2         = Type3
     *  TypeNoParams2 = TypeNoParams3
     */
    JCExpression term2() {
        JCExpression t = term3();
        if ((mode & EXPR) != 0 && prec(S.token()) >= TreeInfo.orPrec) {
            mode = EXPR;
            return term2Rest(t, TreeInfo.orPrec);
        } else {
            return t;
        }
    }

    /*  Expression2Rest = {infixop Expression3}
     *                  | Expression3 instanceof Type
     *  infixop         = "||"
     *                  | "&&"
     *                  | "|"
     *                  | "^"
     *                  | "&"
     *                  | "==" | "!="
     *                  | "<" | ">" | "<=" | ">="
     *                  | "<<" | ">>" | ">>>"
     *                  | "+" | "-"
     *                  | "*" | "/" | "%"
     */
    JCExpression term2Rest(JCExpression t, int minprec) {
        List<JCExpression[]> savedOd = odStackSupply.elems;
        JCExpression[] odStack = newOdStack();
        List<Token[]> savedOp = opStackSupply.elems;
        Token[] opStack = newOpStack();
        // optimization, was odStack = new Tree[...]; opStack = new Tree[...];
        int top = 0;
        odStack[0] = t;
        int startPos = S.pos();
        Token topOp = ERROR;
        while (prec(S.token()) >= minprec) {
            opStack[top] = topOp;
            top++;
            topOp = S.token();
            int pos = S.pos();
            S.nextToken();
            odStack[top] = topOp == INSTANCEOF ? type() : term3();
            while (top > 0 && prec(topOp) >= prec(S.token())) {
                odStack[top-1] = makeOp(pos, topOp, odStack[top-1],
                                        odStack[top]);
                top--;
                topOp = opStack[top];
            }
        }
        assert top == 0;
        t = odStack[0];

        if (t.getTag() == JCTree.PLUS) {
            StringBuffer buf = foldStrings(t);
            if (buf != null) {
                t = toP(F.at(startPos).Literal(TypeTags.CLASS, buf.toString()));
            }
        }

        odStackSupply.elems = savedOd; // optimization
        opStackSupply.elems = savedOp; // optimization
        return t;
    }
//where
        /** Construct a binary or type test node.
         */
        private JCExpression makeOp(int pos,
                                    Token topOp,
                                    JCExpression od1,
                                    JCExpression od2)
        {
            if (topOp == INSTANCEOF) {
                return F.at(pos).TypeTest(od1, od2);
            } else {
                return F.at(pos).Binary(optag(topOp), od1, od2);
            }
        }
        /** If tree is a concatenation of string literals, replace it
         *  by a single literal representing the concatenated string.
         */
        protected StringBuffer foldStrings(JCTree tree) {
            List<String> buf = List.nil();
            while (true) {
                if (tree.getTag() == JCTree.LITERAL) {
                    JCLiteral lit = (JCLiteral) tree;
                    if (lit.typetag == TypeTags.CLASS) {
                        StringBuffer sbuf =
                            new StringBuffer((String)lit.value);
                        while (buf.nonEmpty()) {
                            sbuf.append(buf.head);
                            buf = buf.tail;
                        }
                        return sbuf;
                    }
                } else if (tree.getTag() == JCTree.PLUS) {
                    JCBinary op = (JCBinary)tree;
                    if (op.rhs.getTag() == JCTree.LITERAL) {
                        JCLiteral lit = (JCLiteral) op.rhs;
                        if (lit.typetag == TypeTags.CLASS) {
                            buf = buf.prepend((String) lit.value);
                            tree = op.lhs;
                            continue;
                        }
                    }
                }
                return null;
            }
        }

        /** optimization: To save allocating a new operand/operator stack
         *  for every binary operation, we use supplys.
         */
        ListBuffer<JCExpression[]> odStackSupply = new ListBuffer<JCExpression[]>();
        ListBuffer<Token[]> opStackSupply = new ListBuffer<Token[]>();

        private JCExpression[] newOdStack() {
            if (odStackSupply.elems == odStackSupply.last)
                odStackSupply.append(new JCExpression[infixPrecedenceLevels + 1]);
            JCExpression[] odStack = odStackSupply.elems.head;
            odStackSupply.elems = odStackSupply.elems.tail;
            return odStack;
        }

        private Token[] newOpStack() {
            if (opStackSupply.elems == opStackSupply.last)
                opStackSupply.append(new Token[infixPrecedenceLevels + 1]);
            Token[] opStack = opStackSupply.elems.head;
            opStackSupply.elems = opStackSupply.elems.tail;
            return opStack;
        }

    /** Expression3    = PrefixOp Expression3
     *                 | "(" Expr | TypeNoParams ")" Expression3
     *                 | Primary {Selector} {PostfixOp}
     *  Primary        = "(" Expression ")"
     *                 | Literal
     *                 | [TypeRPLGroupArgs] THIS [Arguments]
     *                 | [TypeRPLGroupArgs] SUPER SuperSuffix
     *                 | NEW [TypeRPLGroupArgs] Creator
     *                 | Ident { "." Ident }
     *                   [ "[" ( "]" BracketsOpt "." CLASS | Expression "]" )
     *                   | Arguments
     *                   | "." ( CLASS | THIS | 
     *                           [TypeRPLGroupArgs] SUPER Arguments | 
     *                           NEW [TypeRPLGroupArgs] InnerCreator )
     *                   ]
     *                 | BasicType BracketsOpt "." CLASS
     *  PrefixOp       = "++" | "--" | "!" | "~" | "+" | "-"
     *  PostfixOp      = "++" | "--"
     *  Type3          = Ident { "." Ident } [TypeRPLGroupArgs] 
     *                   {TypeSelector} BracketsOpt | BasicType
     *  TypeNoParams3  = Ident { "." Ident } BracketsOpt
     *  Selector       = "." [TypeRPLGroupArgs] Ident [Arguments]
     *                 | "." THIS
     *                 | "." [TypeRPLGroupArgs] SUPER SuperSuffix
     *                 | "." NEW [TypeRPLGroupArgs] InnerCreator
     *                 | "[" Expression "]"
     *  TypeSelector   = "." Ident [TypeRPLGroupArgs]
     *  SuperSuffix    = Arguments | "." Ident [Arguments]
     */
    protected JCExpression term3() {
        int pos = S.pos();
        JCExpression t;
        TypeRPLGroupArgs typeRPLGroupArgs = typeRPLGroupArgsOpt(EXPR);
        List<JCExpression> typeArgs = typeRPLGroupArgs.typeArgs;
        List<DPJRegionPathList> rplArgs = typeRPLGroupArgs.rplArgs;
        List<JCIdent> groupArgs = typeRPLGroupArgs.groupArgs;
        switch (S.token()) {
        case QUES:
            if ((mode & TYPE) != 0 && (mode & (TYPEARG|NOPARAMS)) == TYPEARG) {
                mode = TYPE;
                return typeOrRPLArgument();
            } else
                return illegal();
        case PLUSPLUS: case SUBSUB: case BANG: case TILDE: case PLUS: case SUB:
            if (rplArgs == null && typeArgs == null && 
        	    groupArgs == null && (mode & EXPR) != 0) {
                Token token = S.token();
                S.nextToken();
                mode = EXPR;
                if (token == SUB &&
                    (S.token() == INTLITERAL || S.token() == LONGLITERAL) &&
                    S.radix() == 10) {
                    mode = EXPR;
                    t = literal(names.hyphen);
                } else {
                    t = term3();
                    return F.at(pos).Unary(unoptag(token), t);
                }
            } else return illegal();
            break;
        case LPAREN:
            if (rplArgs == null && typeArgs == null && 
        	    groupArgs == null && (mode & EXPR) != 0) {
                S.nextToken();
                mode = EXPR | TYPE | NOPARAMS;
                t = term3();
                if ((mode & TYPE) != 0 && S.token() == LTLT) {
                    int op = JCTree.SL;
                    int pos1 = S.pos();
                    S.nextToken();
                    JCExpression t1 = term3();
                    mode = EXPR;
                    t = F.at(pos1).Binary(op, t, term2Rest(t1, TreeInfo.shiftPrec));
                    t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                }
                if ((mode & TYPE) != 0 && S.token() == LT) {
                    // Handle either (C<T>) or (C<R>) or (EXP1 < EXP2)
                    int op = JCTree.LT;
                    int pos1 = S.pos();
                    S.nextToken();
                    mode &= (EXPR | TYPE);
                    mode |= TYPEARG;
                    JCExpression t1 = null;
                    if (S.token() == REGION || S.token() == STAR || 
                	    S.token() == LBRACKET) {
                	// We know we have an RPL
                	t1 = rpl();
                    } else {
                	// Ambiguous:  Could be type or RPL or expr
                	t1 = term3();
                    }
                    if ( // (mode & TYPE) != 0 && <-- Original javac had this
                        (S.token() == COMMA || S.token() == GT || S.token() == COLON)) {
                	// We now know we're looking at a type or RPL
                	if (S.token() == COLON) {
                	    // We read the first element of an RPL; get the rest of it
                	    DPJRegionPathListElt elt = 
                		F.at(pos).RegionPathListElt(t1, DPJRegionPathListElt.NAME);
                	    t1 = rpl(elt);
                	}
                	mode = TYPE;
                        ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
                        args.append(t1);
                        // TODO: Make this handle group args properly
                        while (S.token() == COMMA) {
                            S.nextToken();
                            args.append(typeOrRPLArgument());
                        }
                        accept(GT);
                        t = F.at(pos1).TypeApply(t, args.toList(), null, 
                        	List.<JCIdent>nil());
                        checkGenerics();
                        t = bracketsOpt(toP(t));
                    } else if ((mode & EXPR) != 0) {
                        mode = EXPR;
                        t = F.at(pos1).Binary(op, t, term2Rest(t1, TreeInfo.shiftPrec));
                        t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                    } else {
                        accept(GT);
                    }
                } else {
                    t = termRest(term1Rest(term2Rest(t, TreeInfo.orPrec)));
                }
                accept(RPAREN);
                lastmode = mode;
                mode = EXPR;
                if ((lastmode & EXPR) == 0) {
                    JCExpression t1 = term3();
                    return F.at(pos).TypeCast(t, t1);
                } else if ((lastmode & TYPE) != 0) {
                    switch (S.token()) {
                    /*case PLUSPLUS: case SUBSUB: */
                    case BANG: case TILDE:
                    case LPAREN: case THIS: case SUPER:
                    case INTLITERAL: case LONGLITERAL: case FLOATLITERAL:
                    case DOUBLELITERAL: case CHARLITERAL: case STRINGLITERAL:
                    case TRUE: case FALSE: case NULL:
                    case NEW: case IDENTIFIER: case ASSERT: case ENUM:
                    case BYTE: case SHORT: case CHAR: case INT:
                    case LONG: case FLOAT: case DOUBLE: case BOOLEAN: case VOID:
                        JCExpression t1 = term3();
                        return F.at(pos).TypeCast(t, t1);
                    }
                }
            } else return illegal();
            t = toP(F.at(pos).Parens(t));
            break;
        // THIS constructor invocation:  [TypeRPLGroupArgs] THIS [Arguments]
        case THIS:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(F.at(pos).Ident(names._this));
                S.nextToken();
                if (rplArgs == null && typeArgs == null && groupArgs == null)
                    t = argumentsOpt(null, null, null, t);
                else
                    t = arguments(rplArgs, typeArgs, groupArgs, t);
                rplArgs = null;
                typeArgs = null;
                groupArgs = null;
            } else return illegal();
            break;
        // SUPER constructor invocation:  [TypeRPLGroupArgs] SUPER SuperSuffix
        case SUPER:
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                t = to(superSuffix(rplArgs, typeArgs, groupArgs,
                	   	   F.at(pos).Ident(names._super)));
                rplArgs = null;
                typeArgs = null;
                groupArgs = null;
            } else return illegal();
            break;
        // Literals
        case INTLITERAL: case LONGLITERAL: case FLOATLITERAL: case DOUBLELITERAL:
        case CHARLITERAL: case STRINGLITERAL:
        case TRUE: case FALSE: case NULL:
            if (rplArgs == null && typeArgs == null 
        	    && groupArgs == null && (mode & EXPR) != 0) {
                mode = EXPR;
                t = literal(names.empty);
            } else return illegal();
            break;
        // Object creation:  NEW [TypeRPLGroupArgs] Creator 
        case NEW:
            if (rplArgs != null || typeArgs != null || 
        	    groupArgs != null) return illegal();
            if ((mode & EXPR) != 0) {
                mode = EXPR;
                S.nextToken();
                if (S.token() == LT) {
                    typeRPLGroupArgs = typeRPLGroupArgs();
                    typeArgs = typeRPLGroupArgs.typeArgs;
                    rplArgs = typeRPLGroupArgs.rplArgs;
                    groupArgs = typeRPLGroupArgs.groupArgs;
                }
                t = creator(pos, rplArgs, typeArgs, groupArgs);
                rplArgs = null;
                typeArgs = null;
                groupArgs = null;
            } else return illegal();
            break;
        case IDENTIFIER: case ASSERT: case ENUM:
            if (rplArgs != null || typeArgs != null
        	    || groupArgs != null) return illegal();
            t = ident();
            loop: while (true) {
                pos = S.pos();
                switch (S.token()) {
                case LBRACKET:
                    S.nextToken();
                    if (S.token() == RBRACKET) {
                        // Ident { "." Ident } "[" "]" BracketsOpt ["." CLASS]
                	t = bracketsOptCont(t,pos);
                        t = bracketsSuffix(t);
                    } else {
                        // Ident { "." Ident } "[" Expression "]"
                        if ((mode & EXPR) != 0) {
                            mode = EXPR;
                            JCExpression t1 = term();
                            t = to(F.at(pos).Indexed(t, t1));
                        }
                        accept(RBRACKET);
                    }
                    break loop;
                // Method invocation:  Ident { "." Ident } [Arguments]
                case LPAREN:
                    if ((mode & EXPR) != 0) {
                        mode = EXPR;
                        t = arguments(rplArgs, typeArgs, groupArgs, t);
                        rplArgs = null;
                        typeArgs = null;
                        groupArgs = null;
                    }
                    break loop;
                case DOT:
                    S.nextToken();
                    typeRPLGroupArgs = typeRPLGroupArgsOpt(EXPR);
                    typeArgs = typeRPLGroupArgs.typeArgs; //Opt(EXPR).typeArgs;
                    rplArgs = typeRPLGroupArgs.rplArgs;
                    groupArgs = typeRPLGroupArgs.groupArgs;
                    if ((mode & EXPR) != 0) {
                        switch (S.token()) {
                        // Ident { "." Ident } "." CLASS
                        case CLASS:
                            if (rplArgs != null || typeArgs != null
                        	    || groupArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._class));
                            S.nextToken();
                            break loop;
                        // Ident { "." Ident } "." THIS
                        case THIS:
                            if (rplArgs != null || typeArgs != null
                        	    || groupArgs != null) return illegal();
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._this));
                            S.nextToken();
                            break loop;
                        // Ident { "." Ident } "." [TypeRPLGroupArgs] SUPER Arguments
                        case SUPER:
                            mode = EXPR;
                            t = to(F.at(pos).Select(t, names._super));
                            t = superSuffix(rplArgs, typeArgs, groupArgs, t);
                            rplArgs = null;
                            typeArgs = null;
                            groupArgs = null;
                            break loop;
                        // Ident { "." Ident } "." NEW [TypeRPLGroupArgs] InnerCreator
                        case NEW:
                            if (rplArgs != null || typeArgs != null ||
                        	    groupArgs != null) return illegal();
                            mode = EXPR;
                            int pos1 = S.pos();
                            S.nextToken();
                            if (S.token() == LT) {
                        	typeRPLGroupArgs = typeRPLGroupArgs();
                        	typeArgs = typeRPLGroupArgs.typeArgs;
                        	rplArgs = typeRPLGroupArgs.rplArgs;
                        	groupArgs = typeRPLGroupArgs.groupArgs;
                            }
                            t = innerCreator(pos1, rplArgs, typeArgs, groupArgs, t);
                            rplArgs = null;
                            typeArgs = null;
                            groupArgs = null;
                            break loop;
                        }
                    }
                    // rplArgs and typeArgs saved for next loop iteration.
                    t = toP(F.at(pos).Select(t, name()));
                    break;
                default:
                    break loop;
                }
            }
            if (rplArgs != null || typeArgs != null
        	    || groupArgs != null) illegal();
            t = typeRPLGroupArgsOpt(t);
            break;
        // Basic types
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (rplArgs != null || typeArgs != null ||
        	    groupArgs != null) illegal();
            t = bracketsSuffix(bracketsOpt(basicType()));
            break;
        case VOID:
            if (rplArgs != null || typeArgs != null ||
        	    groupArgs != null) illegal();
            if ((mode & EXPR) != 0) {
                S.nextToken();
                if (S.token() == DOT) {
                    JCPrimitiveTypeTree ti = toP(F.at(pos).TypeIdent(TypeTags.VOID));
                    t = bracketsSuffix(ti);
                } else {
                    return illegal(pos);
                }
            } else {
                return illegal();
            }
            break;
        default:
            return illegal();
        }
        if (rplArgs != null || typeArgs != null ||
        	groupArgs != null) illegal();
        // Selector
        while (true) {
            int pos1 = S.pos();
            if (S.token() == LBRACKET) {
                S.nextToken();
                if ((mode & TYPE) != 0) {
                    int oldmode = mode;
                    mode = TYPE;
                    // "[" "]"
                    if (S.token() == RBRACKET) {
                	t = bracketsOptCont(t, pos1);
                        return t;
                    }
                    mode = oldmode;
                }
                // "[" Expression "]"
                if ((mode & EXPR) != 0) {
                    mode = EXPR;
                    JCExpression t1 = term();
                    t = to(F.at(pos1).Indexed(t, t1));
                }
                accept(RBRACKET);
            } else if (S.token() == DOT) {
                S.nextToken();
                typeRPLGroupArgs = typeRPLGroupArgsOpt(EXPR);
                typeArgs = typeRPLGroupArgs.typeArgs; //Opt(EXPR).typeArgs;
                rplArgs = typeRPLGroupArgs.rplArgs;
                groupArgs = typeRPLGroupArgs.groupArgs;
                if (S.token() == SUPER && (mode & EXPR) != 0) {
                    // "." [TypeRPLGroupArgs] SUPER Arguments
                    mode = EXPR;
                    t = to(F.at(pos1).Select(t, names._super));
                    S.nextToken();
                    t = arguments(rplArgs, typeArgs, groupArgs, t);
                    rplArgs = null;
                    typeArgs = null;
                    groupArgs = null;
                } else if (S.token() == NEW && (mode & EXPR) != 0) {
                    // "." NEW [TypeRPLGroupArgs] InnerCreator
                    if (rplArgs != null || typeArgs != null
                	    || groupArgs != null) return illegal();
                    mode = EXPR;
                    int pos2 = S.pos();
                    S.nextToken();
                    if (S.token() == LT) {
                	typeRPLGroupArgs = typeRPLGroupArgs();
                	typeArgs = typeRPLGroupArgs.typeArgs;
                	rplArgs = typeRPLGroupArgs.rplArgs;
                	groupArgs = typeRPLGroupArgs.groupArgs;
                    }
                    t = innerCreator(pos2, rplArgs, typeArgs, groupArgs, t);
                    rplArgs = null;
                    typeArgs = null;
                    groupArgs = null;
                } else {
                    t = toP(F.at(pos1).Select(t, name()));
                    t = argumentsOpt(rplArgs, typeArgs, groupArgs,
                	    typeRPLGroupArgsOpt(t));
                    rplArgs = null;
                    typeArgs = null;
                    groupArgs = null;
                }
            } else {
                break;
            }
        }
        while ((S.token() == PLUSPLUS || S.token() == SUBSUB) && (mode & EXPR) != 0) {
            mode = EXPR;
            t = to(F.at(S.pos()).Unary(
                  S.token() == PLUSPLUS ? JCTree.POSTINC : JCTree.POSTDEC, t));
            S.nextToken();
        }
        return toP(t);
    }

    /** SuperSuffix = Arguments | "." [TypeRPLGroupArgs] Ident [Arguments]
     */
    JCExpression superSuffix(List<DPJRegionPathList> rplArgs,
	                     List<JCExpression> typeArgs, 
	                     List<JCIdent> groupArgs,
	                     JCExpression t) {
        S.nextToken();
        if (S.token() == LPAREN || typeArgs != null || rplArgs != null
        	|| groupArgs != null) {
            t = arguments(rplArgs, typeArgs, groupArgs, t);
        } else {
            int pos = S.pos();
            accept(DOT);
            rplArgs = null;
            if (S.token() == LT) {
        	TypeRPLGroupArgs typeRPLGroupArgs = typeRPLGroupArgs();
        	typeArgs = typeRPLGroupArgs.typeArgs;
        	rplArgs = typeRPLGroupArgs.rplArgs;
        	groupArgs = typeRPLGroupArgs.groupArgs;
            }
            t = toP(F.at(pos).Select(t, name()));
            t = argumentsOpt(rplArgs, typeArgs, groupArgs, t);
        }
        return t;
    }

    /** BasicType = BYTE | SHORT | CHAR | INT | LONG | FLOAT | DOUBLE | BOOLEAN
     */
    JCPrimitiveTypeTree basicType() {
        JCPrimitiveTypeTree t = to(F.at(S.pos()).TypeIdent(typetag(S.token())));
        S.nextToken();
        return t;
    }

    /** ArgumentsOpt = [ Arguments ]
     */
    JCExpression argumentsOpt(List<DPJRegionPathList> rplArgs,
	                      List<JCExpression> typeArgs, 
	                      List<JCIdent> groupArgs, JCExpression t) {
        if ((mode & EXPR) != 0 && S.token() == LPAREN || rplArgs != null ||
        	typeArgs != null || groupArgs != null) {
            mode = EXPR;
            return arguments(rplArgs, typeArgs, groupArgs, t);
        } else {
            return t;
        }
    }

    /** Arguments = "(" [Expression { COMMA Expression }] ")"
     */
    List<JCExpression> arguments() {
        ListBuffer<JCExpression> args = lb();
        if (S.token() == LPAREN) {
            S.nextToken();
            if (S.token() != RPAREN) {
                args.append(expression());
                while (S.token() == COMMA) {
                    S.nextToken();
                    args.append(expression());
                }
            }
            accept(RPAREN);
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LPAREN));
        }
        return args.toList();
    }

    JCMethodInvocation arguments(List<DPJRegionPathList> rplArgs,
	    	                 List<JCExpression> typeArgs, 
	    	                 List<JCIdent> groupArgs, JCExpression t) {
        int pos = S.pos();
        List<JCExpression> args = arguments();
        return toP(F.at(pos).Apply(rplArgs, typeArgs, groupArgs, t, args));
    }

    /**  TypeRPLGroupArgsOpt = [ TypeRPLGroupArgs ]
     */
    JCExpression typeRPLGroupArgsOpt(JCExpression t) {
        if (S.token() == LT &&
            (mode & TYPE) != 0 &&
            (mode & NOPARAMS) == 0) {
            mode = TYPE;
            checkGenerics();
            return typeRPLGroupArgs(t);
        } else {
            return t;
        }
    }
    TypeRPLGroupArgs typeRPLGroupArgsOpt() {
        return typeRPLGroupArgsOpt(TYPE);
    }

    TypeRPLGroupArgs typeRPLGroupArgsOpt(int useMode) {
        if (S.token() == LT) {
            checkGenerics();
            if ((mode & useMode) == 0 ||
                (mode & NOPARAMS) != 0) {
                illegal();
            }
            mode = useMode;
            return typeRPLGroupArgs();
        }
        return new TypeRPLGroupArgs(null, null, null);
    }

    /** TypeRPLGroupArgs := "<" ( TypeArgs | RPLGroupArgs | TypeArgs "," RPLGroupArgs ) ">"
     *  RPLGroupArgs := RPLList | GroupList | RPLList "," GroupList
     *  TypeArgs = TypeArgument {"," TypeArgument}
     *  
     *  Note that some of the "types" in the "TypeArgs" may actually be RPLs.  
     *  We won't know that until symbol resolution time.  Here we're trading
     *  some ambiguity (in the form of late resolution) for brevity of notation
     *  (not requiring "region" in front of the first RPL).
     */    
    private class TypeRPLGroupArgs {
	public List<JCExpression> typeArgs;
	public List<DPJRegionPathList> rplArgs;
	public List<JCIdent> groupArgs;
	public TypeRPLGroupArgs(List<JCExpression> typeArgs, 
		List<DPJRegionPathList> rplArgs, List<JCIdent> groupArgs) {
	    this.typeArgs = typeArgs;
	    this.rplArgs = rplArgs;
	    this.groupArgs = groupArgs;
	}
    }
    
    TypeRPLGroupArgs typeRPLGroupArgs() {
        ListBuffer<JCExpression> typeArgs = lb();
        List<DPJRegionPathList> rplArgs = null;
        List<JCIdent> groupArgs = List.nil();
        if (S.token() == LT) {
            S.nextToken();
            // Get the args that may be type (or group) or RPL args, if any.  
            // (Potential group args are parsed the same as types.)
            // Which they are will be resolved at type checking time.
            if (S.token() != REGION && S.token() != REFGROUP) {
        	typeArgs.append(((mode & EXPR) == 0) ? typeOrRPLArgument() : type());
        	while (S.token() == COMMA) {
        	    S.nextToken();
        	    if (S.token() == REGION || S.token() == REFGROUP) break;
        	    typeArgs.append(((mode & EXPR) == 0) ? typeOrRPLArgument() : type());
        	}
            }
            // Get the args that are definitely RPL args, if any
            if (S.token() == SEMI || S.token() == REGION) {
        	if (S.token() == SEMI) S.nextToken();
        	rplArgs = RPLList();
        	if (S.token() == COMMA || S.token() == SEMI) S.nextToken();
            }
            // Get the group args, if any
            if (S.token() == REFGROUP) {        	
        	groupArgs = groups();
            }
            endOfArgs();
        } else {
            syntaxError(S.pos(), "expected", keywords.token2string(LT));
        }
        return new TypeRPLGroupArgs(typeArgs.toList(), rplArgs, groupArgs);
    }

    void endOfArgs() {
        switch (S.token()) {
        case GTGTGTEQ:
            S.token(GTGTEQ);
            break;
        case GTGTEQ:
            S.token(GTEQ);
            break;
        case GTEQ:
            S.token(EQ);
            break;
        case GTGTGT:
            S.token(GTGT);
            break;
        case GTGT:
            S.token(GT);
            break;
        default:
            accept(GT);
            break;
        }	
    }
    
    /** TypeOrRPLArgument = Type
     *               | "?"
     *               | "?" EXTENDS Type {"&" Type}
     *               | "?" SUPER Type
     *               | RPLElement { ":" RPLElement}
     *  RPLElement = Ident | THIS | "[" Expr "]" | "[?]"
     */
    JCExpression typeOrRPLArgument() {
        if (S.token() != QUES) return typeOrRPL();
        int pos = S.pos();
        S.nextToken();
        if (S.token() == EXTENDS) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.EXTENDS));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == SUPER) {
            TypeBoundKind t = to(F.at(S.pos()).TypeBoundKind(BoundKind.SUPER));
            S.nextToken();
            return F.at(pos).Wildcard(t, type());
        } else if (S.token() == IDENTIFIER) {
            //error recovery
            reportSyntaxError(S.prevEndPos(), "expected3",
                    keywords.token2string(GT),
                    keywords.token2string(EXTENDS),
                    keywords.token2string(SUPER));
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            JCExpression wc = toP(F.at(pos).Wildcard(t, null));
            JCIdent id = ident();
            return F.at(pos).Erroneous(List.<JCTree>of(wc, id));
        } else {
            TypeBoundKind t = F.at(Position.NOPOS).TypeBoundKind(BoundKind.UNBOUND);
            return toP(F.at(pos).Wildcard(t, null));
        }
    }

    JCTypeApply typeRPLGroupArgs(JCExpression t) {
        int pos = S.pos();
        TypeRPLGroupArgs typeRPLGroupArgs = typeRPLGroupArgs();
        List<JCExpression> typeArgs = typeRPLGroupArgs.typeArgs;
        List<DPJRegionPathList> rplArgs = typeRPLGroupArgs.rplArgs;
        List<JCIdent> groupArgs = typeRPLGroupArgs.groupArgs;
        JCTypeApply result = toP(F.at(pos).TypeApply(t, typeArgs, 
        	rplArgs, groupArgs));
        return result;
    }
    
    /** BracketsOpt = {"[" "]"}
     */
    private JCExpression bracketsOpt(JCExpression t) {
        if (S.token() == LBRACKET) {
            int pos = S.pos();
            S.nextToken();
            t = bracketsOptCont(t, pos);
            F.at(pos);
        }
        return t;
    }

    private JCArrayTypeTree bracketsOptCont(JCExpression t, int pos) {
        accept(RBRACKET);
        t = bracketsOpt(t);
        return toP(F.at(pos).TypeArray(t));
    }

    /** BracketsSuffixExpr = "." CLASS
     *  BracketsSuffixType =
     */
    JCExpression bracketsSuffix(JCExpression t) {
        if ((mode & EXPR) != 0 && S.token() == DOT) {
            mode = EXPR;
            int pos = S.pos();
            S.nextToken();
            accept(CLASS);
            if (S.pos() == errorEndPos) {
                // error recovery
                Name name = null;
                if (S.token() == IDENTIFIER) {
                    name = S.name();
                    S.nextToken();
                } else {
                    name = names.error;
                }
                t = F.at(pos).Erroneous(List.<JCTree>of(toP(F.at(pos).Select(t, name))));
            } else {
                t = toP(F.at(pos).Select(t, names._class));
            }
        } else if ((mode & TYPE) != 0) {
            mode = TYPE;
        } else {
            syntaxError(S.pos(), "dot.class.expected");
        }
        return t;
    }

    /** Creator = Qualident [TypeRPLGroupArgs] ( ArrayCreatorRest | ClassCreatorRest )
     */
    JCExpression creator(int newpos, List<DPJRegionPathList> rplArgs,
	                 List<JCExpression> typeArgs,
	                 List<JCIdent> groupArgs) {
        switch (S.token()) {
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN:
            if (typeArgs == null)
                return arrayCreatorRest(newpos, basicType());
            break;
        default:
        }
        JCExpression t = qualident();
        int oldmode = mode;
        mode = TYPE;
        if (S.token() == LT) {
            checkGenerics();
            t = typeRPLGroupArgs(t);
        }
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            t = toP(F.at(pos).Select(t, name()));
            if (S.token() == LT) {
                checkGenerics();
                t = typeRPLGroupArgs(t);
            }
        }
        mode = oldmode;
        if (S.token() == LBRACKET) {
            JCExpression e = arrayCreatorRest(newpos, t);
            if (typeArgs != null) {
                int pos = newpos;
                if (!typeArgs.isEmpty() && typeArgs.head.pos != Position.NOPOS) {
                    // note: this should always happen but we should
                    // not rely on this as the parser is continuously
                    // modified to improve error recovery.
                    pos = typeArgs.head.pos;
                }
                setErrorEndPos(S.prevEndPos());
                reportSyntaxError(pos, "cannot.create.array.with.type.arguments");
                return toP(F.at(newpos).Erroneous(typeArgs.prepend(e)));
            }
            return e;
        } else if (S.token() == LPAREN) {
            return classCreatorRest(newpos, null, rplArgs, 
        	    typeArgs, groupArgs, t);
        } else {
            reportSyntaxError(S.pos(), "expected2",
                               keywords.token2string(LPAREN),
                               keywords.token2string(LBRACKET));
            t = toP(F.at(newpos).NewClass(null, rplArgs, typeArgs, 
        	    groupArgs, t, List.<JCExpression>nil(), null));
            return toP(F.at(newpos).Erroneous(List.<JCTree>of(t)));
        }
    }

    /** InnerCreator = Ident [TypeRPLGroupArgs] ClassCreatorRest
     */
    JCExpression innerCreator(int newpos, List<DPJRegionPathList> rplArgs,
	                      List<JCExpression> typeArgs, 
	                      List<JCIdent> groupArgs, JCExpression encl) {
        JCExpression t = ident();
        if (S.token() == LT) {
            checkGenerics();
            t = typeRPLGroupArgs(t);
        }
        return classCreatorRest(newpos, encl, rplArgs, 
        	typeArgs, groupArgs, t);
    }

    /** ArrayCreatorRest = "[" ( "]" BracketsOpt ArrayInitializer
     *                         | Expression "]" {"[" Expression "]"} BracketsOpt )
     */
    JCExpression arrayCreatorRest(int newpos, JCExpression elemtype) {
        accept(LBRACKET);
        if (S.token() == RBRACKET) {
            accept(RBRACKET);
            elemtype = bracketsOpt(elemtype);
            if (S.token() == LBRACE) {
                return arrayInitializer(newpos, elemtype);
            } else {
                return syntaxError(S.pos(), "array.dimension.missing");
            }
        } else {
            ListBuffer<JCExpression> dims = new ListBuffer<JCExpression>();
            dims.append(expression());
            accept(RBRACKET);
            while (S.token() == LBRACKET) {
                int pos = S.pos();
                S.nextToken();
                if (S.token() == RBRACKET) {
                    elemtype = bracketsOptCont(elemtype, pos);
                } else {
                    dims.append(expression());
                    accept(RBRACKET);
                }
            }
            JCNewArray result = toP(F.at(newpos).NewArray(elemtype, dims.toList(), null));
            return result;
        }
    }

    /** ClassCreatorRest = Arguments [ClassBody]
     */
    JCExpression classCreatorRest(int newpos,
                                  JCExpression encl,
                                  List<DPJRegionPathList> rplArgs,
                                  List<JCExpression> typeArgs,
                                  List<JCIdent> groupArgs,
                                  JCExpression t)
    {
        List<JCExpression> args = arguments();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
            int pos = S.pos();
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
            body = toP(F.at(pos).AnonymousClassDef(mods, defs));
        }
        return toP(F.at(newpos).NewClass(encl, rplArgs, typeArgs, 
        	groupArgs, t, args, body));
    }

    /** ArrayInitializer = "{" [VariableInitializer {"," VariableInitializer}] [","] "}"
     */
    JCExpression arrayInitializer(int newpos, JCExpression t) {
        accept(LBRACE);
        ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE) {
            elems.append(variableInitializer());
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE) break;
                elems.append(variableInitializer());
            }
        }
        accept(RBRACE);
        return toP(F.at(newpos).NewArray(t, List.<JCExpression>nil(), elems.toList()));
    }

    /** VariableInitializer = ArrayInitializer | Expression
     */
    public JCExpression variableInitializer() {
        return S.token() == LBRACE ? arrayInitializer(S.pos(), null) : expression();
    }

    /** ParExpression = "(" Expression ")"
     */
    JCExpression parExpression() {
        accept(LPAREN);
        JCExpression t = expression();
        accept(RPAREN);
        return t;
    }

    /** Block = "{" BlockStatements "}"
     */
    JCBlock block(int pos, long flags) {
        accept(LBRACE);
        List<JCStatement> stats = blockStatements();
        JCBlock t = F.at(pos).Block(flags, stats);
        while (S.token() == CASE || S.token() == DEFAULT) {
            syntaxError("orphaned", keywords.token2string(S.token()));
            switchBlockStatementGroups(false);
        }
        // the Block node has a field "endpos" for first char of last token, which is
        // usually but not necessarily the last char of the last token.
        t.endpos = S.pos();
        accept(RBRACE);
        return toP(t);
    }

    public JCBlock block() {
        return block(S.pos(), 0);
    }

    /** BlockStatements = { BlockStatement }
     *  BlockStatement  = LocalVariableDeclarationStatement
     *                  | LocalRegionDeclarationStatement
     *                  | ClassOrInterfaceOrEnumDeclaration
     *                  | [Ident ":"] Statement
     *  LocalVariableDeclarationStatement
     *                  = { FINAL | '@' Annotation } Type VariableDeclarators ";"
     *  LocalRegionDeclarationStatement
     *  		= regionDeclaration ";" 
     */
    @SuppressWarnings("fallthrough")
    List<JCStatement> blockStatements() {
//todo: skip to anchor on error(?)
        int lastErrPos = -1;
        ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
        while (true) {
            int pos = S.pos();
            Token token = S.token();
            switch (token) {
            case RBRACE: case CASE: case DEFAULT: case EOF:
                return stats.toList();
            case LBRACE: case IF: case FOR:
            case WHILE: case DO: case TRY:
            case SWITCH: case SYNCHRONIZED: case RETURN: case THROW: case BREAK:
            case CONTINUE: case SEMI: case ELSE: case FINALLY: case CATCH: 
            case PARDO:
                stats.append(statement());
                break;
            case MONKEYS_AT:
            case FINAL: {
                String dc = S.docComment();
                JCModifiers mods = modifiersOpt();
                if (S.token() == INTERFACE ||
                    S.token() == CLASS ||
                    allowEnums && S.token() == ENUM) {
                    stats.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                } else {
                    JRGRefPerm refPerm = refPermOpt();
                    JCExpression t = type();
                    stats.appendList(variableDeclarators(mods, refPerm, t, false,
                                                         new ListBuffer<JCStatement>()));
                    // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                    storeEnd(stats.elems.last(), S.endPos());
                    accept(SEMI);
                }
                break;
            }
            case REGION: {
        	String dc = S.docComment();
        	pos = S.pos();
                JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
        	stats.appendList(regionDeclarations(pos, mods, dc,
        					    new ListBuffer<JCStatement>()));	
        	storeEnd(stats.elems.last(), S.endPos());
        	break;
            }
            case REFGROUP: {
        	String dc = S.docComment();
        	pos = S.pos();
        	stats.appendList(refGroupDecls(pos, dc));
        	storeEnd(stats.elems.last(), S.endPos());
        	break;        	
            }
            case ABSTRACT: case STRICTFP: {
                String dc = S.docComment();
                JCModifiers mods = modifiersOpt();
                stats.append(classOrInterfaceOrEnumDeclaration(mods, dc));
                break;
            }
            case INTERFACE:
            case CLASS:
                stats.append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                                                               S.docComment()));
                break;
            case ARRAYCLASS:
        	stats.append(arrayDeclaration(modifiersOpt(), S.docComment()));
        	break;
            case ENUM:
            case ASSERT:
                if (allowEnums && S.token() == ENUM) {
                    log.error(S.pos(), "local.enum");
                    stats.
                        append(classOrInterfaceOrEnumDeclaration(modifiersOpt(),
                                                                 S.docComment()));
                    break;
                } else if (allowAsserts && S.token() == ASSERT) {
                    stats.append(statement());
                    break;
                }
                /* fall through to default */
            default:
                Name name = S.name();
                JRGRefPerm refPerm = refPermOpt();
                JCExpression t = term(EXPR | TYPE);
                if (S.token() == COLON && t.getTag() == JCTree.IDENT) {
                    if (!refPerm.isShared()) {
                	syntaxError(S.pos(), "expected", "variable declaration");
                    }
                    S.nextToken();
                    JCStatement stat = statement();
                    stats.append(F.at(pos).Labelled(name, stat));
                } else if ((lastmode & TYPE) != 0 &&
                           (S.token() == IDENTIFIER ||
                            S.token() == ASSERT ||
                            S.token() == ENUM)) {
                    pos = S.pos();
                    JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
                    F.at(pos);
                    stats.appendList(variableDeclarators(mods, refPerm, t, true,
                                                         new ListBuffer<JCStatement>()));
                    // A "LocalVariableDeclarationStatement" subsumes the terminating semicolon
                    storeEnd(stats.elems.last(), S.endPos());
                    accept(SEMI);
                } else {
                    if (!refPerm.isShared()) {
                	log.error(refPerm.pos(), "spurious.ref.perm");
                    }
                    // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                    stats.append(to(F.at(pos).Exec(checkExprStat(t))));
                    accept(SEMI);
                }
            }

            // error recovery
            if (S.pos() == lastErrPos)
                return stats.toList();
            if (S.pos() <= errorEndPos) {
                skip(false, true, true, true);
                lastErrPos = S.pos();
            }

            // ensure no dangling /** @deprecated */ active
            S.resetDeprecatedFlag();
        }
    }

    /** Statement =
     *       Block
     *     | PARDO Block
     *     | IF ParExpression Statement [ELSE Statement]
     *     | FOR "(" ForInitOpt ";" [Expression] ";" ForUpdateOpt ")" Statement
     *     | FOR "(" FormalParameter ":" Expression ")" Statement
     *     | JRGForLoop
     *     | WHILE ParExpression Statement
     *     | DO Statement WHILE ParExpression ";"
     *     | TRY Block ( Catches | [Catches] FinallyPart )
     *     | SWITCH [ "type" ] ParExpression "{" SwitchBlockStatementGroups "}"
     *     | SYNCHRONIZED ParExpression Block
     *     | RETURN [Expression] ";"
     *     | THROW Expression ";"
     *     | BREAK [Ident] ";"
     *     | CONTINUE [Ident] ";"
     *     | ASSERT Expression [ ":" Expression ] ";"
     *     | ";"
     *     | ExpressionStatement
     *     | Ident ":" Statement
     */
    @SuppressWarnings("fallthrough")
    public JCStatement statement() {
        int pos = S.pos();
        switch (S.token()) {
        case LBRACE:
            return block();
        case IF: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement thenpart = statement();
            JCStatement elsepart = null;
            if (S.token() == ELSE) {
                S.nextToken();
                elsepart = statement();
            }
            return F.at(pos).If(cond, thenpart, elsepart);
        }
        case FOR: {
            S.nextToken();
            if (tokenIsIdent("each")) {
        	S.nextToken();
        	return JRGForLoop(pos);
            }
            accept(LPAREN);
            List<JCStatement> inits = S.token() == SEMI ? List.<JCStatement>nil() : forInit();
            if (inits.length() == 1 &&
                inits.head.getTag() == JCTree.VARDEF &&
                ((JCVariableDecl) inits.head).init == null &&
                S.token() == COLON) {
                checkForeach();
                JCVariableDecl var = (JCVariableDecl)inits.head;
                accept(COLON);
                JCExpression expr = expression();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForeachLoop(var, expr, body);
            } else {
                accept(SEMI);
                JCExpression cond = S.token() == SEMI ? null : expression();
                accept(SEMI);
                List<JCExpressionStatement> steps = S.token() == RPAREN ? List.<JCExpressionStatement>nil() : forUpdate();
                accept(RPAREN);
                JCStatement body = statement();
                return F.at(pos).ForLoop(inits, cond, steps, body);
            }
        }
        case WHILE: {
            S.nextToken();
            JCExpression cond = parExpression();
            JCStatement body = statement();
            return F.at(pos).WhileLoop(cond, body);
        }
        case DO: {
            S.nextToken();
            JCStatement body = statement();
            accept(WHILE);
            JCExpression cond = parExpression();
            JCDoWhileLoop t = to(F.at(pos).DoLoop(body, cond));
            accept(SEMI);
            return t;
        }
        case TRY: {
            S.nextToken();
            JCBlock body = block();
            ListBuffer<JCCatch> catchers = new ListBuffer<JCCatch>();
            JCBlock finalizer = null;
            if (S.token() == CATCH || S.token() == FINALLY) {
                while (S.token() == CATCH) catchers.append(catchClause());
                if (S.token() == FINALLY) {
                    S.nextToken();
                    finalizer = block();
                }
            } else {
                log.error(pos, "try.without.catch.or.finally");
            }
            return F.at(pos).Try(body, catchers.toList(), finalizer);
        }
        case SWITCH: {
            S.nextToken();
            JCSwitch t = null;
            JCExpression selector = parExpression();
            if (S.token() == INSTANCEOF) {
        	if (!(selector instanceof JCIdent)) {
                    syntaxError(selector.pos, "expected", keywords.token2string(IDENTIFIER));
        	}
        	S.nextToken();
        	accept(LBRACE);
        	List<JCCase> cases = switchBlockStatementGroups(true);
        	t = to(F.at(pos).InstanceOfSwitch(selector, cases));        	
            } else {
        	accept(LBRACE);
        	List<JCCase> cases = switchBlockStatementGroups(false);
        	t = to(F.at(pos).Switch(selector, cases));
            }
            accept(RBRACE);
            return t;
        }
        case SYNCHRONIZED: {
            S.nextToken();
            JCExpression lock = parExpression();
            JCBlock body = block();
            return F.at(pos).Synchronized(lock, body);
        }
        case PARDO: {
            S.nextToken();
            JCBlock body = block();
            return F.at(pos).Pardo(body);
        }
        case RETURN: {
            S.nextToken();
            JCExpression result = S.token() == SEMI ? null : expression();
            JCReturn t = to(F.at(pos).Return(result));
            accept(SEMI);
            return t;
        }
        case THROW: {
            S.nextToken();
            JCExpression exc = expression();
            JCThrow t = to(F.at(pos).Throw(exc));
            accept(SEMI);
            return t;
        }
        case BREAK: {
            S.nextToken();
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? name() : null;
            JCBreak t = to(F.at(pos).Break(label));
            accept(SEMI);
            return t;
        }
        case CONTINUE: {
            S.nextToken();
            Name label = (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM) ? name() : null;
            JCContinue t =  to(F.at(pos).Continue(label));
            accept(SEMI);
            return t;
        }
        case SEMI:
            S.nextToken();
            return toP(F.at(pos).Skip());
        case ELSE:
            return toP(F.Exec(syntaxError("else.without.if")));
        case FINALLY:
            return toP(F.Exec(syntaxError("finally.without.try")));
        case CATCH:
            return toP(F.Exec(syntaxError("catch.without.try")));
        case ASSERT: {
            if (allowAsserts && S.token() == ASSERT) {
                S.nextToken();
                JCExpression assertion = expression();
                JCExpression message = null;
                if (S.token() == COLON) {
                    S.nextToken();
                    message = expression();
                }
                JCAssert t = to(F.at(pos).Assert(assertion, message));
                accept(SEMI);
                return t;
            }
            /* else fall through to default case */
        }
        case ENUM:
        default:
            Name name = S.name();
            JCExpression expr = expression();
            if (S.token() == COLON && expr.getTag() == JCTree.IDENT) {
                S.nextToken();
                JCStatement stat = statement();
                return F.at(pos).Labelled(name, stat);
            } else {
                // This Exec is an "ExpressionStatement"; it subsumes the terminating semicolon
                JCExpressionStatement stat = to(F.at(pos).Exec(checkExprStat(expr)));
                accept(SEMI);
                return stat;
            }
        }
    }

    /**
     * JRGForLoop = FOR "each" Ident "in" Expression [PARDO] Statement
     */
    
    JRGForLoop JRGForLoop(int pos) {
	Name indexVarName = name();
        JCModifiers mods = F.at(Position.NOPOS).Modifiers(0);
        JCExpression type =
        	to(F.at(S.pos()).TypeIdent(typetag(INT)));
	JCVariableDecl indexVar = 
		toP(F.at(pos).VarDef(mods, indexVarName, null, type, null, null));
	acceptIdent("in");
	JCExpression array = expression();
	boolean isParallel = false;
	if (S.token() == PARDO) {
	    S.nextToken();
	    isParallel = true;
	}
        JCStatement body = statement();
        return F.at(pos).JRGForLoop(indexVar, array, body, isParallel);
    }
    
    /** CatchClause     = CATCH "(" FormalParameter ")" Block
     */
    JCCatch catchClause() {
        int pos = S.pos();
        accept(CATCH);
        accept(LPAREN);
        JCVariableDecl formal =
            variableDeclaratorId(optFinal(Flags.PARAMETER),
                                 null, qualident());
        accept(RPAREN);
        JCBlock body = block();
        return F.at(pos).Catch(formal, body);
    }

    /** SwitchBlockStatementGroups = { SwitchBlockStatementGroup }
     *  SwitchBlockStatementGroup = SwitchLabel BlockStatements
     *  SwitchLabel = CASE ConstantExpression ":" | DEFAULT ":"
     *  TypeSwitchBlockStatementGroups = { TypeSwitchBlockStatementGroup }
     *  TypeSwitchBlockStatementGroup = TypeSwitchLabel BlockStatements
     *  TypeSwitchLabel = CASE Type ":" | DEFAULT ":"
     */
    List<JCCase> switchBlockStatementGroups(boolean isTypeSwitch) {
        ListBuffer<JCCase> cases = new ListBuffer<JCCase>();
        while (true) {
            int pos = S.pos();
            switch (S.token()) {
            case CASE: {
                S.nextToken();
                JCExpression pat = expression();
                accept(COLON);
                List<JCStatement> stats = blockStatements();
                JCCase c = F.at(pos).Case(pat, stats);
                if (stats.isEmpty())
                    storeEnd(c, S.prevEndPos());
                cases.append(c);
                break;
            }
            case DEFAULT: {
                S.nextToken();
                accept(COLON);
                List<JCStatement> stats = blockStatements();
                JCCase c = F.at(pos).Case(null, stats);
                if (stats.isEmpty())
                    storeEnd(c, S.prevEndPos());
                cases.append(c);
                break;
            }
            case RBRACE: case EOF:
                return cases.toList();
            default:
                S.nextToken(); // to ensure progress
                syntaxError(pos, "expected3",
                    keywords.token2string(CASE),
                    keywords.token2string(DEFAULT),
                    keywords.token2string(RBRACE));
            }
        }
    }

    /** MoreStatementExpressions = { COMMA StatementExpression }
     */
    <T extends ListBuffer<? super JCExpressionStatement>> T moreStatementExpressions(int pos,
                                                                    JCExpression first,
                                                                    T stats) {
        // This Exec is a "StatementExpression"; it subsumes no terminating token
        stats.append(toP(F.at(pos).Exec(checkExprStat(first))));
        while (S.token() == COMMA) {
            S.nextToken();
            pos = S.pos();
            JCExpression t = expression();
            // This Exec is a "StatementExpression"; it subsumes no terminating token
            stats.append(toP(F.at(pos).Exec(checkExprStat(t))));
        }
        return stats;
    }

    /** ForInit = StatementExpression MoreStatementExpressions
     *           |  { FINAL | '@' Annotation } Type VariableDeclarators
     */
    List<JCStatement> forInit() {
        ListBuffer<JCStatement> stats = lb();
        int pos = S.pos();
        if (S.token() == FINAL || S.token() == MONKEYS_AT) {
            return variableDeclarators(optFinal(0), refPermOpt(), type(), false, stats).toList();
        } else {
            JRGRefPerm refPerm = refPermOpt();
            JCExpression t = term(EXPR | TYPE);
            if ((lastmode & TYPE) != 0 &&
                (S.token() == IDENTIFIER || S.token() == ASSERT || S.token() == ENUM)) {
        	return variableDeclarators(modifiersOpt(), refPerm, t, 
        		false, stats).toList();
            }
            else
                return moreStatementExpressions(pos, t, stats).toList();
        }
    }

    /** ForUpdate = StatementExpression MoreStatementExpressions
     */
    List<JCExpressionStatement> forUpdate() {
        return moreStatementExpressions(S.pos(),
                                        expression(),
                                        new ListBuffer<JCExpressionStatement>()).toList();
    }

    /** AnnotationsOpt = { '@' Annotation }
     */
    List<JCAnnotation> annotationsOpt() {
        if (S.token() != MONKEYS_AT) return List.nil(); // optimization
        ListBuffer<JCAnnotation> buf = new ListBuffer<JCAnnotation>();
        while (S.token() == MONKEYS_AT) {
            int pos = S.pos();
            S.nextToken();
            buf.append(annotation(pos));
        }
        return buf.toList();
    }

    /** ModifiersOpt = { Modifier }
     *  Modifier = PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL
     *           | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | "@"
     *           | "@" Annotation
     */
    JCModifiers modifiersOpt() {
        return modifiersOpt(null);
    }
    JCModifiers modifiersOpt(JCModifiers partial) {
        long flags = (partial == null) ? 0 : partial.flags;
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        ListBuffer<JCAnnotation> annotations = new ListBuffer<JCAnnotation>();
        if (partial != null) annotations.appendList(partial.annotations);
        int pos = S.pos();
        int lastPos = Position.NOPOS;
    loop:
        while (true) {
            long flag;
            switch (S.token()) {
            case PRIVATE     : flag = Flags.PRIVATE; break;
            case PROTECTED   : flag = Flags.PROTECTED; break;
            case PUBLIC      : flag = Flags.PUBLIC; break;
            case STATIC      : flag = Flags.STATIC; break;
            case TRANSIENT   : flag = Flags.TRANSIENT; break;
            case FINAL       : flag = Flags.FINAL; break;
            case ABSTRACT    : flag = Flags.ABSTRACT; break;
            case NATIVE      : flag = Flags.NATIVE; break;
            case VOLATILE    : flag = Flags.VOLATILE; break;
            case SYNCHRONIZED: flag = Flags.SYNCHRONIZED; break;
            case STRICTFP    : flag = Flags.STRICTFP; break;
            case MONKEYS_AT  : flag = Flags.ANNOTATION; break;
            default: break loop;
            }
            if ((flags & flag) != 0) log.error(S.pos(), "repeated.modifier");
            lastPos = S.pos();
            S.nextToken();
            if (flag == Flags.ANNOTATION) {
                checkAnnotations();
                if (S.token() != INTERFACE) {
                JCAnnotation ann = annotation(lastPos);
                // if first modifier is an annotation, set pos to annotation's.
                if (flags == 0 && annotations.isEmpty())
                    pos = ann.pos;
                annotations.append(ann);
                lastPos = ann.pos;
                    flag = 0;
                }
            }
            flags |= flag;
        }
        switch (S.token()) {
        case ENUM: flags |= Flags.ENUM; break;
        case INTERFACE: flags |= Flags.INTERFACE; break;
        case ARRAYCLASS: flags |= Flags.ARRAYCLASS; break;
        default: break;
        }

        /* A modifiers tree with no modifier tokens or annotations
         * has no text position. */
        if (flags == 0 && annotations.isEmpty())
            pos = Position.NOPOS;

        JCModifiers mods = F.at(pos).Modifiers(flags, annotations.toList());
        if (pos != Position.NOPOS)
            storeEnd(mods, S.prevEndPos());
        return mods;
    }

    /** Annotation              = "@" Qualident [ "(" AnnotationFieldValues ")" ]
     * @param pos position of "@" token
     */
    JCAnnotation annotation(int pos) {
        // accept(AT); // AT consumed by caller
        checkAnnotations();
        JCTree ident = qualident();
        List<JCExpression> fieldValues = annotationFieldValuesOpt();
        JCAnnotation ann = F.at(pos).Annotation(ident, fieldValues);
        storeEnd(ann, S.prevEndPos());
        return ann;
    }

    List<JCExpression> annotationFieldValuesOpt() {
        return (S.token() == LPAREN) ? annotationFieldValues() : List.<JCExpression>nil();
    }

    /** AnnotationFieldValues   = "(" [ AnnotationFieldValue { "," AnnotationFieldValue } ] ")" */
    List<JCExpression> annotationFieldValues() {
        accept(LPAREN);
        ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
        if (S.token() != RPAREN) {
            buf.append(annotationFieldValue());
            while (S.token() == COMMA) {
                S.nextToken();
                buf.append(annotationFieldValue());
            }
        }
        accept(RPAREN);
        return buf.toList();
    }

    /** AnnotationFieldValue    = AnnotationValue
     *                          | Identifier "=" AnnotationValue
     */
    JCExpression annotationFieldValue() {
        if (S.token() == IDENTIFIER) {
            mode = EXPR;
            JCExpression t1 = term1();
            if (t1.getTag() == JCTree.IDENT && S.token() == EQ) {
                int pos = S.pos();
                accept(EQ);
                return toP(F.at(pos).Assign(t1, annotationValue()));
            } else {
                return t1;
            }
        }
        return annotationValue();
    }

    /* AnnotationValue          = ConditionalExpression
     *                          | Annotation
     *                          | "{" [ AnnotationValue { "," AnnotationValue } ] "}"
     */
    JCExpression annotationValue() {
        int pos;
        switch (S.token()) {
        case MONKEYS_AT:
            pos = S.pos();
            S.nextToken();
            return annotation(pos);
        case LBRACE:
            pos = S.pos();
            accept(LBRACE);
            ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
            if (S.token() != RBRACE) {
                buf.append(annotationValue());
                while (S.token() == COMMA) {
                    S.nextToken();
                    if (S.token() == RPAREN) break;
                    buf.append(annotationValue());
                }
            }
            accept(RBRACE);
            return toP(F.at(pos).NewArray(null, List.<JCExpression>nil(), buf.toList()));
        default:
            mode = EXPR;
            return term1();
        }
    }

    /** VariableDeclarators = VariableDeclarator { "," VariableDeclarator }
     */
    public <T extends ListBuffer<? super JCVariableDecl>> T variableDeclarators(JCModifiers mods,
	    								 JRGRefPerm refPerm,
                                                                         JCExpression type,
                                                                         boolean regionOK,
                                                                         T vdefs)
    {
        return variableDeclaratorsRest(S.pos(), mods, refPerm, type, 
        	name(), false, regionOK, null, vdefs);
    }

    /** VariableDeclaratorsRest = VariableDeclaratorRest { "," VariableDeclarator }
     *  ConstantDeclaratorsRest = ConstantDeclaratorRest { "," ConstantDeclarator }
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    <T extends ListBuffer<? super JCVariableDecl>> T variableDeclaratorsRest(int pos,
                                                                     JCModifiers mods,
                                                                     JRGRefPerm refPerm,
                                                                     JCExpression type,
                                                                     Name name,
                                                                     boolean reqInit,
                                                                     boolean regionOK,
                                                                     String dc,
                                                                     T vdefs)
    {
        vdefs.append(variableDeclaratorRest(pos, mods, refPerm, type, name, reqInit, regionOK, dc));
        while (S.token() == COMMA) {
            // All but last of multiple declarators subsume a comma
            storeEnd((JCTree)vdefs.elems.last(), S.endPos());
            S.nextToken();
            vdefs.append(variableDeclarator(mods, refPerm, type, reqInit, regionOK, dc));
        }
        return vdefs;
    }

    /** VariableDeclarator = Ident VariableDeclaratorRest
     *  ConstantDeclarator = Ident ConstantDeclaratorRest
     */
    JCVariableDecl variableDeclarator(JCModifiers mods, JRGRefPerm refPerm, JCExpression type, 
	    boolean reqInit, boolean regionOK, String dc) {
        return variableDeclaratorRest(S.pos(), mods, refPerm, type, name(), reqInit, regionOK, dc);
    }

    /** VariableDeclaratorRest = BracketsOpt RegionOpt ["=" VariableInitializer]
     *  ConstantDeclaratorRest = BracketsOpt RegionOpt "=" VariableInitializer  
     *
     *  @param reqInit  Is an initializer always required?
     *  @param dc       The documentation comment for the variable declarations, or null.
     */
    JCVariableDecl variableDeclaratorRest(int pos, JCModifiers mods, 
	    				  JRGRefPerm refPerm, JCExpression type, 
	    				  Name name, boolean reqInit, 
	    				  boolean regionOK, String dc) {
        type = bracketsOpt(type);
        JCTree.DPJRegionPathList rpl = null;
        if (regionOK) rpl = regionOpt();
        JCExpression init = null;
        if (S.token() == EQ) {
            S.nextToken();
            init = variableInitializer();
        }
        else if (reqInit) syntaxError(S.pos(), "expected", keywords.token2string(EQ));
        JCVariableDecl result =
            toP(F.at(pos).VarDef(mods, name, refPerm, type, rpl, init));
        attach(result, dc);
        return result;
    }

    /** VariableDeclaratorId = Ident BracketsOpt RegionOpt
     */
    JCVariableDecl variableDeclaratorId(JCModifiers mods, JRGRefPerm refPerm,
	    JCExpression type) {
        int pos = S.pos();
        Name name = name();
        if ((mods.flags & Flags.VARARGS) == 0)
            type = bracketsOpt(type);
        JCTree.DPJRegionPathList rpl = regionOpt();
        return toP(F.at(pos).VarDef(mods, name, refPerm, type, rpl, null));
    }

    /** RegionOpt = { "in" RegionPathList }
     */
    private JCTree.DPJRegionPathList regionOpt() {
	if (S.token() == IDENTIFIER) {
	    Name name = name();
	    if (name.compareTo(names.in) != 0) {
		reportSyntaxError(S.prevEndPos(), "expected", "in");
	    }
	    return rpl();
	} else {
            return null;
        }
    }
    
    private boolean badRPLOK = false;
    private boolean goodRPL = false;
    private JCTree.DPJRegionPathList rplOrExpr() {
	badRPLOK = true;
	goodRPL = true;
	JCTree.DPJRegionPathList result = rpl();
	badRPLOK = false;
	return result;
    }

    /** If we see something that is unambiguously an RPL element,
     *  and not a type, then construct and return the appropriate 
     *  AST node.  Otherwise, return null.
     */
    private DPJRegionPathListElt nonTypeRPLElement(boolean thisOK) {
	DPJRegionPathListElt elt = null;
	int pos = S.pos();
        switch (S.token()) {
        case STAR:
            elt = toP(F.at(pos).RegionPathListElt(null, DPJRegionPathListElt.STAR));
            S.nextToken();
            break;
        }
	return elt;
    }

    /** We are looking for an RPL element and there can't be a type here.
     */
    private DPJRegionPathListElt rplElement() {
        JCTree.DPJRegionPathListElt elt = nonTypeRPLElement(false);
        if (elt == null) {
            if (S.token() == IDENTIFIER) {
        	int pos = S.pos();
        	JCExpression regionName = qualident();
        	elt = toP(F.at(pos).RegionPathListElt(regionName, DPJRegionPathListElt.NAME));
            } else {
        	syntaxError("illegal.rpl");
            }
        }
	return elt;
    }
    
    /** Parse an RPL given its first element already parsed.
     */
    private DPJRegionPathList rpl(DPJRegionPathListElt elt) {
        ListBuffer<JCTree.DPJRegionPathListElt> elts = lb();
        int pos = S.pos();
        elts.append(elt);
        while (S.token() == COLON) {
            S.nextToken();
            elt = rplElement();
            if (elt == null) break; // Error condition
            elts.append(elt);
        }
        return toP(F.at(pos).RegionPathList(elts.toList()));
    }
    
    /** Parse something that could be a type or an RPL, we don't know which
     *  at parse time
     */
    private JCExpression typeOrRPL() {
	// Try to get a non-type RPL element.  If we got it, then construct the
	// rest of the RPL and return it as the result.
	DPJRegionPathListElt elt = nonTypeRPLElement(true);
	if (elt != null) return rpl(elt);
	
	// Otherwise, parse a type.  If the next token is not a colon, return
	// that type as the result.
	int pos = S.pos();
	JCExpression typeOrRPLElt = type();
	if (S.token() != COLON) return typeOrRPLElt;
	
	// Otherwise, pack the "type" (really an RPL element) into an RPL element,
	// construct the rest of the RPL, and return it as the result.
	elt = F.at(pos).RegionPathListElt(typeOrRPLElt, DPJRegionPathListElt.NAME);
	return rpl(elt);
    }
    
    /** RPL = [ REGION ] RPLElement { ":" RPLElement }
     *  RPLElement = Ident { "." Ident } | "*"
     */
    private JCTree.DPJRegionPathList rpl() {
	if (S.token() == REGION) S.nextToken(); // optional 'region' marker
	ListBuffer<JCTree.DPJRegionPathListElt> elts = lb();
        JCTree.DPJRegionPathListElt elt = null;
        int rplPos = S.pos();
        boolean done = false;
        do {
            int pos = S.pos();
            switch (S.token()) {
            case IDENTIFIER:
        	JCExpression regionName = qualident();
        	elt = toP(F.at(pos).RegionPathListElt(regionName, DPJRegionPathListElt.NAME));
        	elts.append(elt);
        	break;
            case STAR:
        	elts.append(F.at(pos).RegionPathListElt(null, DPJRegionPathListElt.STAR));
        	S.nextToken();
        	break;
            default:
            	break;
            }
            if (S.token() == COLON) {
        	accept(COLON);
            } else {
        	done = true;
            }
        } while (!done);
        if (elts.size() == 0) {
            if (!badRPLOK)
        	syntaxError(S.pos(), "empty.rpl");
            else
        	goodRPL = false;
        }
        return toP(F.at(rplPos).RegionPathList(elts.toList()));
    }
    
    /** CompilationUnit = [ { "@" Annotation } PACKAGE Qualident ";"] {ImportDeclaration} {TypeDeclaration}
     */
    public JCTree.JCCompilationUnit compilationUnit() {
        int pos = S.pos();
        JCExpression pid = null;
        String dc = S.docComment();
        JCModifiers mods = null;
        List<JCAnnotation> packageAnnotations = List.nil();
        if (S.token() == MONKEYS_AT)
            mods = modifiersOpt();

        if (S.token() == PACKAGE) {
            if (mods != null) {
                checkNoMods(mods.flags);
                packageAnnotations = mods.annotations;
                mods = null;
            }
            S.nextToken();
            pid = qualident();
            accept(SEMI);
        }
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
       boolean checkForImports = true;
        while (S.token() != EOF) {
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(checkForImports, false, false, false);
                if (S.token() == EOF)
                    break;
            }
            if (checkForImports && mods == null && S.token() == IMPORT) {
                defs.append(importDeclaration());
            } else {
                JCTree def = typeDeclaration(mods);
                if (def instanceof JCExpressionStatement)
                    def = ((JCExpressionStatement)def).expr;
                defs.append(def);
                if (def instanceof JCClassDecl)
                    checkForImports = false;
                mods = null;
            }
        }
        JCTree.JCCompilationUnit toplevel = F.at(pos).TopLevel(packageAnnotations, pid, defs.toList());
        attach(toplevel, dc);
        if (defs.elems.isEmpty())
            storeEnd(toplevel, S.prevEndPos());
        if (keepDocComments) toplevel.docComments = docComments;
        return toplevel;
    }

    /** ImportDeclaration = IMPORT [ STATIC ] Ident { "." Ident } [ "." "*" ] ";"
     */
    JCTree importDeclaration() {
        int pos = S.pos();
        S.nextToken();
        boolean importStatic = false;
        if (S.token() == STATIC) {
            checkStaticImports();
            importStatic = true;
            S.nextToken();
        }
        JCExpression pid = ident();
        do {
            int pos1 = S.pos();
            accept(DOT);
            if (S.token() == STAR) {
                pid = to(F.at(pos1).Select(pid, names.asterisk));
                S.nextToken();
                break;
            } else {
                pid = toP(F.at(pos1).Select(pid, name()));
            }
        } while (S.token() == DOT);
        accept(SEMI);
        return toP(F.at(pos).Import(pid, importStatic));
    }

    /** TypeDeclaration = ClassOrArrayClassOrInterfaceOrEnumDeclaration ";"
     */
    JCTree typeDeclaration(JCModifiers mods) {
        int pos = S.pos();
        if (mods == null && S.token() == SEMI) {
            S.nextToken();
            return toP(F.at(pos).Skip());
        } else {
            String dc = S.docComment();
            return classOrInterfaceOrEnumDeclaration(modifiersOpt(mods), dc);
        }
    }

    /** ClassOrInterfaceOrEnumDeclaration = ModifiersOpt
     *           (ClassDeclaration | InterfaceDeclaration | EnumDeclaration)
     *  @param mods     Any modifiers starting the class or interface declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCStatement classOrInterfaceOrEnumDeclaration(JCModifiers mods, String dc) {
        if (S.token() == ARRAYCLASS) {
            return arrayDeclaration(modifiersOpt(mods), dc);
        }
        else if (S.token() == CLASS) {
            return classDeclaration(mods, dc);
        } else if (S.token() == INTERFACE) {
            return interfaceDeclaration(mods, dc);
        } else if (allowEnums) {
            if (S.token() == ENUM) {
                return enumDeclaration(mods, dc);
            } else {
                int pos = S.pos();
                List<JCTree> errs;
                if (S.token() == IDENTIFIER) {
                    errs = List.<JCTree>of(mods, ident());
                    setErrorEndPos(S.pos());
                } else {
                    errs = List.<JCTree>of(mods);
                }
                return toP(F.Exec(syntaxError(pos, errs, "expected3",
                                              keywords.token2string(CLASS),
                                              keywords.token2string(INTERFACE),
                                              keywords.token2string(ENUM))));
            }
        } else {
            if (S.token() == ENUM) {
                log.error(S.pos(), "enums.not.supported.in.source", source.name);
                allowEnums = true;
                return enumDeclaration(mods, dc);
            }
            int pos = S.pos();
            List<JCTree> errs;
            if (S.token() == IDENTIFIER) {
                errs = List.<JCTree>of(mods, ident());
                setErrorEndPos(S.pos());
            } else {
                errs = List.<JCTree>of(mods);
            }
            return toP(F.Exec(syntaxError(pos, errs, "expected2",
                                          keywords.token2string(CLASS),
                                          keywords.token2string(INTERFACE))));
        }
    }

    /** ClassDeclaration = CLASS Ident ParametersOpt
     *                     [EXTENDS Type]
     *                     [IMPLEMENTS TypeList] ClassBody
     *  @param mods     The modifiers starting the class declaration
     *  @param dc       The documentation comment for the class, or null.
     */
    JCClassDecl classDeclaration(JCModifiers mods, String dc) {
        int pos = S.pos();
        accept(CLASS);
        Name name = name();

        Pair<List<JCTypeParameter>,JRGParamInfo> params = 
            parametersOpt();
        JRGParamInfo dpjParamInfo = params.snd;
        List<JCTypeParameter> typarams = params.fst;

        JCTree extending = null;
        if (S.token() == EXTENDS) {
            S.nextToken();
            extending = type();
        }
        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }
        List<JCTree> defs = classOrInterfaceBody(name, false);
        JCClassDecl result = toP(F.at(pos).ClassDef(
            mods, name, dpjParamInfo, typarams, extending, 
            implementing, defs));
        attach(result, dc);
        return result;
    }

    /** InterfaceDeclaration = INTERFACE Ident ParametersOpt
     *                         [EXTENDS TypeList] InterfaceBody
     *  @param mods    The modifiers starting the interface declaration
     *  @param dc      The documentation comment for the interface, or null.
     */
    JCClassDecl interfaceDeclaration(JCModifiers mods, String dc) {
        int pos = S.pos();
        accept(INTERFACE);
        Name name = name();

        Pair<List<JCTypeParameter>,JRGParamInfo> params = parametersOpt();
        List<JCTypeParameter> typarams = params.fst;
        JRGParamInfo dpjParamInfo = params.snd;

        List<JCExpression> extending = List.nil();
        if (S.token() == EXTENDS) {
            S.nextToken();
            extending = typeList();
        }
        List<JCTree> defs = classOrInterfaceBody(name, true);
        JCClassDecl result = toP(F.at(pos).ClassDef(
            mods, name, dpjParamInfo, typarams, null, extending, defs));
        attach(result, dc);
        return result;
    }

    /** ArrayDeclaration = ARRAYCLASS Ident ParametersOpt ArrayBody
     *  @param mods   The modifiers starting the array declaration
     *  @param dc     The documentation comment for the array, or null
     */
    JCClassDecl arrayDeclaration(JCModifiers mods, String dc) {
        int pos = S.pos();
        accept(ARRAYCLASS);
        Name name = name();

        Pair<List<JCTypeParameter>,JRGParamInfo> params = parametersOpt();
        List<JCTypeParameter> typarams = params.fst;
        JRGParamInfo dpjParamInfo = params.snd;

        List<JCTree> defs = arrayBody(name);
        JCModifiers newMods =
            F.at(mods.pos).Modifiers(mods.flags|Flags.ARRAYCLASS, mods.annotations);
        JCClassDecl result = toP(F.at(pos).
            ClassDef(newMods, name, dpjParamInfo, typarams, null, 
        	    List.<JCExpression>nil(), defs));
        attach(result, dc);
        return result;
    }
    
    /** ArrayBody = { RegionDeclarations }
     *              RefPermOpt Type VariableDeclaratorRest ";"
     * 
     */
    List<JCTree> arrayBody(Name arrayName) {
        accept(LBRACE);
        int flags = Flags.PUBLIC;
        String dc = S.docComment();
        List<JCAnnotation> annotations = annotationsOpt();
        JCModifiers mods = F.at(annotations.isEmpty() ? 
        	Position.NOPOS : S.pos()).Modifiers(flags, annotations);
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        
        // Get region decls, if any
        if (S.token() == REGION) {
            regionDeclarations(S.pos(), mods, dc, defs).toList();
        }

        // Get field decl        
        JRGRefPerm refPerm = refPermOpt();
        JCExpression type = type();
        JCVariableDecl var = 
        	variableDeclaratorRest(S.pos(), mods, refPerm, type, 
        		names.fromString("cell"), false, true, dc);
        storeEnd(var, S.endPos());
        defs.append(var);
        accept(SEMI);
        accept(RBRACE);
        return defs.toList();
    }

    /** RefPermOpt = [ RefPerm ]
     *  RefPerm = UNIQUE "(" Ident ")"
     */
    JRGRefPerm refPermOpt() {
	JCIdent group = null;
	int pos = S.pos();
	if (S.token() == UNIQUE) {
	    S.nextToken();
	    accept(LPAREN);
	    group = ident();
	    accept(RPAREN);
	}
	return toP(F.at(pos).RefPerm(group));
    }
    
    /** EnumDeclaration = ENUM Ident [IMPLEMENTS TypeList] EnumBody
     *  @param mods    The modifiers starting the enum declaration
     *  @param dc       The documentation comment for the enum, or null.
     */
    JCClassDecl enumDeclaration(JCModifiers mods, String dc) {
        int pos = S.pos();
        accept(ENUM);
        Name name = name();

        List<JCExpression> implementing = List.nil();
        if (S.token() == IMPLEMENTS) {
            S.nextToken();
            implementing = typeList();
        }

        List<JCTree> defs = enumBody(name);
        JCModifiers newMods =
            F.at(mods.pos).Modifiers(mods.flags|Flags.ENUM, mods.annotations);
        JCClassDecl result = toP(F.at(pos).
            ClassDef(newMods, name, null,
        	    List.<JCTypeParameter>nil(), null, implementing, defs));
        attach(result, dc);
        return result;
    }

    /** EnumBody = "{" { EnumeratorDeclarationList } [","]
     *                  [ ";" {ClassBodyDeclaration} ] "}"
     */
    List<JCTree> enumBody(Name enumName) {
        accept(LBRACE);
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        if (S.token() == COMMA) {
            S.nextToken();
        } else if (S.token() != RBRACE && S.token() != SEMI) {
            defs.append(enumeratorDeclaration(enumName));
            while (S.token() == COMMA) {
                S.nextToken();
                if (S.token() == RBRACE || S.token() == SEMI) break;
                defs.append(enumeratorDeclaration(enumName));
            }
            if (S.token() != SEMI && S.token() != RBRACE) {
                defs.append(syntaxError(S.pos(), "expected3",
                                keywords.token2string(COMMA),
                                keywords.token2string(RBRACE),
                                keywords.token2string(SEMI)));
                S.nextToken();
            }
        }
        if (S.token() == SEMI) {
            S.nextToken();
            while (S.token() != RBRACE && S.token() != EOF) {
                defs.appendList(classOrInterfaceBodyDeclaration(enumName,
                                                                false));
                if (S.pos() <= errorEndPos) {
                    // error recovery
                   skip(false, true, true, false);
                }
            }
        }
        accept(RBRACE);
        return defs.toList();
    }

    /** EnumeratorDeclaration = AnnotationsOpt [TypeRPLGroupArgs] 
     *  IDENTIFIER [ Arguments ] [ "{" ClassBody "}" ]
     */
    JCTree enumeratorDeclaration(Name enumName) {
        String dc = S.docComment();
        int flags = Flags.PUBLIC|Flags.STATIC|Flags.FINAL|Flags.ENUM;
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        int pos = S.pos();
        List<JCAnnotation> annotations = annotationsOpt();
        JCModifiers mods = F.at(annotations.isEmpty() ? 
        	Position.NOPOS : pos).Modifiers(flags, annotations);
        TypeRPLGroupArgs typeRPLGroupArgs = typeRPLGroupArgsOpt();
        List<JCExpression> typeArgs = typeRPLGroupArgs.typeArgs;
        List<DPJRegionPathList> rplArgs = typeRPLGroupArgs.rplArgs;
        List<JCIdent> groupArgs = typeRPLGroupArgs.groupArgs;
        int identPos = S.pos();
        Name name = name();
        int createPos = S.pos();
        List<JCExpression> args = (S.token() == LPAREN)
            ? arguments() : List.<JCExpression>nil();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
            JCModifiers mods1 = F.at(Position.NOPOS).Modifiers(Flags.ENUM | Flags.STATIC);
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            body = toP(F.at(identPos).AnonymousClassDef(mods1, defs));
        }
        if (args.isEmpty() && body == null)
            createPos = Position.NOPOS;
        JCIdent ident = F.at(Position.NOPOS).Ident(enumName);
        JCNewClass create = F.at(createPos).NewClass(null, rplArgs, typeArgs, 
        	groupArgs, ident, args, body);
        if (createPos != Position.NOPOS)
            storeEnd(create, S.prevEndPos());
        ident = F.at(Position.NOPOS).Ident(enumName);
        JCTree result = toP(F.at(pos).VarDef(mods, name, null, ident, null, create));
        attach(result, dc);
        return result;
    }

    /** TypeList = Type {"," Type}
     */
    List<JCExpression> typeList() {
        ListBuffer<JCExpression> ts = new ListBuffer<JCExpression>();
        ts.append(type());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(type());
        }
        return ts.toList();
    }

    /** ClassBody     = "{" {ClassBodyDeclaration} "}"
     *  InterfaceBody = "{" {InterfaceBodyDeclaration} "}"
     */
    List<JCTree> classOrInterfaceBody(Name className, boolean isInterface) {
        accept(LBRACE);
        if (S.pos() <= errorEndPos) {
            // error recovery
            skip(false, true, false, false);
            if (S.token() == LBRACE)
                S.nextToken();
        }
        ListBuffer<JCTree> defs = new ListBuffer<JCTree>();
        while (S.token() != RBRACE && S.token() != EOF) {
            defs.appendList(classOrInterfaceBodyDeclaration(className, isInterface));
            if (S.pos() <= errorEndPos) {
               // error recovery
               skip(false, true, true, false);
           }
        }
        accept(RBRACE);
        return defs.toList();
    }

    /** ClassBodyDeclaration =
     *      ";"
     *    | [STATIC] Block
     *    | ModifiersOpt
     *      ( RefPermOpt Type Ident
     *        ( VariableDeclaratorsRest ";" | MethodDeclaratorRest )
     *      | VOID Ident MethodDeclaratorRest
     *      | Parameters (RefPermOpt Type | VOID) Ident MethodDeclaratorRest
     *      | Ident ConstructorDeclaratorRest
     *      | Parameters Ident ConstructorDeclaratorRest
     *      | ClassOrInterfaceOrEnumDeclaration
     *      | RegionDeclarations
     *      )
     *  InterfaceBodyDeclaration =
     *      ";"
     *    | ModifiersOpt Type Ident
     *      ( ConstantDeclaratorsRest | InterfaceMethodDeclaratorRest ";" )
     */
    List<JCTree> classOrInterfaceBodyDeclaration(Name className, boolean isInterface) {
        if (S.token() == SEMI) {
            S.nextToken();
            return List.<JCTree>of(F.at(Position.NOPOS).Block(0, List.<JCStatement>nil()));
        } else {
            String dc = S.docComment();
            int pos = S.pos();
            JCModifiers mods = modifiersOpt();
            if (S.token() == CLASS ||
                S.token() == INTERFACE ||
                allowEnums && S.token() == ENUM) {
                return List.<JCTree>of(classOrInterfaceOrEnumDeclaration(mods, dc));
            } else if (S.token() == ARRAYCLASS) {
        	return List.<JCTree>of(arrayDeclaration(mods, dc));
            } else if (S.token() == REGION) {
        	ListBuffer<JCTree> rdefs = new ListBuffer<JCTree>();
        	return regionDeclarations(pos, mods, dc, rdefs).toList();
            } else if (S.token() == LBRACE && !isInterface &&
                       (mods.flags & Flags.StandardFlags & ~Flags.STATIC) == 0 &&
                       mods.annotations.isEmpty()) {
                return List.<JCTree>of(block(pos, mods.flags));
            } else {
                pos = S.pos();
                Pair<List<JCTypeParameter>,JRGParamInfo> params = parametersOpt();
                List<JCTypeParameter> typarams = params.fst;
                JRGParamInfo dpjParamInfo = params.snd;
                // Hack alert:  if there are type arguments but no Modifiers, the start
                // position will be lost unless we set the Modifiers position.  There
                // should be an AST node for type parameters (BugId 5005090).
                if (typarams.length() > 0 && mods.pos == Position.NOPOS) {
                    mods.pos = pos;
                }
                Token token = S.token();
                Name name = S.name();
                pos = S.pos();
                JCExpression type;
                JRGRefPerm refPerm = null;
                boolean isVoid = S.token() == VOID;
                if (isVoid) {
                    type = to(F.at(pos).TypeIdent(TypeTags.VOID));
                    S.nextToken();
                } else {
                    refPerm = refPermOpt();
                    type = type();
                }
                if (S.token() == LPAREN && !isInterface && type.getTag() == JCTree.IDENT) {
                    if (isInterface || name != className)
                        log.error(pos, "invalid.meth.decl.ret.type.req");
                    return List.of(methodDeclaratorRest(
                        pos, mods, null, null, names.init, dpjParamInfo, typarams,
                        isInterface, true, dc));
                } else {
                    pos = S.pos();
                    name = name();
                    if (S.token() == LPAREN) {
                        return List.of(methodDeclaratorRest(
                            pos, mods, refPerm, type, name, dpjParamInfo, typarams,
                            isInterface, isVoid, dc));
                    } else if (!isVoid && typarams.isEmpty() && 
                	    (dpjParamInfo == null || !dpjParamInfo.hasParams())) {
                        List<JCTree> defs =
                            variableDeclaratorsRest(pos, mods, refPerm, type, name, 
                        	    isInterface, true, dc, new ListBuffer<JCTree>()).toList();
                        storeEnd(defs.last(), S.endPos());
                        accept(SEMI);
                        return defs;
                    } else {
                        pos = S.pos();
                        List<JCTree> err = isVoid
                            ? List.<JCTree>of(toP(F.at(pos).MethodDef(mods, name, 
                        	    refPerm, type, dpjParamInfo, typarams,
                                List.<JCVariableDecl>nil(), null, List.<JCExpression>nil(), null, null)))
                            : null;
                        return List.<JCTree>of(syntaxError(S.pos(), err, "expected", keywords.token2string(LPAREN)));
                    }
                }
            }
        }
    }

    /** RegionDeclarations = 
     *           REGION Ident { "," Ident } ";"
     *
     *  @param mods     Any modifiers starting the region declaration
     *  @param dc       The documentation comment for the region, or null.
     */
    // <T extends ListBuffer<? super JCVariableDecl>>
    <T extends ListBuffer<? super DPJRegionDecl>>T regionDeclarations(int pos, JCModifiers mods, 
	    String dc, T rdefs) {
	accept(REGION);
        rdefs.append(F.at(S.pos()).RegionDecl(mods, name()));
        while (S.token() == COMMA) {
            // All but last of multiple declarators subsume a comma
            storeEnd((JCTree)rdefs.elems.last(), S.endPos());
            S.nextToken();
            rdefs.append(F.at(S.pos()).RegionDecl(mods, name()));
        }
        accept(SEMI);
        return rdefs;
    }

    /** RefGroupDeclarations = REFGROUP Ident { "," Ident } ";"
     */
    List<JCStatement> refGroupDecls(int pos, String dc) {
	accept(REFGROUP);
	ListBuffer<JCStatement> lb = ListBuffer.lb();
	lb.append(F.at(S.pos()).RefGroupDecl(name()));
	while (S.token() == COMMA) {
	    storeEnd((JCTree)lb.elems.last(), S.endPos());
	    S.nextToken();
	    lb.append(F.at(S.pos()).RefGroupDecl(name()));
	}
	accept(SEMI);
	return lb.toList();
    }
    
    /**
     * EffectPerms := PURE | [ ReadEffects ] [ WriteEffects ]
     * ReadEffects := READS RPLList
     * WriteEffects := WRITES RPLList
     */
    JRGEffectPerm effectPerms(int pos) {	
        List<JCTree.DPJRegionPathList> readEffects = List.nil();
        List<JCTree.DPJRegionPathList> writeEffects = List.nil();
        if (S.token() == READS) {
            S.nextToken();
            readEffects = RPLList();
        }
        if (S.token() == WRITES) {
            S.nextToken();
            writeEffects = RPLList();
        }
        // FIXME
        JCTree.JRGEffectPerm result = 
            toP(F.at(pos).EffectPerm(null, null));
        return result;
    }
    
    /** MethodDeclaratorRest =
     *      FormalParameters BracketsOpt MethodPermsOpt [THROWS TypeList] 
     *      ( MethodBody | [DEFAULT AnnotationValue] ";")
     *  VoidMethodDeclaratorRest =
     *      FormalParameters MethodPermsOpt [THROWS TypeList] ( MethodBody | ";")
     *  InterfaceMethodDeclaratorRest =
     *      FormalParameters BracketsOpt MethodPermsOpt [THROWS TypeList] ";"
     *  VoidInterfaceMethodDeclaratorRest =
     *      FormalParameters MethodPermsOpt [THROWS TypeList] ";"
     *  ConstructorDeclaratorRest =
     *      "(" FormalParameterListOpt ")" MethodPermsOpt [THROWS TypeList] MethodBody
     */
    JCTree methodDeclaratorRest(int pos,
                              JCModifiers mods,
                              JRGRefPerm resPerm,
                              JCExpression type,
                              Name name,
                              JCTree.JRGParamInfo rgnParamInfo,
                              List<JCTypeParameter> typarams,
                              boolean isInterface, boolean isVoid,
                              String dc) {
        List<JCVariableDecl> params = formalParameters();
        if (!isVoid) type = bracketsOpt(type);
        JRGMethodPerms perms = methodPermsOpt();
        List<JCExpression> thrown = List.nil();
        if (S.token() == THROWS) {
            S.nextToken();
            thrown = qualidentList();
        }
        JCBlock body = null;
        JCExpression defaultValue;
        if (S.token() == LBRACE) {
            body = block();
            defaultValue = null;
        } else {
            if (S.token() == DEFAULT) {
                accept(DEFAULT);
                defaultValue = annotationValue();
            } else {
                defaultValue = null;
            }
            accept(SEMI);
            if (S.pos() <= errorEndPos) {
                // error recovery
                skip(false, true, false, false);
                if (S.token() == LBRACE) {
                    body = block();
                }
            }
        }
        JCMethodDecl result =
            toP(F.at(pos).MethodDef(mods, name, resPerm, type, rgnParamInfo, 
        	    		    typarams, params, perms, thrown,
                                    body, defaultValue));
        ++methodCount;
        attach(result, dc);
        return result;
    }

    /** MethodPermsOpt = RefPermOpt [FRESH IdentList] CopyPermsOpt
     *                   [READS EffectPermList] [WRITES EffectPermList]
     *                   [PRESERVES IdentList]
     */
    JRGMethodPerms methodPermsOpt() {
	int pos = S.pos();
	JRGRefPerm refPerm = refPermOpt();
	List<JCIdent> freshGroups = List.nil();
	if (S.token() == FRESH) {
	    accept(FRESH);
	    freshGroups = identList();
	}
	List<JRGCopyPerm> copyPerms = copyPermsOpt();
	boolean defaultEffectPerms = true;
	List<JRGEffectPerm> readEffectPerms = List.nil();
	if (S.token() == READS) {
	    S.nextToken();
	    defaultEffectPerms = false;
	    readEffectPerms = effectPermList();
	}
	List<JRGEffectPerm> writeEffectPerms = List.nil();
	if (S.token() == WRITES) {
	    S.nextToken();
	    defaultEffectPerms = false;
	    writeEffectPerms = effectPermList();	    
	}
	List<JCIdent> preservedGroups = List.nil();
	if (S.token() == PRESERVES) {
	    accept(PRESERVES);
	    preservedGroups = identList();
	}
	return toP(F.at(pos).MethodPerms(refPerm, freshGroups, copyPerms, 
		defaultEffectPerms, readEffectPerms, writeEffectPerms,
		preservedGroups));
    }
    
    /** CopyPermsOpt = [COPIES DerefSet "to" Ident {"," DerefSet "to" Ident}]
     */
    List<JRGCopyPerm> copyPermsOpt() {
	ListBuffer<JRGCopyPerm> buf = ListBuffer.lb();
	if (S.token() == COPIES) {
	    accept(COPIES);
	    int pos = S.pos();
	    JRGDerefSet derefSet = derefSet();
	    acceptIdent("to");
	    JCIdent group = ident();
	    buf.append(toP(F.at(pos).CopyPerm(derefSet, group)));
	    while (S.token() == COMMA) {
		accept(COMMA);
		pos = S.pos();
		derefSet = derefSet();
		acceptIdent("to");
		group = ident();
		buf.append(toP(F.at(pos).CopyPerm(derefSet, group)));
	    }
	}
	return buf.toList();
    }

    /** EffectPermList = EffectPerm {"," EffectPerm}
     */
    List<JRGEffectPerm> effectPermList() {
	ListBuffer<JRGEffectPerm> buf = ListBuffer.lb();
	buf.append(effectPerm());
	while (S.token() == COMMA) {
	    S.nextToken();
	    buf.append(effectPerm());
	}
	return buf.toList();
    }
    
    /** EffectPerm = RPL ["via" DerefSet]
     */
    JRGEffectPerm effectPerm() {
	int pos = S.pos();
	DPJRegionPathList rpl = rpl();
	JRGDerefSet derefSet = null;
	if (tokenIsIdent("via")) {	    
	    S.nextToken();
	    derefSet = derefSet();
	}
	return toP(F.at(pos).EffectPerm(rpl, derefSet));
    }    
    
    /** DerefSet = Expression3 [ ELLIPSIS Ident ]
     */
    JRGDerefSet derefSet() {
	int pos = S.pos();
        mode = EXPR;
	JCExpression root = term3();
	JCIdent group = null;
	if (S.token() == ELLIPSIS) {
	    accept(ELLIPSIS);
	    group = ident();
	}
	return toP(F.at(pos).DerefSet(root, group));	
    }
    
    /** IdentList = Ident {"," Ident}
     */
    List<JCIdent> identList() {
	ListBuffer<JCIdent> buf = ListBuffer.lb();
	buf.append(ident());
	while (S.token() == COMMA) {
	    S.nextToken();
	    buf.append(ident());
	}
	return buf.toList();
    }
    
    /** QualidentList = Qualident {"," Qualident}
     */
    List<JCExpression> qualidentList() {
        ListBuffer<JCExpression> ts = new ListBuffer<JCExpression>();
        ts.append(qualident());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(qualident());
        }
        return ts.toList();
    }

    /** RPLList = RPL {"," RPL}
     */
    List<JCTree.DPJRegionPathList> RPLList() {
	ListBuffer<JCTree.DPJRegionPathList> ts = 
            new ListBuffer<JCTree.DPJRegionPathList>();
        ts.append(rpl());
        while (S.token() == COMMA) {
            S.nextToken();
            ts.append(rpl());
        }        
        return ts.toList();
    }
    
    /** ParametersOpt := [ "<" Parameters ">" ]
     *  Parameters := TypeParams | RPLGroupParamsConstraints | 
     *                TypeParams (",") RPLGroupParamsConstraints
     *  TypeParams := [ "type" ] Ident { "," [ "type" ] Ident }
     *  RPLGroupParamsConstraints := RPLGroupParams [ "|" RPLConstraints ]
     *  RPLGroupParams := RPLParams | GroupParams | RPLParams (",") GroupParams
     *  RPLParams := REGION Ident { "," [ REGION ] Ident }
     *  GroupParams := REFGROUP Ident { "," [ REFGROUP ] Ident }
     */
    Pair<List<JCTypeParameter>, JRGParamInfo> parametersOpt() {
	if (S.token() == LT) {
            checkGenerics();
            S.nextToken();
            List<JCTypeParameter> typarams = null;
            JRGParamInfo paramInfo = null;
            List<DPJRegionParameter> rplParams = List.nil();
            List<Pair<DPJRegionPathList,DPJRegionPathList>> rplConstraints = List.nil();
            List<JCIdent> groupParams = List.nil();
            if (S.token() != REGION && S.token() != REFGROUP) {
        	typarams = typeParameters();
            } else {
        	typarams = List.nil();
            }
            int pos = S.pos();
            if (S.token() == REGION) {
        	rplParams = regionParameters();
            }
            if (S.token() == REFGROUP) {
        	groupParams = groups();
            }
            if (S.token() == BAR) {
        	S.nextToken();
        	rplConstraints = rplConstraints();
            }
            if (rplParams.nonEmpty() || rplConstraints.nonEmpty() ||
        	    groupParams.nonEmpty()) {
        	paramInfo = toP(F.at(pos).ParamInfo(rplParams,
        		rplConstraints, groupParams));
            }
            accept(GT);
            return new Pair(typarams, paramInfo);
        } else {
            return new Pair(List.nil(), null);
        }
    }

    // Skip over optional marker
    private void skipIdent(String name) {
	if (tokenIsIdent(name))
	    S.nextToken();
    }
    
    // Accept required marker
    private void acceptIdent(String name) {
	if (!tokenIsIdent(name)) {
            reportSyntaxError(S.pos(), "expected", name);
	} else {
	    skipIdent(name);
	}
    }
    
    private boolean tokenIsIdent(String name) {
	return (S.token() == IDENTIFIER && S.name().equals(names.fromString(name)));
    }
    
    /** TypeParameters = TypeParameter {"," TypeParameter}
     */
    List<JCTypeParameter> typeParameters() {
        ListBuffer<JCTypeParameter> typarams = new ListBuffer<JCTypeParameter>();
        typarams.append(typeParameter());
        while (S.token() == COMMA) {
            S.nextToken();
            if (S.token() == REGION || S.token() == REFGROUP) break;
            typarams.append(typeParameter());
        }
        return typarams.toList();    
    }
    
    
    /** RPLConstraints := RPLConstraint {(",") RPLConstraint}
     */
    List<Pair<DPJRegionPathList,DPJRegionPathList>> rplConstraints() {
        ListBuffer<Pair<DPJRegionPathList,DPJRegionPathList>> rplConstraints = 
            	ListBuffer.lb();
        Pair<DPJRegionPathList,DPJRegionPathList> rplConstraint = rplConstraint();
        rplConstraints.append(rplConstraint);
	while (S.token() == COMMA || S.token() == SEMI) {
	    S.nextToken();
	    rplConstraint = rplConstraint();
	    rplConstraints.append(rplConstraint);
	}
	return rplConstraints.toList();
    }
    
    /** RegionParameters = RegionParameter {"," RegionParameter}
     */
    List<DPJRegionParameter> regionParameters() {
        ListBuffer<DPJRegionParameter> rgnparams = ListBuffer.lb();
        rgnparams.append(regionParameter());
        while (S.token() == COMMA) {
            S.nextToken();
            if (S.token() == REFGROUP) break;
            rgnparams.append(regionParameter());
        }
	return rgnparams.toList();
    }
    
    /** Groups := Group { "," Group }
     */
    List<JCIdent> groups() {
	ListBuffer<JCIdent> params = ListBuffer.lb();
	params.append(group());
	while (S.token() == COMMA) {
	    S.nextToken();
	    params.append(group());
	}
	return params.toList();
    }
    
    /** Group := [ REFGROUP ] Ident
     */
    JCIdent group() {
	if (S.token() == REFGROUP) {
	    S.nextToken();
	}
	int pos = S.pos();
	Name name = name();
	return toP(F.at(pos).Ident(name));
    }
    
    /** RegionParameter = [ REGION ] Ident
     */
    DPJRegionParameter regionParameter() {
	if (S.token() == REGION) S.nextToken();
	int pos = S.pos();
        Name name = name();
        return toP(F.at(pos).RegionParameter(name, null, false));
    }
    
    /** RPLConstraint := RPL "#" RPL
     */
    Pair<DPJRegionPathList,DPJRegionPathList> rplConstraint() {
        DPJRegionPathList left = rpl();
        accept(NUMBER);
        DPJRegionPathList right = rpl();
        return new Pair<DPJRegionPathList,DPJRegionPathList>(left, right);	
    }
    
    /** TypeParameter = { "type" } TypeVariable [TypeParameterBound]
     *  TypeParameterBound = EXTENDS Type {"&" Type}
     *  TypeVariable = Ident ["<" RegionParameters ">"]
     */
    JCTypeParameter typeParameter() {
	skipIdent("type");
	int pos = S.pos();
        Name name = name();
        List<DPJRegionParameter> rplparams = List.nil();
        if (S.token() == LT) {
            S.nextToken();
            rplparams = regionParameters();
            endOfArgs();
        }
        ListBuffer<JCExpression> bounds = new ListBuffer<JCExpression>();
        if (S.token() == EXTENDS) {
            S.nextToken();
            bounds.append(type());
            while (S.token() == AMP) {
                S.nextToken();
                bounds.append(type());
            }
        }
        return toP(F.at(pos).TypeParameter(name, rplparams, bounds.toList()));
    }

    /** FormalParameters = "(" [ FormalParameterList ] ")"
     *  FormalParameterList = [ FormalParameterListNovarargs , ] LastFormalParameter
     *  FormalParameterListNovarargs = [ FormalParameterListNovarargs , ] FormalParameter
     */
    List<JCVariableDecl> formalParameters() {
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        JCVariableDecl lastParam = null;
        accept(LPAREN);
        if (S.token() != RPAREN) {
            params.append(lastParam = formalParameter());
            while ((lastParam.mods.flags & Flags.VARARGS) == 0 && S.token() == COMMA) {
                S.nextToken();
                params.append(lastParam = formalParameter());
            }
        }
        accept(RPAREN);
        return params.toList();
    }

    JCModifiers optFinal(long flags) {
        JCModifiers mods = modifiersOpt();
        checkNoMods(mods.flags & ~(Flags.FINAL | Flags.DEPRECATED));
        mods.flags |= flags;
        return mods;
    }

    /** FormalParameter = { FINAL | '@' Annotation } Type VariableDeclaratorId
     *  LastFormalParameter = { FINAL | '@' Annotation } Type '...' Ident | FormalParameter
     */
    JCVariableDecl formalParameter() {
        JCModifiers mods = optFinal(Flags.PARAMETER);
        JRGRefPerm refPerm = refPermOpt();
        JCExpression type = type();
        if (S.token() == ELLIPSIS) {
            checkVarargs();
            mods.flags |= Flags.VARARGS;
            type = to(F.at(S.pos()).TypeArray(type));
            S.nextToken();
        }
        return variableDeclaratorId(mods, refPerm, type);
    }

/* ---------- auxiliary methods -------------- */

    /** Check that given tree is a legal expression statement.
     */
    protected JCExpression checkExprStat(JCExpression t) {
        switch(t.getTag()) {
        case JCTree.PREINC: case JCTree.PREDEC:
        case JCTree.POSTINC: case JCTree.POSTDEC:
        case JCTree.ASSIGN:
        case JCTree.BITOR_ASG: case JCTree.BITXOR_ASG: case JCTree.BITAND_ASG:
        case JCTree.SL_ASG: case JCTree.SR_ASG: case JCTree.USR_ASG:
        case JCTree.PLUS_ASG: case JCTree.MINUS_ASG:
        case JCTree.MUL_ASG: case JCTree.DIV_ASG: case JCTree.MOD_ASG:
        case JCTree.APPLY: case JCTree.NEWCLASS:
        case JCTree.ERRONEOUS:
            return t;
        default:
            log.error(t.pos, "not.stmt");
            return F.at(t.pos).Erroneous(List.<JCTree>of(t));
        }
    }

    /** Check that given tree is a legal spawn expression.
     */
    protected JCExpression checkSpawn(JCExpression t) {
        switch(t.getTag()) {
        case JCTree.APPLY: case JCTree.NEWCLASS:
        case JCTree.ERRONEOUS:
            return t;
        default:
            log.error(t.pos, "illegal.spawn");
            return F.at(t.pos).Erroneous(List.<JCTree>of(t));
        }
    }
    
    /** Return precedence of operator represented by token,
     *  -1 if token is not a binary operator. @see TreeInfo.opPrec
     */
    static int prec(Token token) {
        int oc = optag(token);
        return (oc >= 0) ? TreeInfo.opPrec(oc) : -1;
    }

    /** Return operation tag of binary operator represented by token,
     *  -1 if token is not a binary operator.
     */
    static int optag(Token token) {
        switch (token) {
        case BARBAR:
            return JCTree.OR;
        case AMPAMP:
            return JCTree.AND;
        case BAR:
            return JCTree.BITOR;
        case BAREQ:
            return JCTree.BITOR_ASG;
        case CARET:
            return JCTree.BITXOR;
        case CARETEQ:
            return JCTree.BITXOR_ASG;
        case AMP:
            return JCTree.BITAND;
        case AMPEQ:
            return JCTree.BITAND_ASG;
        case EQEQ:
            return JCTree.EQ;
        case BANGEQ:
            return JCTree.NE;
        case LT:
            return JCTree.LT;
        case GT:
            return JCTree.GT;
        case LTEQ:
            return JCTree.LE;
        case GTEQ:
            return JCTree.GE;
        case LTLT:
            return JCTree.SL;
        case LTLTEQ:
            return JCTree.SL_ASG;
        case GTGT:
            return JCTree.SR;
        case GTGTEQ:
            return JCTree.SR_ASG;
        case GTGTGT:
            return JCTree.USR;
        case GTGTGTEQ:
            return JCTree.USR_ASG;
        case PLUS:
            return JCTree.PLUS;
        case PLUSEQ:
            return JCTree.PLUS_ASG;
        case SUB:
            return JCTree.MINUS;
        case SUBEQ:
            return JCTree.MINUS_ASG;
        case STAR:
            return JCTree.MUL;
        case STAREQ:
            return JCTree.MUL_ASG;
        case SLASH:
            return JCTree.DIV;
        case SLASHEQ:
            return JCTree.DIV_ASG;
        case PERCENT:
            return JCTree.MOD;
        case PERCENTEQ:
            return JCTree.MOD_ASG;
        case INSTANCEOF:
            return JCTree.TYPETEST;
        default:
            return -1;
        }
    }

    /** Return operation tag of unary operator represented by token,
     *  -1 if token is not a binary operator.
     */
    static int unoptag(Token token) {
        switch (token) {
        case PLUS:
            return JCTree.POS;
        case SUB:
            return JCTree.NEG;
        case BANG:
            return JCTree.NOT;
        case TILDE:
            return JCTree.COMPL;
        case PLUSPLUS:
            return JCTree.PREINC;
        case SUBSUB:
            return JCTree.PREDEC;
        default:
            return -1;
        }
    }

    /** Return type tag of basic type represented by token,
     *  -1 if token is not a basic type identifier.
     */
    static int typetag(Token token) {
        switch (token) {
        case BYTE:
            return TypeTags.BYTE;
        case CHAR:
            return TypeTags.CHAR;
        case SHORT:
            return TypeTags.SHORT;
        case INT:
            return TypeTags.INT;
        case LONG:
            return TypeTags.LONG;
        case FLOAT:
            return TypeTags.FLOAT;
        case DOUBLE:
            return TypeTags.DOUBLE;
        case BOOLEAN:
            return TypeTags.BOOLEAN;
        default:
            return -1;
        }
    }

    void checkGenerics() {
        if (!allowGenerics) {
            log.error(S.pos(), "generics.not.supported.in.source", source.name);
            allowGenerics = true;
        }
    }
    void checkVarargs() {
        if (!allowVarargs) {
            log.error(S.pos(), "varargs.not.supported.in.source", source.name);
            allowVarargs = true;
        }
    }
    void checkForeach() {
        if (!allowForeach) {
            log.error(S.pos(), "foreach.not.supported.in.source", source.name);
            allowForeach = true;
        }
    }
    void checkStaticImports() {
        if (!allowStaticImport) {
            log.error(S.pos(), "static.import.not.supported.in.source", source.name);
            allowStaticImport = true;
        }
    }
    void checkAnnotations() {
        if (!allowAnnotations) {
            log.error(S.pos(), "annotations.not.supported.in.source", source.name);
            allowAnnotations = true;
        }
    }
}
