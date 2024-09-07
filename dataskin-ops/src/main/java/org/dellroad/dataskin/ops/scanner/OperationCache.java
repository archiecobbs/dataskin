/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops.scanner;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.StaticQuery;
import org.dellroad.dataskin.ops.annotation.DataSkinAction;
import org.dellroad.dataskin.ops.annotation.DataSkinQuery;

/**
 * Caches the {@link Operation}s gleaned from scanning Java types for
 * {@link DataSkinAction @DataSkinAction} and {@link DataSkinQuery @DataSkinQuery} annotations.
 */
public class OperationCache {

    private final Object context;
    private final LoadingCache<Class<?>, List<Operation<?, ?>>> cache;

    /**
     * Constructor.
     *
     * @param context operation method context object (may be null if not needed)
     */
    public OperationCache(Object context) {
        this.context = context;
        this.cache = CacheBuilder.newBuilder()
          .softValues()
          .build(new CacheLoader<>() {
            @Override
            public List<Operation<?, ?>> load(Class<?> type) {
                return OperationCache.this.findOperations(type);
            }
          });
    }

    /**
     * Get the configured context object.
     *
     * @return context object
     */
    public Object getContext() {
        return this.context;
    }

    /**
     * Get the operations associated with the given type.
     *
     * @param type Java type
     * @return list of associated operations, possibly empty but never null
     * @throws IllegalArgumentException if {@code type} is null
     */
    public Stream<Operation<?, ?>> getOperations(Class<?> type) {
        Preconditions.checkArgument(type != null, "null type");
        return this.cache.getUnchecked(type).stream();
    }

    /**
     * Find a default query, if any exists.
     *
     * @param type Java type
     * @return default query, or null if none exists
     * @throws IllegalArgumentException if {@code type} is null
     */
    public Optional<StaticQuery<?, ?>> getDefaultQuery(Class<?> type) {
        return this.getOperations(type)
          .filter(StaticQuery.class::isInstance)
          .<StaticQuery<?, ?>>map(op -> (StaticQuery<?, ?>)op)
          .filter(query -> !query.requiresConfiguration())
          .filter(query -> type == query.getResultType().getRawType())
          .findFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Operation<?, ?>> findOperations(Class<?> type) {
        Preconditions.checkArgument(type != null, "null type");
        final TypeToken<?> contextType = this.context != null ? TypeToken.of(this.context.getClass()) : null;
        return Stream.<AbstractMethodScanner<?, ?, ?>.OperationInfo<?, ?>>concat(
            new QueryMethodScanner<>(type, contextType).queryInfos(),
            new ActionMethodScanner<>(type, contextType).actionInfos())
          .map(info -> ((AbstractMethodScanner<?, ?, Object>.OperationInfo<?, ?>)info).getOperation(this.context))
          .collect(Collectors.toList());
    }
}
