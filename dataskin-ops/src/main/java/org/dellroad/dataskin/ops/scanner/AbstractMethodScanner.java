/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops.scanner;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.Operation;
import org.dellroad.stuff.java.MethodAnnotationScanner;

/**
 * Scanner support superclass.
 *
 * @param <T> Java type being introspected
 * @param <A> method annotation type
 * @param <X> method context parameter type
 */
abstract class AbstractMethodScanner<T, A extends Annotation, X> extends MethodAnnotationScanner<T, A> {

    protected final TypeToken<X> contextParameterType;

    AbstractMethodScanner(Class<T> type, Class<A> atype, TypeToken<X> contextParameterType) {
        super(type, atype);
        this.contextParameterType = contextParameterType;
    }

    public String getAnnotationDescription() {
        return "@" + this.annotationType.getSimpleName();
    }

    public String getErrorPrefix(Method method) {
        return String.format("invalid %s annotation on method %s", this.getAnnotationDescription(), method);
    }

// Subclass Helpers

    <OP extends Operation<?, ?>> Stream<OP> findOperations(Class<?> targetType, Class<OP> operationType, X context) {
        Preconditions.checkArgument(targetType != null, "null targetType");
        Preconditions.checkArgument(operationType != null, "null operationType");
        return Stream.<AbstractMethodScanner<?, ?, X>.OperationInfo<?, ?>>concat(
            new QueryMethodScanner<>(targetType, this.contextParameterType).queryInfos(),
            new ActionMethodScanner<>(targetType, this.contextParameterType).actionInfos())
          .map(info -> info.getOperation(context))
          .filter(operationType::isInstance)
          .map(operationType::cast);
    }

// OperationInfo

    public abstract class OperationInfo<C, R> extends MethodAnnotationScanner<T, A>.MethodInfo {

        final OperationTypeAnalysis<C, R> typeAnalysis;

        OperationInfo(Method method, A annotation, OperationTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation);
            this.typeAnalysis = typeAnalysis;
        }

        public OperationTypeAnalysis<C, R> getTypeAnalysis() {
            return this.typeAnalysis;
        }

        public abstract Operation<C, R> getOperation(X context);
    }

// OperationTypeAnalysis

    abstract class OperationTypeAnalysis<C, R> {

        protected final boolean isStatic;
        protected final boolean hasContext;
        protected final boolean hasProgress;
        protected final boolean hasConfig;
        protected final TypeToken<C> configType;
        protected final TypeToken<R> resultType;

        @SuppressWarnings("unchecked")
        protected OperationTypeAnalysis(Method method, TypeToken<R> resultType) {
            Preconditions.checkArgument(method != null, "null method");
            Preconditions.checkArgument(resultType != null, "null resultType");

            // Initialize
            this.resultType = resultType;
            this.isStatic = (method.getModifiers() & Modifier.STATIC) != 0;

            // Get parameter types
            List<TypeToken<?>> paramTypes = Stream.of(method.getGenericParameterTypes())
              .map(TypeToken::of)
              .collect(Collectors.toList());

            // Context parameter?
            this.hasContext = !paramTypes.isEmpty()
              && (AbstractMethodScanner.this.contextParameterType == null
                || paramTypes.get(0).isSupertypeOf(AbstractMethodScanner.this.contextParameterType));
            if (this.hasContext)
                paramTypes = paramTypes.subList(1, paramTypes.size());

            // Progress consumer parameter?
            this.hasProgress = !paramTypes.isEmpty()
              && paramTypes.get(0).isSupertypeOf(new TypeToken<Consumer<? super Operation.Progress>>() { });
            if (this.hasProgress)
                paramTypes = paramTypes.subList(1, paramTypes.size());

            // Configuration parameter?
            this.hasConfig = !paramTypes.isEmpty();
            if (this.hasConfig) {
                this.configType = (TypeToken<C>)paramTypes.get(0);
                paramTypes = paramTypes.subList(1, paramTypes.size());
            } else
                this.configType = (TypeToken<C>)(Object)TypeToken.of(Void.class);

            // There should be no more parameters
            if (!paramTypes.isEmpty()) {
                throw new IllegalArgumentException(String.format(
                  "%s: invalid parameters for method", AbstractMethodScanner.this.getErrorPrefix(method)));
            }
        }

        boolean isStatic() {
            return this.isStatic;
        }

        Object[] buildParamArray(X context, Consumer<? super Operation.Progress> progressConsumer, Object config) {
            final ArrayList<Object> params = new ArrayList<>(3);
            if (this.hasContext)
                params.add(context);
            if (this.hasProgress)
                params.add(progressConsumer);
            if (this.hasProgress)
                params.add(config);
            return params.toArray();
        }
    }
}
