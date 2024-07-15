/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

import java.util.Optional;
import java.util.function.Consumer;

import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.Query;
import org.dellroad.stuff.vaadin24.util.AsyncTaskManager;
import org.dellroad.stuff.vaadin24.util.AsyncTaskStatusChangeEvent;
import org.dellroad.stuff.vaadin24.util.VaadinUtil;

/**
 * Represents an operation in progress.
 */
public abstract class ExecutingOperation<C, R, O extends Operation<C, R>> {

    // Context
    protected final DataViewer dataViewer;
    protected final VaadinSession session;

    // Task management
    protected final AsyncTaskManager<R> taskManager;
    protected final Registration listenerRegistration;
    protected final long taskId;

    // Operation
    protected final O operation;
    protected final Operation.Handle<C, R> handle;
    protected final C config;

    // Display
    protected final Dialog dialog;
    protected final Text statusField;
    protected final ProgressBar progressBar;
    protected final Button cancelButton;

// Constructor

    protected ExecutingOperation(DataViewer dataViewer, O operation, Operation.Handle<C, R> handle, C config) {

        // Sanity check
        Preconditions.checkArgument(dataViewer != null, "null dataViewer");
        Preconditions.checkArgument(operation != null, "null operation");
        Preconditions.checkArgument(config != null, "null config");

        // Initialize
        this.dataViewer = dataViewer;
        this.taskManager = new AsyncTaskManager<R>(this.dataViewer.executor);
        this.listenerRegistration = this.taskManager.addAsyncTaskStatusChangeListener(this::asynTaskStatusChange);
        this.session = this.taskManager.getVaadinSession();
        this.operation = operation;
        this.handle = handle;
        this.config = config;

        // Build display dialog
        this.dialog = new Dialog();
        this.dialog.setHeaderTitle(this.operation.getDescription());
        this.statusField = new Text("Starting operation...");
        this.progressBar = new ProgressBar(0.0, 1.0);
        this.progressBar.setIndeterminate(true);
        final VerticalLayout layout = new VerticalLayout(this.statusField, this.progressBar);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.getStyle().set("width", "18rem").set("max-width", "100%");
        this.dialog.add(layout);
        this.cancelButton = new Button("Cancel", e -> this.cancel());
        this.dialog.getFooter().add(this.cancelButton);

        // Create progress updater
        final Consumer<Operation.Progress> progressUpdater =
          progress -> VaadinUtil.accessSession(this.session, () -> this.updateProgress(progress));

        // Start task
        this.taskId = this.taskManager.startTask(id -> this.handle.perform(this.config, progressUpdater));

        // Open dialog
        this.dialog.open();
    }

    public boolean cancel() {

        // Sanity check
        VaadinUtil.assertCurrentSession(this.session);
        if (this.taskManager.cancelTask() != this.taskId)
            return false;

        // Update display
        this.statusField.setText("Cancelling...");
        this.progressBar.setIndeterminate(true);
        this.cancelButton.setEnabled(false);
        return true;
    }

    protected void updateProgress(Operation.Progress progress) {
        VaadinUtil.assertCurrentSession(this.session);
        this.statusField.setText(Optional.ofNullable(progress.getMessage()).orElse(""));
        this.progressBar.setValue(progress.getRatio());
        this.progressBar.setIndeterminate(progress.isIndeterminate());
    }

    protected void asynTaskStatusChange(AsyncTaskStatusChangeEvent<R> event) {
        switch (event.getStatus()) {
        case AsyncTaskStatusChangeEvent.STARTED:
            this.statusField.setText(String.format("Performing %s...", this.operation instanceof Query ? "query" : "action"));
            break;
        case AsyncTaskStatusChangeEvent.CANCELED:       // assume they know already, so don't notify
            this.dialog.close();
            break;
        case AsyncTaskStatusChangeEvent.FAILED:
            this.handleOperationError(event.getException());
            this.dialog.close();
            break;
        case AsyncTaskStatusChangeEvent.COMPLETED:
            this.handleOperationCompleted(event.getResult());
            this.dialog.close();
            break;
        default:
            throw new RuntimeException("internal error");
        }
    }

    protected void handleOperationError(Throwable error) {
        this.dialog.getUI().ifPresent(ui -> VaadinUtil.accessUI(ui,
          () -> this.dataViewer.displayHooks.displayOperationError(ui, this.operation, this.config, error)));
    }

    protected abstract void handleOperationCompleted(R result);
}
