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
     * Perform this operation.
     *
     * <p>
     * This method will normally be invoked on a background/service thread.
     *
     * @param config operation configuration
     * @param progressUpdater where to report progress updates (if desired)
     * @return operation result
     * @throws RuntimeException if an error occurs during task execution
     * @throws InterruptedException if the current thread is interrupted
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
