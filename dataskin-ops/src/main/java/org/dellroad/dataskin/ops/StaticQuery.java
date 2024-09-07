/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import java.util.stream.Stream;

/**
 * A query operation that is configured outside of the context of any particular target instance.
 *
 * @param <C> configuration type for this query
 * @param <R> result item type for this query
 */
public interface StaticQuery<C, R> extends Query<C, R>, StaticOperation<C, Stream<R>> {
}
