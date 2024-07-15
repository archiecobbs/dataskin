/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.data;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.StaticAction;
import org.dellroad.dataskin.ops.StaticQuery;

/**
 * Simple implementation of the {@link NavNode} interface.
 */
public class NavNodeImpl implements NavNode {

    private String label = "(Unlabeled)";
    private List<? extends NavNode> childNodes = Collections.emptyList();
    private List<? extends StaticQuery<?, ?>> staticQueries = Collections.emptyList();
    private List<? extends StaticAction<?, ?>> staticActions = Collections.emptyList();

// Setters

    public void setLabel(String label) {
        Preconditions.checkArgument(label != null, "null label");
        this.label = label;
    }

    public void setChildNodes(List<? extends NavNode> childNodes) {
        Preconditions.checkArgument(childNodes != null, "null childNodes");
        this.childNodes = childNodes;
    }

    public void setStaticQueries(List<? extends StaticQuery<?, ?>> staticQueries) {
        Preconditions.checkArgument(staticQueries != null, "null staticQueries");
        this.staticQueries = staticQueries;
    }

    public void setStaticActions(List<? extends StaticAction<?, ?>> staticActions) {
        Preconditions.checkArgument(staticActions != null, "null staticActions");
        this.staticActions = staticActions;
    }

    /**
     * Determine if the given {@link StaticQuery} is a candidate for the
     * {@linkplain #getDefaultQuery default query} for this node.
     *
     * <p>
     * The implementation in {@link NavNodeImpl} returns true if the query doesn't
     * {@linkplain StaticQuery#requiresConfiguration require configuration}.
     */
    protected boolean isDefaultQueryCandidate(StaticQuery<?, ?> query) {
        Preconditions.checkArgument(query != null, "null query");
        return !query.requiresConfiguration();
    }

// NavNode

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public Stream<? extends NavNode> getChildNodes() {
        return this.childNodes.stream();
    }

    @Override
    public StaticQuery<?, ?> getDefaultQuery() {
        return this.getStaticQueries()
          .filter(this::isDefaultQueryCandidate)
          .findFirst()
          .orElseThrow(() -> new RuntimeException(String.format("entity node \"%s\" has no default query", this.getLabel())));
    }

    @Override
    public Stream<? extends StaticQuery<?, ?>> getStaticQueries() {
        return this.staticQueries.stream();
    }

    @Override
    public Stream<? extends StaticAction<?, ?>> getStaticActions() {
        return this.staticActions.stream();
    }
}
