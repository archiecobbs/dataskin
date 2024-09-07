/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import java.util.stream.Stream;

/**
 * A query operation that is configured in the context of a particular target instance.
 *
 * <p>
 * Note that a query's return type can be different from it's target type. For example, a {@code Author}
 * type might have an {@link InstanceQuery} named "Show Books" that returns result item type {@code Book}.
 *
 * @param <T> target Java type
 * @param <C> configuration type for this query
 * @param <R> result item type for this query
 */
public interface InstanceQuery<T, C, R> extends Query<C, R>, InstanceOperation<T, C, Stream<R>> {
}
