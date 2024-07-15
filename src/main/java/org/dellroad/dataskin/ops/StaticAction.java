/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

/**
 * An action that is configured outside of the context of any particular target instance.
 *
 * @param <C> configuration type for this action
 * @param <R> result type for this action
 */
public interface StaticAction<C, R> extends Action<C, R>, StaticOperation<C, R> {
}
