/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.InstanceAction;
import org.dellroad.dataskin.ops.InstanceOperation;
import org.dellroad.dataskin.ops.InstanceQuery;
import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.Query;
import org.dellroad.dataskin.ops.StaticAction;
import org.dellroad.dataskin.ops.StaticOperation;
import org.dellroad.dataskin.ops.StaticQuery;
import org.dellroad.dataskin.viewer.display.QueryDisplay;

@SuppressWarnings("serial")
public class ResultTab<C, R> extends VerticalLayout {

    // Context
    private final DataViewer dataViewer;

    // The original query that opened this tab
    private final Query<C, R> query;
    private final Operation.Handle<C, Stream<R>> handle;
    private final C config;                             // possibly modified via EditQuery

    // GUI info
    private final MenuBar menuBar = new MenuBar();
    private final QueryDisplay<R> queryDisplay;
    private final ListDataProvider<R> dataProvider = new ListDataProvider<R>(Collections.emptySet());
    private final Grid<R> grid;

// Constructor

    public ResultTab(DataViewer dataViewer, Query<C, R> query, C config, Operation.Handle<C, Stream<R>> handle, Stream<R> result) {
        Preconditions.checkArgument(dataViewer != null, "null dataViewer");
        Preconditions.checkArgument(query != null, "null query");
        Preconditions.checkArgument(config != null, "null config");
        Preconditions.checkArgument(handle != null, "null handle");
        Preconditions.checkArgument(result != null, "null result");
        this.dataViewer = dataViewer;
        this.query = query;
        this.config = config;
        this.handle = handle;
        this.queryDisplay = this.dataViewer.displayHooks.buildQueryDisplay(this.query, this.config);
        this.buildLayout();
        this.grid = this.queryDisplay.getGrid();
        this.grid.setDataProvider(this.dataProvider);
        this.reload(result);
    }

// Public Methods

    public Query<C, R> getQuery() {
        return this.query;
    }

    public Operation.Handle<C, Stream<R>> getHandle() {
        return this.handle;
    }

    public C getConfig() {
        return this.config;
    }

    public boolean matches(Query<?, ?> query, Operation.Handle<?, ?> handle, Object config) {
        Preconditions.checkArgument(query != null, "null query");
        Preconditions.checkArgument(handle != null, "null handle");
        Preconditions.checkArgument(config != null, "null config");
        return Objects.equals(this.query, query)
          && Objects.equals(this.handle, handle)
          && Objects.equals(this.config, config);
    }

    public void reload(Stream<R> result) {
        this.dataProvider.getItems().clear();
        result.forEach(this.dataProvider.getItems()::add);
        this.dataProvider.refreshAll();
    }

// Grid

    protected void buildLayout() {

        // Get static and instance operations
        final List<StaticOperation<?, ?>> staticOps = this.query.getStaticOperations(this.config).collect(Collectors.toList());

        // Add edit query and reload query operations
        if (this.query.requiresConfiguration())
            staticOps.add(new EditQuery());
        staticOps.add(new ReloadQuery());

        // Add static query menu
        final List<StaticQuery<?, ?>> staticQueries = this.filter(staticOps, new TypeToken<StaticQuery<?, ?>>() { });
        if (!staticQueries.isEmpty())
            this.addStaticMenu("Query", staticQueries);

        // Add static action menu
        final List<StaticAction<?, ?>> staticActions = this.filter(staticOps, new TypeToken<StaticAction<?, ?>>() { });
        if (!staticActions.isEmpty())
            this.addStaticMenu("Action", staticActions);

        // Allow menu bar customizations
        this.queryDisplay.customizeMenuBar(this.menuBar);

        // Add menu bar if not empty
        if (!this.menuBar.getItems().isEmpty())
            this.add(this.menuBar);

        // Add grid/component
        this.add(this.queryDisplay.getComponent());

        // Add context menu if there are any instance operations
        List<InstanceOperation<? super R, ?, ?>> instanceOps = this.query.getInstanceOperations(this.config)
          .collect(Collectors.toList());
        final List<InstanceQuery<? super R, ?, ?>> instanceQueries = this.filter(instanceOps,
          new TypeToken<InstanceQuery<? super R, ?, ?>>() { });
        final List<InstanceAction<? super R, ?, ?>> instanceActions = this.filter(instanceOps,
          new TypeToken<InstanceAction<? super R, ?, ?>>() { });
        instanceOps = this.combineWithSeparatingNull(instanceActions, instanceQueries);
        if (!instanceOps.isEmpty()) {

            // Add context menu with instance operations
            final GridContextMenu<R> contextMenu = this.grid.addContextMenu();
            this.generateMenuItems(instanceOps, (label, operation) -> {
                if (label == null)
                    contextMenu.add(new Hr());
                else {
                    contextMenu.addItem(label,
                      e -> e.getItem().ifPresent(
                        item -> this.dataViewer.initiateInstanceOperation(operation, item)));
                }
            });

            // Only display the context menu when there is an item (i.e., not when clicked on header)
            contextMenu.setDynamicContentHandler(Objects::nonNull);
        }
    }

