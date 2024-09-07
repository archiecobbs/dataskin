/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import java.util.function.Consumer;

/**
 * An operation that is configured outside of the context of any particular target instance.
 *
 * @param <C> configuration type for this operation
 * @param <R> result type for this operation
 */
public interface StaticOperation<C, R> extends Operation<C, R> {

    /**
     * Execute this operation.
     *
     * <p>
     * The operation executes synchronously in the current thread. Progress updates may delivered
     * to {@code progressUpdater} to update the progrss of the operation; these callbacks may occur
     * in the current thread or in another thread.
     *
     * <p>
     * An in-progress operation may be cancelled by interrupting the current thread; if so,
     * an {@link InterruptedException} is thrown.
     *
     * @param config the configuration for the operation
     * @param progressUpdater where progress reports should be sent
     * @return operation result
     * @see Operation#getHandle Operation.getHandle()
     * @throws IllegalArgumentException if {@code config} is invalid
     * @throws IllegalArgumentException if {@code progressUpdater} is null
     */
    R perform(C config, Consumer<? super Progress> progressUpdater) throws InterruptedException;

    @Override
    default Handle<C, R> getHandle(Object target) {
        return (config, progress) -> this.perform(config, progress);
    }

    /**
     * Create a new configuration object for this operation.
     *
     * <p>
     * The implementation in {@link StaticOperation} attempts to instantiate an instance
     * of the {@linkplain #getConfigType configuration type} using a default constructor.
     *
     * @return new configuration object, never null unless the configuration type is {@link Void}
     */
    @SuppressWarnings("unchecked")
    default C newConfig() {
        final Class<? super C> type = this.getConfigType().getRawType();
        try {
            return (C)type.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't instantiate " + type, e);
        }
    }
}
