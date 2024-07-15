/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer;

import java.util.stream.Stream;

import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.Query;

/**
 * Represents a query in progress.
 */
public class ExecutingQuery<C, R> extends ExecutingOperation<C, Stream<R>, Query<C, R>> {

    public ExecutingQuery(DataViewer dataViewer, Query<C, R> query, Operation.Handle<C, Stream<R>> handle, C config) {
        super(dataViewer, query, handle, config);
    }

    @Override
    protected void handleOperationCompleted(Stream<R> result) {
        this.dataViewer.addQueryResult(this.operation, this.handle, this.config, result);
    }
}
