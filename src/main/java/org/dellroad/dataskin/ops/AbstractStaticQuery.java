/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.reflect.TypeToken;

import java.util.stream.Stream;

public abstract class AbstractStaticQuery<C, R> extends AbstractQuery<C, R> implements StaticQuery<C, R> {

// Constructors

    protected AbstractStaticQuery(TypeToken<C> configType, TypeToken<Stream<R>> resultType) {
        this(configType, resultType, null);
    }

    protected AbstractStaticQuery(TypeToken<C> configType, TypeToken<Stream<R>> resultType, String description) {
        super(configType, resultType, description);
    }
}
