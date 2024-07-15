/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.basic;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import org.dellroad.dataskin.ops.Action;
import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.Query;
import org.dellroad.dataskin.viewer.Notifications;
import org.dellroad.dataskin.viewer.display.DisplayHooks;
import org.dellroad.stuff.vaadin24.field.AbstractFieldBuilder;
import org.dellroad.stuff.vaadin24.field.FieldBuilder;
import org.dellroad.stuff.vaadin24.field.FieldBuilderContextImpl;
import org.dellroad.stuff.vaadin24.grid.GridColumnScanner;
import org.dellroad.stuff.vaadin24.util.VaadinUtil;
import org.dellroad.stuff.vaadin24.util.WholeBeanValidator;

/**
 * Basic implementation of {@link DisplayHooks}.
 */
public class BasicDisplayHooks implements DisplayHooks {

    protected final BasicDataHooks dataHooks;
    protected final Function<? super Runnable, ? extends Future<?>> executor;

    private HashMap<Class<?>, FieldBuilder<?>> fieldBuilderMap = new HashMap<>();
    private HashMap<Class<?>, GridColumnScanner<?>> gridColumnScannerMap = new HashMap<>();

    public BasicDisplayHooks(BasicDataHooks dataHooks, Function<? super Runnable, ? extends Future<?>> executor) {
        Preconditions.checkArgument(dataHooks != null, "null dataHooks");
        Preconditions.checkArgument(executor != null, "null executor");
        this.dataHooks = dataHooks;
        this.executor = executor;
    }

    public BasicDataHooks getDataHooks() {
        return this.dataHooks;
    }

    @Override
    public <C, R> void buildConfigDialog(Dialog dialog, Operation<C, R> operation,
      C config, Consumer<? super C> confirmed, Runnable canceled) {

        // Sanity check
        VaadinUtil.assertCurrentSession();
        Preconditions.checkArgument(config != null, "null config");

        // Create fields and binder
        final TypeToken<C> configType = operation.getConfigType();
        final FieldBuilder<C> fieldBuilder = this.getFieldBuilder(configType);
        final Binder<C> binder = new BeanValidationBinder<>(this.toRaw(configType))
          .withValidator(new WholeBeanValidator(this.toRaw(configType)));
        binder.setBean(config);
        fieldBuilder.bindFields(binder);

        // Build config dialog
        dialog.setHeaderTitle(operation.getDescription());
        final FormLayout formLayout = new FormLayout();
        fieldBuilder.addFieldComponents(formLayout);
        dialog.add(formLayout);

        // Add buttons
        final Button okButton = new Button("OK", e -> {
            if (Notifications.validateAndShowErrors(binder)) {
                confirmed.accept(config);
                dialog.close();
            }
        });
        final Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
            canceled.run();
        });
        dialog.getFooter().add(okButton);
        dialog.getFooter().add(cancelButton);
    }

    @Override
    public <C, R> QueryDisplay<R> buildQueryDisplay(Query<C, R> query, C config) {
        VaadinUtil.assertCurrentSession();
        return QueryDisplay.of(this.getGridColumnScanner(query.getResultItemType()).buildGrid());
    }

    @Override
    public <C, R> void displayActionResult(UI ui, Action<C, R> action, C config, R result) {
        VaadinUtil.assertCurrentSession();
        final String message = String.format("%s successful", action.getDescription());
        if (result == null)
            Notifications.success(message);
        else {
            String details = result.toString();
            if (details.length() > 100)
                details = details.substring(0, 100) + "...";
            Notifications.success(message, details);
        }
    }

    @Override
    public <C, R> void displayOperationError(UI ui, Operation<C, R> operation, C config, Throwable error) {
        VaadinUtil.assertCurrentSession();
        final String message = String.format("%s failed", operation.getDescription());
        Notifications.error(message, error.getMessage() != null ? error.getMessage() : error.toString());
    }

    /**
     * Get a {@link FieldBuilder} for the given type.
     *
     * <p>
     * This method caches its results to speed up repeated invocations, only invoking {@link #newFieldBuilder}
     * the first time {@code type} is seen.
     */
    @SuppressWarnings("unchecked")
    protected <T> FieldBuilder<T> getFieldBuilder(TypeToken<T> type) {
        final FieldBuilder<T> prototype
          = (FieldBuilder<T>)this.fieldBuilderMap.computeIfAbsent(this.toRaw(type), this::newFieldBuilder);
        return new FieldBuilder<>(prototype);
    }

    /**
     * Get a {@link GridColumnScanner} for the given type.
     *
     * <p>
     * This method caches its results to speed up repeated invocations, only invoking {@link #newGridColumnScanner}
     * the first time {@code type} is seen.
     */
    @SuppressWarnings("unchecked")
    protected <T> GridColumnScanner<T> getGridColumnScanner(TypeToken<T> type) {
        final GridColumnScanner<T> prototype
          = (GridColumnScanner<T>)this.gridColumnScannerMap.computeIfAbsent(this.toRaw(type), this::newGridColumnScanner);
        return new GridColumnScanner<>(prototype);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> toRaw(TypeToken<T> type) {
        return (Class<T>)type.getRawType();
    }

// Subclass Hooks

    /**
     * Create a new {@link FieldBuilder} for the given type.
     *
     * <p>
     * The implementation in {@link BasicDisplayHooks} creates a {@link FieldBuilder} that uses
     * {@link FieldBuilderContext} for context.
     */
    protected <T> FieldBuilder<T> newFieldBuilder(Class<T> type) {
        Preconditions.checkArgument(type != null, "null type");
        return new FieldBuilder<>(type) {
            @Override
            protected FieldBuilderContext newFieldBuilderContext(AbstractFieldBuilder<FieldBuilder<T>, T>.BindingInfo bindingInfo) {
                return new FieldBuilderContext(bindingInfo, BasicDisplayHooks.this);
            }
        };
    }

    /**
     * Create a new {@link GridColumnScanner} for the given type.
     *
     * <p>
     * The implementation in {@link BasicDisplayHooks} just invokes the {@link GridColumnScanner} constructor.
     * Subclasses can override to customize the {@link GridColumnScanner} used.
     */
    protected <T> GridColumnScanner<T> newGridColumnScanner(Class<T> type) {
        Preconditions.checkArgument(type != null, "null type");
        return new GridColumnScanner<>(type);
    }

// FieldBuilderContext

    /**
     * The {@link BasicDisplayHooks} custom version of {@link FieldBuilderContextImpl}.
     */
    @SuppressWarnings("serial")
    public static class FieldBuilderContext extends FieldBuilderContextImpl {

        protected final BasicDisplayHooks displayHooks;

        public FieldBuilderContext(AbstractFieldBuilder<?, ?>.BindingInfo bindingInfo, BasicDisplayHooks displayHooks) {
            super(bindingInfo);
            Preconditions.checkArgument(displayHooks != null, "null displayHooks");
            this.displayHooks = displayHooks;
        }

        public BasicDisplayHooks getDisplayHooks() {
            return this.displayHooks;
        }

        public Function<? super Runnable, ? extends Future<?>> getExecutor() {
            return this.displayHooks.executor;
        }

        public BasicDataHooks getDataHooks() {
            return this.displayHooks.dataHooks;
        }
    }
}
