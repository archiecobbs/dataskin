/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.basic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.dellroad.dataskin.ops.InstanceQuery;
import org.dellroad.dataskin.ops.Query;
import org.dellroad.dataskin.ops.StaticQuery;
import org.dellroad.dataskin.viewer.DataViewer;
import org.dellroad.dataskin.viewer.basic.BasicDataHooks;

/**
 * Annotates a {@link DataViewer} query method on a database entity class.
 *
 * <p>
 * The {@link BasicDataHooks} class detects this annotation and uses the annotated method to
 * automatically construct a {@link Query}.
 *
 * <p><b>Query Type</b>
 *
 * <p>
 * The resulting {@link Query} will be a {@link StaticQuery} or {@link InstanceQuery} depending on
 * whether the method is a static method or an instance method.
 *
 * <p><b>Method Parameters</b>
 *
 * <p>
 * The method takes up to three parameters, all optional, in this order:
 * <ul>
 *  <li>An {@link BasicDataHooks} instance (for context)
 *  <li>A {@link Consumer Consumer&lt;? super Operation.Progress&gt;} for providing progress updates
 *  <li>A configuration object of arbitrary type {@code C}; the type {@code C} will be
 *      used as the {@linkplain Query#getConfigType query config type}.
 * </ul>
 *
 * <p><b>Return Type</b>
 *
 * <p>
 * The method must return {@link Stream Stream<R>} for some {@code R}; the type {@code R} will be used
 * as the {@linkplain Query#getResultType query result item type}.
 *
 * @see BasicDataHooks
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DataViewerQuery {

    /**
     * Get the description of this query, to be used for {@link Query#getDescription}.
     *
     * @return query description
     */
    String value();
}
