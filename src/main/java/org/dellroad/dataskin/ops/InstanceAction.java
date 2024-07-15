/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

/**
 * An action that is configured in the context of a particular target instance.
 *
 * @param <T> target Java type
 * @param <C> configuration type for this action
 * @param <R> result type for this action
 */
public interface InstanceAction<T, C, R> extends Action<C, R>, InstanceOperation<T, C, R> {
}
