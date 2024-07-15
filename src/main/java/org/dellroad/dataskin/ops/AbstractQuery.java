/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractQuery<C, R> extends AbstractOperation<C, Stream<R>> implements Query<C, R> {

    private List<? extends StaticOperation<?, ?>> staticOperations = Collections.emptyList();
    private List<? extends InstanceOperation<? super R, ?, ?>> instanceOperations = Collections.emptyList();

// Constructors

    protected AbstractQuery(TypeToken<C> configType, TypeToken<Stream<R>> resultType) {
        this(configType, resultType, null);
    }

    @SuppressWarnings("unchecked")
    protected AbstractQuery(TypeToken<C> configType, TypeToken<Stream<R>> resultType, String description) {
        super(configType, resultType, description);
        Preconditions.checkArgument(resultType != null, "null resultType");
    }

// Setters

    /**
     * Configure the {@link StaticOperation}s to be returned by {@link #getStaticOperations}.
     *
     * @param list list of static operations
     * @throws IllegalArgumentException if {@code list} or any element therein is null
     */
    public void setStaticOperations(List<? extends StaticOperation<?, ?>> list) {
        Preconditions.checkArgument(list != null, "null list");
        list.forEach(op -> Preconditions.checkArgument(op != null, "null operation"));
        this.staticOperations = list;
    }

    /**
     * Configure the {@link InstanceOperation}s to be returned by {@link #getInstanceOperations}.
     *
     * @param list list of instance operations
     * @throws IllegalArgumentException if {@code list} or any element therein is null
     */
    public void setInstanceOperations(List<? extends InstanceOperation<? super R, ?, ?>> list) {
        Preconditions.checkArgument(list != null, "null list");
        list.forEach(op -> Preconditions.checkArgument(op != null, "null operation"));
        this.instanceOperations = list;
    }

// Query

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation in {@link AbstractQuery} ignores {@code config} and returns the operations configured
     * via {@link #setStaticOperations setStaticOperations()}, if any, or else an empty stream.
     *
     * @return list list of static operations
     */
    @Override
    public Stream<? extends StaticOperation<?, ?>> getStaticOperations(C config) {
        return this.staticOperations.stream();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation in {@link AbstractQuery} ignores {@code config} and returns the operations configured
     * via {@link #setInstanceOperations setInstanceOperations()}, if any, or else an empty stream.
     *
     * @return list list of instance operations
     */
    @Override
    public Stream<? extends InstanceOperation<? super R, ?, ?>> getInstanceOperations(C config) {
        return this.instanceOperations.stream();
    }
}
