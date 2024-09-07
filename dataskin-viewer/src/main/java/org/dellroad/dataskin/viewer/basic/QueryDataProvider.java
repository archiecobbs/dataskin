/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.basic;

import com.google.common.base.Preconditions;
import com.vaadin.flow.data.provider.DataProvider;

import java.util.Optional;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.InstanceQuery;
import org.dellroad.dataskin.ops.Query;
import org.dellroad.dataskin.ops.StaticQuery;
import org.dellroad.stuff.vaadin24.data.AsyncDataProvider;
import org.dellroad.stuff.vaadin24.field.FieldBuilder;

/**
 * A {@link DataProvider} that can load items from a class by using one of its annotation-defined queries.
 *
 * <p>
 * This class is handy with {@link FieldBuilder} annotations having a {@code dataProvider()} or {@code items()} property
 * when combined with {@link BasicDisplayHooks}, which provides the appropriate context object to this class' constructor.
 *
 * <p>
 * During construction, will autotmatically load themselves from the default query unless it requires configuration.
 *
 * @param <I> query item type
 */
@SuppressWarnings("serial")
public class QueryDataProvider<I> extends AsyncDataProvider<I> {

    protected final Class<I> itemType;
    protected final BasicDisplayHooks.FieldBuilderContext context;
    protected final Optional<StaticQuery<Void, I>> defaultQuery;

    @SuppressWarnings("unchecked")
    public QueryDataProvider(BasicDisplayHooks.FieldBuilderContext context) {
        this.context = context;
        this.itemType = (Class<I>)this.context.inferDataModelType();
        this.defaultQuery = this.context.getOperationCache().getDefaultQuery(this.itemType)
          .map(query -> (StaticQuery<Void, I>)query);
        this.getAsyncTaskManager().setAsyncExecutor(context.getExecutor());
        this.loadFromDefaultQuery();
    }

    public void loadFromDefaultQuery() {
        if (!this.defaultQuery.isPresent()) {
            this.load(id -> Stream.empty());
            return;
        }
        this.loadFromQuery(this.defaultQuery.get(), null);
    }

    public <C> void loadFromQuery(StaticQuery<C, I> query, C config) {
        this.loadFromQuery(query, null, config);
    }

    public <T, C> void loadFromQuery(InstanceQuery<T, C, I> query, T target, C config) {
        this.loadFromQuery((Query<C, I>)query, target, config);
    }

    public <C> void loadFromQuery(Query<C, I> query, Object target, C config) {
        Preconditions.checkArgument(query != null, "null query");
        Preconditions.checkArgument(config != null || !query.requiresConfiguration(), "null config");
        final Query.Handle<C, Stream<I>> handle = query.getHandle(target);
        this.load(id -> handle.perform(config, progress -> { }));
    }
}
