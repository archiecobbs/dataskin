/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.basic;

import com.google.common.base.Preconditions;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.StaticAction;
import org.dellroad.dataskin.ops.StaticQuery;
import org.dellroad.dataskin.ops.Util;
import org.dellroad.dataskin.ops.annotation.DataSkinAction;
import org.dellroad.dataskin.ops.annotation.DataSkinQuery;
import org.dellroad.dataskin.ops.scanner.OperationCache;
import org.dellroad.dataskin.viewer.DataViewer;
import org.dellroad.dataskin.viewer.navtree.NavTreeNode;

/**
 * A {@link DataViewer} navigation tree auto-generated from an annotated Java type hierarchy.
 *
 * <p>
 * Given a set of Java types, this class will organize them in a tree based on subtyping (i.e., type hierarchy),
 * and associate with each type any static actions and/or queries defined using
 * {@link DataSkinAction @DataSkinAction} and {@link DataSkinQuery @DataSkinQuery} annotations.
 */
public class BasicNavTree extends TreeData<NavTreeNode> {

    private final OperationCache operationCache;
    private final HashMap<Class<?>, Node> nodeMap = new HashMap<>();

    /**
     * Constructor.
     *
     * <p>
     * Node labels will be auto-generated from class names.
     *
     * @param types stream of Java types
     * @param operationCache scanned operation cache
     * @throws IllegalArgumentException if {@code types} or any type therein is null
     */
    public BasicNavTree(Stream<? extends Class<?>> types, OperationCache operationCache) {
        this(types, operationCache, null);
    }

    /**
     * Constructor.
     *
     * @param types stream of Java types
     * @param operationCache scanned operation cache
     * @param labeler node label generator, or null to auto-generate from class names
     * @throws IllegalArgumentException if {@code types} or any type therein is null
     */
    public BasicNavTree(Stream<? extends Class<?>> types, OperationCache operationCache,
      Function<? super Class<?>, String> labeler) {
        Preconditions.checkArgument(operationCache != null, "null operationCache");
        Preconditions.checkArgument(types != null, "null types");
        this.operationCache = operationCache;

        // Build mapping from type to label
        final Function<? super Class<?>, String> labeler2
          = labeler != null ? labeler : type -> Util.nameFromCamelCase(type.getSimpleName());
        final HashMap<Class<?>, String> labelMap = new HashMap<>();
        types.forEach(type -> {
            Preconditions.checkArgument(type != null, "null type");
            final String label = labeler2.apply(type);
            Preconditions.checkArgument(label != null, "null label");
            labelMap.put(type, label);
        });

        // Build mapping from type to supertype (slow O(n^2))
        final HashMap<Class<?>, Class<?>> supertypeMap = new HashMap<>();
        for (Class<?> type : labelMap.keySet()) {
            Class<?> supertype = null;
            for (Class<?> otherType : labelMap.keySet()) {
                if (otherType != type && this.isStrictAncestor(otherType, type)) {
                    if (supertype == null || this.isStrictAncestor(supertype, otherType))
                        supertype = otherType;
                }
            }
            supertypeMap.put(type, supertype);
        }

        // Build inverse map from supertype to subtypes
        final HashMap<Class<?>, List<Class<?>>> subtypeMap = new HashMap<>();
        supertypeMap.forEach((subtype, supertype) -> subtypeMap.computeIfAbsent(supertype, p -> new ArrayList<>()).add(subtype));

        // Scan for queries and action and create the nodes
        labelMap.forEach((type, label) -> {

            // Gather queries
            final List<StaticQuery<?, ?>> queries = this.operationCache.getOperations(type)
              .filter(StaticQuery.class::isInstance)
              .map(c -> (StaticQuery<?, ?>)c)
              .collect(Collectors.toList());

            // Gather actions
            final List<StaticAction<?, ?>> actions = this.operationCache.getOperations(type)
              .filter(StaticAction.class::isInstance)
              .map(c -> (StaticAction<?, ?>)c)
              .collect(Collectors.toList());

            // Find default query
            final Optional<StaticQuery<?, ?>> defaultQuery = this.operationCache.getDefaultQuery(type);

            // Create node
            this.nodeMap.put(type, new Node(type, label, queries, actions, defaultQuery));
        });

        // Populate tree
        this.addSubtree(subtypeMap, null);
    }

    private void addSubtree(Map<Class<?>, List<Class<?>>> subtypeMap, Class<?> type) {
        final Node node = this.nodeMap.get(type);
        for (Class<?> subtype : subtypeMap.get(type)) {
            this.addItem(node, this.nodeMap.get(subtype));
            this.addSubtree(subtypeMap, subtype);
        }
    }

    // Is c1 a strict ancestor of c2?
    private boolean isStrictAncestor(Class<?> c1, Class<?> c2) {
        return c1.isAssignableFrom(c2) && !c2.isAssignableFrom(c1);
    }

    /**
     * Get all of the nodes keyed by Java type.
     */
    public Map<Class<?>, Node> getNodeMap() {
        return Collections.unmodifiableMap(this.nodeMap);
    }

    /**
     * Build a {@link TreeDataProvider} from the given Java types.
     *
     * @param types stream of Java types
     * @throws IllegalArgumentException if {@code types} or any type therein is null
     * @throws IllegalArgumentException if {@code operationCache} is null
     */
    public static TreeDataProvider<NavTreeNode> from(Stream<? extends Class<?>> types, OperationCache operationCache) {
        return new TreeDataProvider<>(new BasicNavTree(types, operationCache));
    }

// Node

    /**
     * One node in a {@link BasicNavTree} navigation tree
     */
    public static class Node implements NavTreeNode {

        private final Class<?> type;
        private final String label;
        private final List<? extends StaticQuery<?, ?>> queries;
        private final List<? extends StaticAction<?, ?>> actions;
        private final Optional<StaticQuery<?, ?>> defaultQuery;

        Node(Class<?> type, String label, List<? extends StaticQuery<?, ?>> queries,
          List<? extends StaticAction<?, ?>> actions, Optional<StaticQuery<?, ?>> defaultQuery) {
            this.type = type;
            this.label = label;
            this.queries = queries;
            this.actions = actions;
            this.defaultQuery = defaultQuery;
        }

        public Class<?> getType() {
            return this.type;
        }

    // NavTreeNode

        @Override
        public String getLabel() {
            return this.label;
        }

        @Override
        public Optional<StaticQuery<?, ?>> getDefaultQuery() {
            return this.defaultQuery;
        }

        @Override
        public Stream<? extends StaticQuery<?, ?>> getQueries() {
            return this.queries.stream();
        }

        @Override
        public Stream<? extends StaticAction<?, ?>> getActions() {
            return this.actions.stream();
        }

    // Object

        @Override
        public int hashCode() {
            return this.getClass().hashCode() ^ this.type.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            final Node that = (Node)obj;
            return this.type == that.type;
        }
    }
}
