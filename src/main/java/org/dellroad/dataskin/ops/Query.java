/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

/**
 * Read-only data access operation that returns a stream of result items.
 *
 * <p>
 * Result items can themselves be targets for both static and instance operations according to
 * {@link #getStaticOperations getStaticOperations()} and {@link #getInstanceOperations getInstanceOperations()}
 * (respectively).
 *
 * @param <C> configuration type for this query
 * @param <R> result type for this query
 */
public interface Query<C, R> extends Operation<C, Stream<R>> {

    /**
     * Get the item type returned by this query with the given config.
     *
     * <p>
     * The implementation in {@link Query} uses reflection to resolve the generic type parameter {@code <R>}.
     *
     * @return query item result type
     */
    @SuppressWarnings("unchecked")
    default TypeToken<R> getResultItemType() {
        final ParameterizedType streamType = (ParameterizedType)Query.class.getTypeParameters()[1];
        final Type itemType = streamType.getActualTypeArguments()[0];
        return (TypeToken<R>)this.getResultType().resolveType(itemType);
    }

    /**
     * Get the static operations available on result items generated from this query with the given config.
     *
     * @param config query configuration
     * @return static operations
     */
    Stream<? extends StaticOperation<?, ?>> getStaticOperations(C config);

    /**
     * Get the instance operations available on result items generated from this query with the given config.
     *
     * @param config query configuration
     * @return instance operations
     */
    Stream<? extends InstanceOperation<? super R, ?, ?>> getInstanceOperations(C config);
}
