/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.data;

import java.util.stream.Stream;

import org.dellroad.dataskin.ops.StaticAction;
import org.dellroad.dataskin.ops.StaticQuery;

/**
 * A node in the data navigation tree.
 *
 * <p>
 * The methods in this interface should be efficient, as they may be invoked frequently.
 */
public interface NavNode {

    /**
     * Get the display labe for this node.
     */
    String getLabel();

    /**
     * Get the child nodes of this node.
     */
    Stream<? extends NavNode> getChildNodes();

    /**
     * Get the default query that executes when this node is clicked on.
     *
     * @return default query (must not be null)
     */
    StaticQuery<?, ?> getDefaultQuery();

    /**
     * Get the static queries available from this node via context menu.
     */
    Stream<? extends StaticQuery<?, ?>> getStaticQueries();

    /**
     * Get the static actions available from this node via context menu.
     */
    Stream<? extends StaticAction<?, ?>> getStaticActions();
}
