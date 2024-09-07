/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.navtree;

import java.util.Optional;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.StaticAction;
import org.dellroad.dataskin.ops.StaticQuery;

/**
 * A node in the DataViewer navigation tree.
 *
 * <p>
 * The methods in this interface should be efficient, as they may be invoked frequently.
 */
public interface NavTreeNode {

    /**
     * Get the display label for this node.
     */
    String getLabel();

    /**
     * Get the default query that executes when this node is clicked on, if any.
     *
     * @return default query
     */
    Optional<StaticQuery<?, ?>> getDefaultQuery();

    /**
     * Get the queries available from this node via context menu.
     */
    Stream<? extends StaticQuery<?, ?>> getQueries();

    /**
     * Get the actions available from this node via context menu.
     */
    Stream<? extends StaticAction<?, ?>> getActions();
}
