package com.sun.source.tree;

/**
 * A tree node for a 'cobegin' statement.
 *
 * For example:
 * <pre>
 *   cobegin <em>statement</em>
 * </pre>
 *
 * @author Rob Bocchino
 */
public interface PardoTree extends StatementTree {
    StatementTree getStatement();
}
