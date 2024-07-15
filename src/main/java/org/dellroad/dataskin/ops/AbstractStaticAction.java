/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.reflect.TypeToken;

public abstract class AbstractStaticAction<C, R> extends AbstractAction<C, R> implements StaticAction<C, R> {

// Constructors

    protected AbstractStaticAction(TypeToken<C> configType, TypeToken<R> resultType) {
        this(configType, resultType, null);
    }

    protected AbstractStaticAction(TypeToken<C> configType, TypeToken<R> resultType, String description) {
        super(configType, resultType, description);
    }
}
