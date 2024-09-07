/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops.scanner;

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
import org.dellroad.dataskin.ops.annotation.DataSkinAction;

/**
 * Scanner for {@link DataSkinAction @DataSkinAction} annotations.
 *
 * @param <T> Java type being introspected
 * @param <X> method context parameter type
 */
public class ActionMethodScanner<T, X> extends AbstractMethodScanner<T, DataSkinAction, X> {

    public ActionMethodScanner(Class<T> type, TypeToken<X> contextParameterType) {
        super(type, DataSkinAction.class, contextParameterType);
    }

    @SuppressWarnings("unchecked")
    public Stream<ActionInfo<?, ?>> actionInfos() {
        return super.findAnnotatedMethods().stream()
          .map(info -> (ActionInfo<?, ?>)info);
    }

    @Override
    protected boolean includeMethod(Method method, DataSkinAction annotation) {
        return true;
    }

    @Override
    protected ActionInfo<?, ?> createMethodInfo(Method method, DataSkinAction annotation) {
        return this.createActionInfo(method, annotation, this.newActionTypeAnalysis(method));
    }

    // This method exists solely to bind the generic types
    private <OT, C, R>  ActionInfo<C, R> createActionInfo(Method method,
      DataSkinAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
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

        ActionInfo(Method method, DataSkinAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public ActionTypeAnalysis<C, R> getTypeAnalysis() {
            return (ActionTypeAnalysis<C, R>)super.getTypeAnalysis();
        }

        @Override
        public abstract Action<C, R> getOperation(X context);
    }

    public class StaticActionInfo<C, R> extends ActionInfo<C, R> {

        private AbstractStaticAction<C, R> action;

        StaticActionInfo(Method method, DataSkinAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public StaticAction<C, R> getOperation(X context) {
            if (this.action != null) {
                this.action = new AbstractStaticAction<C, R>(
                  this.typeAnalysis.configType, this.typeAnalysis.resultType, this.getAnnotation().label()) {
                    @Override
                    @SuppressWarnings("unchecked")
                    public R perform(C config, Consumer<? super Operation.Progress> progressConsumer) {
                        return (R)StaticActionInfo.this.invoke(null,
                          StaticActionInfo.this.typeAnalysis.buildParamArray(context, progressConsumer, config));
                    }
                };
            }
            return this.action;
        }
    }

// InstanceActionInfo

    public class InstanceActionInfo<C, R> extends ActionInfo<C, R> {

        private AbstractInstanceAction<T, C, R> action;

        InstanceActionInfo(Method method, DataSkinAction annotation, ActionTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public InstanceAction<T, C, R> getOperation(X context) {
            if (this.action != null) {
                this.action = new AbstractInstanceAction<T, C, R>(TypeToken.of(ActionMethodScanner.this.type),
                  this.typeAnalysis.configType, this.typeAnalysis.resultType, this.getAnnotation().label()) {
                    @Override
                    @SuppressWarnings("unchecked")
                    public R perform(T target, C config, Consumer<? super Operation.Progress> progressConsumer) {
                        return (R)InstanceActionInfo.this.invoke(target,
                          InstanceActionInfo.this.typeAnalysis.buildParamArray(context, progressConsumer, config));
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
