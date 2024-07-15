/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer;

import com.google.common.base.Preconditions;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dellroad.dataskin.viewer.data.DataHooks;
import org.dellroad.dataskin.viewer.data.NavNode;

@SuppressWarnings("serial")
public class NavTreeDataProvider extends TreeDataProvider<NavNode> {

    private final DataHooks dataHooks;
    private final Runnable listenerRegistration;

// Constructor

    /**
     * Constructor.
     *
     * @param dataHooks data access hooks
     * @throws IllegalArgumentException if {@code dataHooks} is null
     */
    public NavTreeDataProvider(DataHooks dataHooks) {
        super(new TreeData<>());
        Preconditions.checkArgument(dataHooks != null, "null dataHooks");
        this.dataHooks = dataHooks;
        this.listenerRegistration = this.dataHooks.addNavNodesListener(d -> this.reloadTreeData());
        this.reloadTreeData();
    }

    protected void reloadTreeData() {
        if (this.reloadChildren(null, this.dataHooks.getRootNavNodes()))
            this.refreshAll();
    }

    protected boolean reloadChildren(NavNode treeParent, Stream<? extends NavNode> dataChildStream) {

        // Get tree data
        final TreeData<NavNode> treeData = this.getTreeData();

        // Get the child nodes in the Vaadin tree (we will keep this list up-to-date)
        final List<NavNode> treeNodes = new ArrayList<>();
        final Runnable reloadTreeNodes = () -> {
            treeNodes.clear();
            treeNodes.addAll(treeParent != null ? treeData.getChildren(treeParent) : treeData.getRootItems());
        };
        reloadTreeNodes.run();

        // Get the child nodes in the data nav tree
        final List<NavNode> dataNodes = dataChildStream.collect(Collectors.toCollection(ArrayList::new));

        // Rectify the children of the nav node parent vs. the Vaadin node parent
        boolean changed = false;
        for (int index = 0; index < treeNodes.size() && index < dataNodes.size(); ) {
            final NavNode treeNode = index < treeNodes.size() ? treeNodes.get(index) : null;
            final NavNode dataNode = index < dataNodes.size() ? dataNodes.get(index) : null;
            assert treeNode != null || dataNode != null;
            final boolean equal = Objects.equals(treeNode, dataNode);
            final boolean addNode = treeNode == null || !equal;
            final boolean delNode = dataNode == null || !equal;
            if (delNode) {
                treeData.removeItem(treeNode);
                treeNodes.remove(index);
            }
            if (addNode) {

                // Remove the node from the tree if it's already in the tree somewhere else we haven't gotten to yet
                if (treeData.contains(dataNode)) {
                    final boolean sameParent = Objects.equals(treeData.getParent(dataNode), treeParent);
                    treeData.removeItem(dataNode);
                    if (sameParent)
                        reloadTreeNodes.run();
                }

                // Add node to the parent (at the end), then move it back into place if needed
                treeData.addItem(treeParent, dataNode);
                if (index < treeNodes.size()) {
                    final NavNode previousSibling = index > 0 ? treeNodes.get(index - 1) : null;
                    treeData.moveAfterSibling(dataNode, previousSibling);
                }
                treeNodes.add(index, dataNode);
            }
            changed |= delNode || addNode;
            if (!(delNode && !addNode))
                index++;
        }

        // Recurse on the children
        assert treeNodes.size() == dataNodes.size();
        for (int i = 0; i < treeNodes.size(); i++) {
            final NavNode treeNode = treeNodes.get(i);
            final NavNode dataNode = dataNodes.get(i);
            changed |= this.reloadChildren(treeNode, dataNode.getChildNodes());
        }

        // Done
        return changed;
    }
}
