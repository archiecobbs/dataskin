/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import org.dellroad.dataskin.ops.Action;
import org.dellroad.dataskin.ops.InstanceAction;
import org.dellroad.dataskin.ops.StaticAction;
import org.dellroad.dataskin.ops.scanner.ActionMethodScanner;

/**
 * Annotates methods that implement DataSkin {@link Action}s.
 *
 * <p>
 * The {@link ActionMethodScanner} class detects this annotation and uses the annotated method to
 * automatically construct an {@link Action}.
 *
 * <p><b>Action Type</b>
 *
 * <p>
 * The resulting {@link Action} will be a {@link StaticAction} or {@link InstanceAction} depending on
 * whether the method is a static method or an instance method.
 *
 * <p><b>Method Parameters</b>
 *
 * <p>
 * The method takes up to three parameters, all optional, in this order:
 * <ul>
 *  <li>A context object parameter, whose required type depends on the {@link ActionMethodScanner}
 *  <li>A {@link Consumer Consumer&lt;? super Operation.Progress&gt;} for providing progress updates
 *  <li>A configuration object of arbitrary type {@code C}; the type {@code C} will be
 *      used as the {@linkplain Action#getConfigType action config type}.
 * </ul>
 *
 * <p><b>Return Type</b>
 *
 * <p>
 * The method may return {@code void}, or some arbitrary type {@code R} which will be used as the result type.
 *
 * @see Action
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DataSkinAction {

    /**
     * Get the label for this action, to be used for {@link Action#getLabel}.
     *
     * @return action label
     */
    String label();
}
