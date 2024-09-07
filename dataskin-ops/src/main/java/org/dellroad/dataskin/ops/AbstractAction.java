/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.reflect.TypeToken;

public abstract class AbstractAction<C, R> extends AbstractOperation<C, R> implements Action<C, R> {

// Constructors

    protected AbstractAction(TypeToken<C> configType, TypeToken<R> resultType) {
        this(configType, resultType, null);
    }

    protected AbstractAction(TypeToken<C> configType, TypeToken<R> resultType, String description) {
        super(configType, resultType, description);
    }

// Action

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation in {@code AbstractAction} always returns true.
     */
    @Override
    public <QC, QR> boolean affectsStaticQuery(C config, R result, StaticQuery<QC, QR> query, QC queryConfig) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation in {@code AbstractAction} always returns true.
     */
    @Override
    public <QT, QC, QR> boolean affectsInstanceQuery(C config, R result,
      InstanceQuery<QT, QC, QR> query, QC queryConfig, QT target) {
        return true;
    }
}
