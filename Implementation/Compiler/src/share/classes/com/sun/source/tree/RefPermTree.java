package com.sun.source.tree;

/**
 * A tree node for a reference permission.
 *
 * For example:
 * <pre>
 *   unique(G)
 * </pre>
 *
 */

public interface RefPermTree extends Tree {
    boolean isShared();
}
