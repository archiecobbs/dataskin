/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer;

import org.dellroad.dataskin.ops.Action;
import org.dellroad.dataskin.ops.Operation;
import org.dellroad.stuff.vaadin24.util.VaadinUtil;

/**
 * Represents an action in progress.
 */
public class ExecutingAction<C, R> extends ExecutingOperation<C, R, Action<C, R>> {

    public ExecutingAction(DataViewer dataViewer, Action<C, R> action, Operation.Handle<C, R> handle, C config) {
        super(dataViewer, action, handle, config);
    }

    @Override
    protected void handleOperationCompleted(R result) {
        this.dialog.getUI().ifPresent(ui -> VaadinUtil.accessUI(ui,
          () -> this.dataViewer.displayHooks.displayActionResult(ui, this.operation, this.config, result)));
    }
}
