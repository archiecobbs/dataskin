/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.basic;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.AbstractInstanceQuery;
import org.dellroad.dataskin.ops.AbstractQuery;
import org.dellroad.dataskin.ops.AbstractStaticQuery;
import org.dellroad.dataskin.ops.InstanceOperation;
import org.dellroad.dataskin.ops.InstanceQuery;
import org.dellroad.dataskin.ops.Operation;
import org.dellroad.dataskin.ops.Query;
import org.dellroad.dataskin.ops.StaticOperation;
import org.dellroad.dataskin.ops.StaticQuery;
import org.dellroad.dataskin.viewer.basic.annotation.DataViewerQuery;

/**
 * Scanner for {@link DataViewerQuery @DataViewerQuery} annotations.
 */
public class QueryMethodScanner<T> extends AbstractMethodScanner<T, DataViewerQuery> {

    public QueryMethodScanner(Class<T> type) {
        super(type, DataViewerQuery.class);
    }

    public Stream<QueryInfo<?, ?>> queryInfos() {
        return super.findAnnotatedMethods().stream()
          .map(info -> (QueryInfo<?, ?>)info);
    }

    @Override
    protected boolean includeMethod(Method method, DataViewerQuery annotation) {
        return true;
    }

    @Override
    protected QueryInfo<?, ?> createMethodInfo(Method method, DataViewerQuery annotation) {
        return this.createQueryInfo(method, annotation, this.newQueryTypeAnalysis(method));
    }

    // This method exists solely to bind the generic types
    private <OT, C, R>  QueryInfo<C, R> createQueryInfo(Method method,
      DataViewerQuery annotation, QueryTypeAnalysis<C, R> typeAnalysis) {
        return typeAnalysis.isStatic() ?
          new StaticQueryInfo<>(method, annotation, typeAnalysis) :
          new InstanceQueryInfo<>(method, annotation, typeAnalysis);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private QueryTypeAnalysis<?, ?> newQueryTypeAnalysis(Method method) {
        final TypeToken<?> returnType = TypeToken.of(method.getGenericReturnType());
        if (!returnType.isSubtypeOf(Stream.class)) {
            throw new IllegalArgumentException(String.format(
              "%s: method is required to return %s", this.getErrorPrefix(method), Stream.class.getName()));
        }
        return (QueryTypeAnalysis<?, ?>)new QueryTypeAnalysis(method, returnType);
    }

// StaticQueryInfo

    public abstract class QueryInfo<C, R> extends OperationInfo<C, Stream<R>> {

        QueryInfo(Method method, DataViewerQuery annotation, QueryTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public QueryTypeAnalysis<C, R> getTypeAnalysis() {
            return (QueryTypeAnalysis<C, R>)super.getTypeAnalysis();
        }

        @Override
        public abstract Query<C, R> getOperation(BasicDataHooks entityDataHooks);

        @SuppressWarnings("unchecked")
        protected void addStaticOperations(AbstractQuery<C, R> query, BasicDataHooks entityDataHooks) {
            query.setStaticOperations(
              entityDataHooks.getOperations(this.typeAnalysis.resultType.getRawType(), StaticOperation.class)
              .map(op -> (StaticOperation<?, ?>)op)
              .collect(Collectors.toList()));
            query.setInstanceOperations(
              entityDataHooks.getOperations(this.typeAnalysis.resultType.getRawType(), InstanceOperation.class)
              .map(op -> (InstanceOperation<? super R, ?, ?>)op)
              .collect(Collectors.toList()));
        }
    }

    public class StaticQueryInfo<C, R> extends QueryInfo<C, R> {

        private AbstractStaticQuery<C, R> query;

        StaticQueryInfo(Method method, DataViewerQuery annotation, QueryTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public StaticQuery<C, R> getOperation(BasicDataHooks entityDataHooks) {
            if (this.query != null) {
                this.query = new AbstractStaticQuery<C, R>(
                  this.typeAnalysis.configType, this.typeAnalysis.resultType, this.getAnnotation().value()) {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Stream<R> perform(C config, Consumer<? super Operation.Progress> progressConsumer) {
                        return (Stream<R>)StaticQueryInfo.this.invoke(null,
                          StaticQueryInfo.this.typeAnalysis.buildParamArray(entityDataHooks, progressConsumer, config));
                    }
                };
                this.addStaticOperations(this.query, entityDataHooks);
            }
            return this.query;
        }
    }

// InstanceQueryInfo

    public class InstanceQueryInfo<C, R> extends QueryInfo<C, R> {

        private AbstractInstanceQuery<T, C, R> query;

        InstanceQueryInfo(Method method, DataViewerQuery annotation, QueryTypeAnalysis<C, R> typeAnalysis) {
            super(method, annotation, typeAnalysis);
        }

        @Override
        public InstanceQuery<T, C, R> getOperation(BasicDataHooks entityDataHooks) {
            if (this.query != null) {
                this.query = new AbstractInstanceQuery<T, C, R>(TypeToken.of(QueryMethodScanner.this.type),
                  this.typeAnalysis.configType, this.typeAnalysis.resultType, this.getAnnotation().value()) {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Stream<R> perform(T target, C config, Consumer<? super Operation.Progress> progressConsumer) {
                        return (Stream<R>)InstanceQueryInfo.this.invoke(target,
                          InstanceQueryInfo.this.typeAnalysis.buildParamArray(entityDataHooks, progressConsumer, config));
                    }
                };
                this.addStaticOperations(this.query, entityDataHooks);
            }
            return this.query;
        }
    }

// QueryTypeAnalysis

    class QueryTypeAnalysis<C, R> extends OperationTypeAnalysis<C, Stream<R>> {

        QueryTypeAnalysis(Method method, TypeToken<Stream<R>> resultType) {
            super(method, resultType);
        }
    }
}
