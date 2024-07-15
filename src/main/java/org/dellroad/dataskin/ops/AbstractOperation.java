/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.vaadin.flow.shared.util.SharedUtil;

public abstract class AbstractOperation<C, R> implements Operation<C, R> {

    private final TypeToken<C> configType;
    private final TypeToken<R> resultType;

    private String description;

// Constructors

    protected AbstractOperation(TypeToken<C> configType, TypeToken<R> resultType) {
        this(configType, resultType, null);
    }

    @SuppressWarnings("unchecked")
    protected AbstractOperation(TypeToken<C> configType, TypeToken<R> resultType, String description) {
        Preconditions.checkArgument(configType != null, "null configType");
        Preconditions.checkArgument(resultType != null, "null resultType");
        this.configType = configType;
        this.resultType = resultType;
        this.description = description;
    }

// Operation

    @Override
    public TypeToken<C> getConfigType() {
        return this.configType;
    }

    @Override
    public TypeToken<R> getResultType() {
        return this.resultType;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation in {@code AbstractOperation} returns the description provided to the construtor,
     * if any, or else creates a description from this instance's class name by expanding it from {@code CamelCase}.
     */
    @Override
    public String getDescription() {
        if (this.description != null)
            return this.description;
        return SharedUtil.camelCaseToHumanFriendly(this.getClass().getSimpleName());
    }
}
