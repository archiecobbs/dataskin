/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.display;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.menubar.MenuBar;

/**
 * Represents the display of query resulrs.
 *
 * @see DisplayHooks#buildQueryDisplay DisplayHooks.buildQueryDisplay()
 */
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
