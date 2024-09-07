/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

public abstract class AbstractInstanceAction<T, C, R> extends AbstractAction<C, R> implements InstanceAction<T, C, R> {

    private final TypeToken<T> targetType;

// Constructors

    protected AbstractInstanceAction(TypeToken<T> targetType, TypeToken<C> configType, TypeToken<R> resultType) {
        this(targetType, configType, resultType, null);
    }

    @SuppressWarnings("unchecked")
    protected AbstractInstanceAction(TypeToken<T> targetType,
      TypeToken<C> configType, TypeToken<R> resultType, String description) {
        super(configType, resultType, description);
        Preconditions.checkArgument(targetType != null, "null targetType");
        this.targetType = targetType;
    }

// InstanceOperation

    @Override
    public TypeToken<T> getTargetType() {
        return this.targetType;
    }
}
