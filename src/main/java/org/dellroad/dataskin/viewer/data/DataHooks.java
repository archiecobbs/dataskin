/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.data;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Data access hooks.
 *
 * <p>
 * The methods in this interface should be efficient, as they may be invoked frequently.
 */
public interface DataHooks {

    /**
     * Get the root navigation nodes.
     */
    Stream<? extends NavNode> getRootNavNodes();

    /**
     * Register a listener for changes in the data navigation tree.
     *
     * @param listener callback for notifications
     * @return callback for unregistering {@code listener}; will be supplied with this instance
     */
    Runnable addNavNodesListener(Consumer<? super DataHooks> listener);
}
