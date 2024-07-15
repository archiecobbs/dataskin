/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.display;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;

import java.util.function.Consumer;

import org.dellroad.dataskin.ops.Action;
import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.Query;

/**
 * Display callbacks.
 *
 * <p>
 * The methods in this class may assume that there is a current {@link VaadinSession}.
 */
public interface DisplayHooks {

    /**
     * Populate the give {@link Dialog} with a form for configuring the given operation.
     *
     * <p>
     * This method should layout a form for editing the given configuration and arrange for
     * one of the callbacks to be invoked when done.
     *
     * @param dialog dialog window to populate
     * @param operation the operation being configured
     * @param config the configuration to edit
     * @param canceled to invoke if configuration operation is canceled
     * @param confirmed to invoke if configuration operation is confirmed
     */
    <C, R> void buildConfigDialog(Dialog dialog, Operation<C, R> operation,
      C config, Consumer<? super C> confirmed, Runnable canceled);

    /**
     * Build the component(s) that will display the results of a successful query operation.
     *
     * <p>
     * The component must be or contain a {@link Grid} somewhere.
     *
     * <p>
     * Note: no data provider should be configured; the caller of this method is responsible for that.
     *
     * @param query the query (to be) performed
     * @param config the query configuration
     */
    <C, R> QueryDisplay<R> buildQueryDisplay(Query<C, R> query, C config);

    /**
     * Display a notification that the given {@code action}, which was configured via {@code config},
     * was successful and produced given {@code result}.
     *
     * <p>
     * Typically this is done via a {@link Notification}.
     *
     * @param ui associated Vaadin {@link UI}
     * @param action the action that was performed
     * @param result the result from the action
     */
    <C, R> void displayActionResult(UI ui, Action<C, R> action, C config, R result);

    /**
     * Display a notification that the given {@code operation}, which was configured via {@code config},
     * failed with the given exception.
     *
     * <p>
     * Typically this is done via a {@link Notification}.
     *
     * <p>
     * If the exception is an {@link InterruptedException}, then the operation was cancelled.
     *
     * @param ui associated Vaadin {@link UI}
     * @param operation the operation that failed
     * @param config the operation's configuration
     * @param error the exception caught
     */
    <C, R> void displayOperationError(UI ui, Operation<C, R> operation, C config, Throwable error);

// QueryDisplay

    public interface QueryDisplay<R> {

        /**
         * Get the component to display.
         *
         * <p>
         * The implementation in {@link QueryDisplay} just returns {@link #getGrid}.
         *
         * @return display component, must not be null
         */
        default Component getComponent() {
            return this.getGrid();
        }

        /**
         * Get the {@link Grid} that displays each item returned by the query, if any.
         *
         * <p>
         * The grid's data provider, if any, will be replaced, so it does not need to have one.
         *
         * @return display grid, if any, or else
         */
        Grid<R> getGrid();

        /**
         * Perform any customizations desired to the menu bar above the component.
         *
         * <p>
         * The implementation in {@link QueryDisplay} does nothing.
         *
         * @param menuBar menu bar
         */
        default void customizeMenuBar(MenuBar menuBar) {
        }

        static <R> QueryDisplay<R> of(Grid<R> grid) {
            Preconditions.checkArgument(grid != null, "null grid");
            return () -> grid;
        }
    }
}
