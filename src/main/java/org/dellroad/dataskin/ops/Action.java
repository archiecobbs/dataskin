/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

/**
 * A possibly mutating data access operation.
 *
 * <p>
 * Examples of {@link Action}s include the basic CUD operations (create, update, delete) and other arbitrary
 * data access operations like "Reset password", "Trigger confirmation email", "Generate payroll report",
 * "Scrub obsolete records", "Turn on maintenance mode", etc.
 *
 * @param <C> configuration type for this action
 * @param <R> result type for this action
 */
public interface Action<C, R> extends Operation<C, R> {

    /**
     * Determine whether this action, when configured via {@code config}, could possibly
     * change the result of the given static query, assuming it was configured with {@code queryConfig}.
     *
     * <p>
     * It is always safe to return true, though that can result in unnecessary reloads.
     * Implementations should make an appropriate trade-off between precision and efficiency.
     *
     * @param config action config
     * @param result action result
     * @param query possibly affected query
     * @param queryConfig query configuration
     * @param <QC> query configuration type
     * @param <QR> query result type
     */
    <QC, QR> boolean affectsStaticQuery(C config, R result, StaticQuery<QC, QR> query, QC queryConfig);

    /**
     * Determine whether this action, when configured via {@code config}, could possibly
     * change the result of the given instance query, assuming it was configured with {@code queryConfig}
     * and executed on {@code target}.
     *
     * <p>
     * It is always safe to return true, though that can result in unnecessary reloads.
     * Implementations should make an appropriate trade-off between precision and efficiency.
     *
     * @param config action config
     * @param result action result
     * @param query possibly affected query
     * @param queryConfig query configuration
     * @param target query target instance
     * @param <QC> query configuration type
     * @param <QR> query result type
     */
    <QT, QC, QR> boolean affectsInstanceQuery(C config, R result, InstanceQuery<QT, QC, QR> query, QC queryConfig, QT target);
}
