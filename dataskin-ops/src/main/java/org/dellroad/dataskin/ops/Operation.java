/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.reflect.TypeToken;

import java.util.function.Consumer;

/**
 * An operation that can be performed in a DataSkin model.
 *
 * <p>
 * Instances and their corresponding configuration objects should properly implement {@link Object#equals}.
 *
 * @param <C> configuration type for this operation
 * @param <R> result type for this operation
 */
public interface Operation<C, R> {

    /**
     * Get the type of the objects that are used to configure this operation, if any.
     *
     * <p>
     * If no configuration is required, this should return type {@link Void}.
     *
     * @return operation configuration object type, never null
     */
    TypeToken<C> getConfigType();

    /**
     * Get the type of the result of this operation.
     *
     * @return operation result type, never null
     */
    TypeToken<R> getResultType();

    /**
     * Determine if this operation requires configuration.
     *
     * <p>
     * If an operation does not require configuration, the configuration object will not be edited
     * prior to starting the operation.
     *
     * <p>
     * The implementation in {@link Operation} compares the configuration Java type to {@link Void}.
     */
    default boolean requiresConfiguration() {
        return !this.getConfigType().equals(TypeToken.of(Void.class));
    }

    /**
     * Get a simple textual description of this operation.
     *
     * <p>
     * Should be something short and suitable as a box title or menu item,
     * e.g., "All Users", "Delete User", "Search by Name", etc.
     *
     * @return operation label
     */
    String getLabel();

    /**
     * Get a handle for executing this operation on the specified instance (if any).
     *
     * <p>
     * For an {@link InstanceOperation}, the {@code target} must be non-null, and the returned {@link Handle}
     * will be bound to it.
     *
     * <p>
     * For a {@link StaticOperation}, the {@code target} must be null.
     *
     * @param target target object, or null if this is a {@link StaticOperation}
     * @return handle for executing this operation on {@code target}
     * @throws IllegalArgumentException if this is an {@link InstanceOperation} and {@code target} has the wrong type
     * @throws IllegalArgumentException if this is an {@link InstanceOperation} and {@code target} is null
     * @throws IllegalArgumentException if this is an {@link StaticOperation} and {@code target} is not null
     */
    Handle<C, R> getHandle(Object target);

// Progress

    /**
     * Captures the state of progress of an {@link Operation}.
     */
    class Progress {

        private final double ratio;
        private final String message;

    // Constructors

        public Progress(String message) {
            this(Double.NaN, message);
        }

        public Progress(double ratio) {
            this(ratio, null);
        }

        /**
         * Constructor.
         *
         * @param ratio progress ratio, a value from zero to one or {@link Double#NaN} for indeterminate
         * @param message stsatus message, or null for none
         */
        public Progress(double ratio, String message) {
            this.ratio = !Double.isFinite(ratio) ? Double.NaN : Double.max(0.0, Double.min(1.0, ratio));
            this.message = message;
        }

    // Accessors

        public double getRatio() {
            return this.ratio;
        }

        public boolean isIndeterminate() {
            return Double.isNaN(this.ratio);
        }

        public String getMessage() {
            return this.message;
        }
    }

// Handle

    /**
     * An {@link Operation} bound to a target object (if necessary) so that it may be executed.
     */
    @FunctionalInterface
    interface Handle<C, R> {

        /**
         * Perform an operation.
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
         * @see StaticOperation#perform StaticOperation.perform()
         * @see InstanceOperation#perform InstanceOperation.perform()
         * @throws IllegalArgumentException if {@code config} is invalid
         * @throws IllegalArgumentException if {@code progressUpdater} is null
         */
        R perform(C config, Consumer<? super Progress> progressUpdater) throws InterruptedException;
    }
}