    private void addStaticMenu(String title, List<? extends StaticOperation<?, ?>> operations) {
        final MenuItem menu = this.menuBar.addItem(title);
        final SubMenu subMenu = menu.getSubMenu();
        this.generateMenuItems(operations, (label, operation) -> {
            if (label == null)
                subMenu.add(new Hr());
            else
                subMenu.addItem(label, e -> this.dataViewer.initiateStaticOperation(operation));
        });
    }

    private <O extends Operation<?, ?>> void generateMenuItems(List<O> operations, BiConsumer<String, ? super O> handler) {
        operations.forEach(operation -> {
            if (operation == null)
                handler.accept(null, null);
            else {
                String label = operation.getLabel();
                if (operation.requiresConfiguration())
                    label += "...";
                handler.accept(label, operation);
            }
        });
    }

    private <E> List<E> combineWithSeparatingNull(List<? extends E> list1, List<? extends E> list2) {
        final ArrayList<E> combinedList = new ArrayList<>(list1.size() + list2.size());
        combinedList.addAll(list1);
        if (!list1.isEmpty() && !list2.isEmpty())
            combinedList.add(null);                 // null represents the separator
        combinedList.addAll(list2);
        return combinedList;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> filter(List<?> list, TypeToken<T> typeToken) {
        final Class<? super T> type = typeToken.getRawType();
        return (List<T>)list.stream()
          .filter(type::isInstance)
          .map(type::cast)
          .collect(Collectors.toList());
    }

// DerivedQuery

    private abstract class DerivedQuery<C2> implements StaticQuery<C2, R> {

        private final String description;
        private final TypeToken<C2> configType;

        DerivedQuery(String description, TypeToken<C2> configType) {
            this.description = description;
            this.configType = configType;
        }

    // Operation

        @Override
        public final TypeToken<C2> getConfigType() {
            return this.configType;
        }

        @Override
        public final String getLabel() {
            return this.description;
        }

    // Query

        @Override
        public final TypeToken<Stream<R>> getResultType() {
            return ResultTab.this.query.getResultType();
        }

        @Override
        public final Stream<? extends StaticOperation<?, ?>> getStaticOperations(C2 config) {
            return ResultTab.this.query.getStaticOperations(this.mapConfig(config));
        }

        @Override
        public final Stream<? extends InstanceOperation<? super R, ?, ?>> getInstanceOperations(C2 config) {
            return ResultTab.this.query.getInstanceOperations(this.mapConfig(config));
        }

        @Override
        public final Stream<R> perform(C2 config, Consumer<? super Operation.Progress> progressUpdater)
          throws InterruptedException {
            return ResultTab.this.handle.perform(this.mapConfig(config), progressUpdater);
        }

        protected abstract C mapConfig(C2 config);
    }

// ReloadQuery

    private class ReloadQuery extends DerivedQuery<Void> {

        ReloadQuery() {
            super("Reload", TypeToken.of(Void.class));
        }

        @Override
        public Void newConfig() {
            return null;
        }

        @Override
        protected C mapConfig(Void config) {
            return ResultTab.this.config;
        }
    }

// EditQuery

    private class EditQuery extends DerivedQuery<C> {

        EditQuery() {
            super("Edit Query", ResultTab.this.query.getConfigType());
        }

        @Override
        public C newConfig() {
            return ResultTab.this.config;
        }

        @Override
        protected C mapConfig(C config) {
            return config;
        }
    }
}
