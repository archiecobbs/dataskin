/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.basic;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.AbstractInstanceAction;
import org.dellroad.dataskin.ops.AbstractStaticAction;
import org.dellroad.dataskin.ops.Action;
import org.dellroad.dataskin.ops.InstanceAction;
import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.StaticAction;
import org.dellroad.dataskin.viewer.basic.annotation.DataViewerAction;

/**
 * Scanner for {@link DataViewerAction @DataViewerAction} annotations.
 */
public class ActionMethodScanner<T> extends AbstractMethodScanner<T, DataViewerAction> {

    public ActionMethodScanner(Class<T> type) {
        super(type, DataViewerAction.class);
    }

    public Stream<ActionInfo<?, ?>> actionInfos() {
        return super.findAnnotatedMethods().stream()
          .map(info -> (ActionInfo<?, ?>)info);
    }

    @Override
    protected boolean includeMethod(Method method, DataViewerAction annotation) {
        return true;
    }

    @Override
    protected ActionInfo<?, ?> createMethodInfo(Method method, DataViewerAction annotation) {
        return this.createActionInfo(method, annotation, this.newActionTypeAnalysis(method));
    }

    // This method exists solely to bind the generic types
    private <OT, C, R>  ActionInfo<C, R> createActionInfo(Method method,
      DataViewerAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
        return typeAnalysis.isStatic() ?
          new StaticActionInfo<>(method, annotation, typeAnalysis) :
          new InstanceActionInfo<>(method, annotation, typeAnalysis);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ActionTypeAnalysis<?, ?> newActionTypeAnalysis(Method method) {
        final TypeToken<?> returnType = TypeToken.of(method.getGenericReturnType());
        return (ActionTypeAnalysis<?, ?>)new ActionTypeAnalysis(method, returnType.wrap());
    }

// StaticActionInfo

    public abstract class ActionInfo<C, R> extends OperationInfo<C, R> {

        ActionInfo(Method method, DataViewerAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public ActionTypeAnalysis<C, R> getTypeAnalysis() {
            return (ActionTypeAnalysis<C, R>)super.getTypeAnalysis();
        }

        @Override
        public abstract Action<C, R> getOperation(BasicDataHooks entityDataHooks);
    }

    public class StaticActionInfo<C, R> extends ActionInfo<C, R> {

        private AbstractStaticAction<C, R> action;

        StaticActionInfo(Method method, DataViewerAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public StaticAction<C, R> getOperation(BasicDataHooks entityDataHooks) {
            if (this.action != null) {
                this.action = new AbstractStaticAction<C, R>(
                  this.typeAnalysis.configType, this.typeAnalysis.resultType, this.getAnnotation().value()) {
                    @Override
                    @SuppressWarnings("unchecked")
                    public R perform(C config, Consumer<? super Operation.Progress> progressConsumer) {
                        return (R)StaticActionInfo.this.invoke(null,
                          StaticActionInfo.this.typeAnalysis.buildParamArray(entityDataHooks, progressConsumer, config));
                    }
                };
            }
            return this.action;
        }
    }

// InstanceActionInfo

    public class InstanceActionInfo<C, R> extends ActionInfo<C, R> {

        private AbstractInstanceAction<T, C, R> action;

        InstanceActionInfo(Method method, DataViewerAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public InstanceAction<T, C, R> getOperation(BasicDataHooks entityDataHooks) {
            if (this.action != null) {
                this.action = new AbstractInstanceAction<T, C, R>(TypeToken.of(ActionMethodScanner.this.type),
                  this.typeAnalysis.configType, this.typeAnalysis.resultType, this.getAnnotation().value()) {
                    @Override
                    @SuppressWarnings("unchecked")
                    public R perform(T target, C config, Consumer<? super Operation.Progress> progressConsumer) {
                        return (R)InstanceActionInfo.this.invoke(target,
                          InstanceActionInfo.this.typeAnalysis.buildParamArray(entityDataHooks, progressConsumer, config));
                    }
                };
            }
            return this.action;
        }
    }

// ActionTypeAnalysis

    class ActionTypeAnalysis<C, R> extends OperationTypeAnalysis<C, R> {

        ActionTypeAnalysis(Method method, TypeToken<R> resultType) {
            super(method, resultType);
        }
    }
}
