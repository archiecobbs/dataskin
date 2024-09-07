/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import java.util.stream.Stream;

public abstract class AbstractInstanceQuery<T, C, R> extends AbstractQuery<C, R> implements InstanceQuery<T, C, R> {

    private final TypeToken<T> targetType;

// Constructors

    protected AbstractInstanceQuery(TypeToken<T> targetType, TypeToken<C> configType, TypeToken<Stream<R>> resultType) {
        this(targetType, configType, resultType, null);
    }

    @SuppressWarnings("unchecked")
    protected AbstractInstanceQuery(TypeToken<T> targetType,
      TypeToken<C> configType, TypeToken<Stream<R>> resultType, String description) {
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
