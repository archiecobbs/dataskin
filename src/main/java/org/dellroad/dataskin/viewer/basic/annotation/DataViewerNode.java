/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.basic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.dellroad.dataskin.viewer.DataViewer;
import org.dellroad.dataskin.viewer.basic.BasicDataHooks;
import org.dellroad.dataskin.viewer.data.NavNode;

/**
 * Annotates a {@link DataViewer} database entity class that should appear as a node in the navigation tree.
 *
 * <p>
 * The {@link BasicDataHooks} class detects this annotation and uses the annotated classes and their inheritance
 * relationships to automatically build a tree of {@link NavNode}s.
 *
 * <p>
 * Queries and actions are inferred from {@link DataViewerQuery @DataViewerQuery} and {@link DataViewerAction @DataViewerAction}
 * annotations. The (first) {@link DataViewerQuery @DataViewerQuery} method that is static and has no configuration object,
 * if any, is chosen as the node's {@link NavNode#getDefaultQuery default query}.
 *
 * @see BasicDataHooks
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Documented
public @interface DataViewerNode {

    /**
     * Get the label for this type, to be used for {@link NavNode#getLabel}.
     *
     * @return navigation node label
     */
    String value();
}
