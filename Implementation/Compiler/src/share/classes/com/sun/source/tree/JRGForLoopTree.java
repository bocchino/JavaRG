package com.sun.source.tree;

/**
 * A tree node for a JRG 'for' statement.
 *
 * For example:
 * <pre>
 *   for each (int i in <em>expression</em>) <em>statement</em>
 * </pre>
 * <pre>
 *   for each (int i in <em>expression</em>) pardo <em>statement</em>
 * </pre>
 *
 * @author Rob Bocchino
 */
public interface JRGForLoopTree extends StatementTree {
    VariableTree getIndexVar();
    ExpressionTree getArray();
    StatementTree getBody();
}
