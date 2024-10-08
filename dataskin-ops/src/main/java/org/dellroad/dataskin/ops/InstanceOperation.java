/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

/**
 * An operation that executes in the context of a particular target object.
 *
 * @param <T> target object type
 * @param <C> configuration type for this operation
 * @param <R> result type for this operation
 */
public interface InstanceOperation<T, C, R> extends Operation<C, R> {

    /**
     * Get the Java type that this operation targets.
     */
    TypeToken<T> getTargetType();

    /**
     * Execute this operation on the specified target object.
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
     * @param target target instance
     * @param config the configuration for the operation
     * @param progressUpdater where progress reports should be sent
     * @return operation result
     * @see Operation#getHandle Operation.getHandle()
     * @throws IllegalArgumentException if {@code config} is invalid
     * @throws IllegalArgumentException if {@code progressUpdater} is null
     */
    R perform(T target, C config, Consumer<? super Progress> progressUpdater) throws InterruptedException;

    @Override
    @SuppressWarnings("unchecked")
    default Handle<C, R> getHandle(Object target) {
        final Class<?> targetType = this.getTargetType().getRawType();
        final T target2;
        try {
            target2 = (T)targetType.cast(target);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("target is not of type " + targetType.getName());
        }
        return (config, progress) -> this.perform(target2, config, progress);
    }

    /**
     * Create a new configuration object for this operation in the context of the specified target instance.
     *
     * <p>
     * The implementation in {@link InstanceOperation} attempts to instantiate an instance
     * of the {@linkplain #getConfigType configuration type} using a constructor taking one
     * argument (the {@code target}), and if that fails, falls back to a default constructor.
     *
     * @param target target instance
     * @return new configuration object, never null unless the configuration type is {@link Void}
     */
    @SuppressWarnings("unchecked")
    default C newConfig(T target) {
        final Class<? super C> type = this.getConfigType().getRawType();
        for (Constructor<?> constructor : type.getConstructors()) {
            final Class<?>[] ptypes = constructor.getParameterTypes();
            if (ptypes.length == 1) {
                try {
                    return (C)constructor.newInstance(target);
                } catch (IllegalArgumentException | ReflectiveOperationException e) {
                    // nope
                }
            }
        }
        try {
            return (C)type.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't instantiate " + type, e);
        }
    }
}
