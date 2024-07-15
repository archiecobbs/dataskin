/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.Action;
import org.dellroad.dataskin.ops.InstanceOperation;
import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.Query;
import org.dellroad.dataskin.ops.StaticOperation;
import org.dellroad.dataskin.viewer.data.DataHooks;
import org.dellroad.dataskin.viewer.data.NavNode;
import org.dellroad.dataskin.viewer.display.DisplayHooks;

/**
 * General purpose data viewer component.
 *
 * <p>
 * Instances are configured with {@link DataHooks} that help determine how the data is accessed
 * and {@link DisplayHooks} that help determine how data is displayed.
 */
@SuppressWarnings("serial")
public class DataViewer extends AppLayout {

    protected final DataHooks dataHooks;
    protected final DisplayHooks displayHooks;
    protected final Function<? super Runnable, ? extends Future<?>> executor;
    protected final ArrayList<ResultTab<?, ?>> resultTabs = new ArrayList<>();
    protected final TabSheet tabSheet = new TabSheet();

// Constructor

    /**
     * Constructor.
     *
     * @param dataHooks data access hooks
     * @param displayHooks data display hooks
     * @param executor executor for background operations
     * @throws IllegalArgumentException if any parameter is null
     */
    @SuppressWarnings("this-escape")
    public DataViewer(DataHooks dataHooks, DisplayHooks displayHooks, Function<? super Runnable, ? extends Future<?>> executor) {

        // Sanity check
        Preconditions.checkArgument(dataHooks != null, "null dataHooks");
        Preconditions.checkArgument(displayHooks != null, "null displayHooks");
        Preconditions.checkArgument(executor != null, "null executor");

        // Initialize
        this.dataHooks = dataHooks;
        this.displayHooks = displayHooks;
        this.executor = executor;

        // Build layout
        this.buildNavbar();
        this.buildDrawer();
        this.buildMain();
    }

// Top Navigation Bar

    protected void buildNavbar() {
        final H1 label = new H1("DataViewer");
        label.addClassNames(
          LumoUtility.FontSize.LARGE,
          LumoUtility.Margin.MEDIUM);

        final HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), label);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(label);
        header.setWidthFull();
        header.addClassNames(
          LumoUtility.Padding.Vertical.NONE,
          LumoUtility.Padding.Horizontal.MEDIUM);

        this.addToNavbar(header);
    }

// Navigation Tree

    protected void buildDrawer() {

        // Build entity type tree grid
        final NavTreeDataProvider dataProvider = new NavTreeDataProvider(this.dataHooks);
        final TreeGrid<NavNode> treeGrid = new TreeGrid<>(dataProvider);
        treeGrid.addHierarchyColumn(NavNode::getLabel);
        treeGrid.expandRecursively(dataProvider.getTreeData().getRootItems(), 2);

        // Handle click on entity type
        treeGrid.addItemClickListener(e -> this.handleNavTreeClick(e.getItem()));

        // Done
        this.addToDrawer(treeGrid);
    }

    @SuppressWarnings("unchecked")
    protected <T> void handleNavTreeClick(NavNode node) {
        Optional.ofNullable(node.getDefaultQuery())
          .ifPresent(this::initiateStaticOperation);
    }

// Operations

    protected <C, R> void initiateStaticOperation(StaticOperation<C, R> operation) {
        this.initiateOperation(operation, operation.getHandle(null), operation.newConfig());
    }

    @SuppressWarnings("unchecked")
    protected <T, C, R> void initiateInstanceOperation(InstanceOperation<T, C, R> operation, Object item) {
        this.initiateOperation(operation, operation.getHandle((T)item), operation.newConfig((T)item));
    }

    protected <C, R> void initiateOperation(Operation<C, R> operation, Operation.Handle<C, R> handle, C initialConfig) {

        // If the config does not need to be edited, proceed with the operation
        if (!operation.requiresConfiguration()) {
            this.executeOperation(operation, handle, initialConfig);
            return;
        }

        // Build config dialog
        final Dialog dialog = new Dialog();
        dialog.setHeaderTitle(operation.getDescription());
        this.displayHooks.buildConfigDialog(dialog, operation, initialConfig, completedConfig -> {
            this.executeOperation(operation, handle, completedConfig);
            dialog.close();
          }, dialog::close);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void executeOperation(Operation operation, Operation.Handle handle, Object config) {
        if (operation instanceof Query)
            this.newExecutingQuery((Query)operation, handle, config);
        else
            this.newExecutingAction((Action)operation, handle, config);
    }

    protected <C, R> ExecutingQuery<C, R>  newExecutingQuery(Query<C, R> query, Operation.Handle<C, Stream<R>> handle, C config) {
        return new ExecutingQuery<C, R>(this, query, handle, config);
    }

    protected <C, R> ExecutingAction<C, R>  newExecutingAction(Action<C, R> action, Operation.Handle<C, R> handle, C config) {
        return new ExecutingAction<C, R>(this, action, handle, config);
    }

// ResultTabs

    @SuppressWarnings("unchecked")
    public <C, R> void addQueryResult(Query<C, R> query, Operation.Handle<C, Stream<R>> handle, C config, Stream<R> result) {

        // See if query matches an existing tab
        for (ResultTab<?, ?> resultTab : this.resultTabs) {
            if (resultTab.matches(query, handle, config)) {
                ((ResultTab<C, R>)resultTab).reload(result);
                return;
            }
        }

        // Create a new one
        final ResultTab<C, R> resultTab = new ResultTab<>(this, query, config, handle, result);
        final Tab tabLabel = this.buildTabLabel(resultTab);

        // If there were zero tabs before, display tab sheet
        if (this.resultTabs.isEmpty())
            this.setContent(this.tabSheet);

        // Add new tab to tab sheet
        this.tabSheet.add(tabLabel, resultTab);
        this.resultTabs.add(resultTab);
    }

// Main Content Area

    protected void buildMain() {
        this.setContent(this.buildEmptyDisplayComponent());
    }

    protected Component buildEmptyDisplayComponent() {
        return new VerticalLayout();
    }

    private <C, R> void removeTab(ResultTab<C, R> resultTab) {

        // Calculate tab index
        final int tabIndex = this.resultTabs.indexOf(resultTab);
        Preconditions.checkState(tabIndex >= 0, "tab not found");

        // Remove tab from tab sheet
        this.tabSheet.remove(tabIndex);
        this.resultTabs.remove(tabIndex);

        // If zero tabs are left, display the empty content panel
        if (this.resultTabs.isEmpty())
            this.setContent(this.buildEmptyDisplayComponent());
    }

    protected <C, R> Tab buildTabLabel(ResultTab<C, R> resultTab) {
        final Button closeButton = new Button(new Icon("lumo", "cross"), e -> this.removeTab(resultTab));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute(ElementConstants.ARIA_LABEL_ATTRIBUTE_NAME, "Close");
        return new Tab(new HorizontalLayout(closeButton, new Text(resultTab.getQuery().getDescription())));
    }
}
