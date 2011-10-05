package com.sun.source.tree;

import javax.lang.model.element.Name;

/**
 * A tree node for a reference group declaration.
 *
 * For example:
 * <pre>
 *   refgroup <em>name</em> ;
 * </pre>
 *
 */
public interface RefGroupDeclTree extends StatementTree {
    Name getName();
}
