/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.dom.ElementConstants;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dellroad.stuff.vaadin24.data.MapPropertySet;

public final class Notifications {

    public static final int NOTIFY_SUCCESS_DURATION = 3000;
    public static final int NOTIFY_INFO_DURATION = 3000;
    public static final int NOTIFY_ERROR_DURATION = 0;                      // require user to close

    private Notifications() {
    }

// Validation

    /**
     * Validate the given {@link Binder} and display an error in an error notification on the screen if invalid.
     *
     * @param binder form binder
     * @return true if valid, false if there were errors
     * @throws IllegalArgumentException if {@code binder} is null
     */
    public static boolean validateAndShowErrors(Binder<?> binder) {

        // Sanity check
        Preconditions.checkArgument(binder != null, "null binder");

        // Check status
        final BinderValidationStatus<?> status = binder.validate();
        if (status.isOk())
            return true;

        // Show error(s)
        Notifications.error("Invalid Input", Notifications.toStrings(status));
        return false;
    }

    /**
     * Extract {@link BinderValidationStatus} information from a {@link ValidationException}.
     */
    public static <T> BinderValidationStatus<T> toBinderValidationStatus(Binder<T> binder, ValidationException e) {
        Preconditions.checkArgument(binder != null, "null binder");
        Preconditions.checkArgument(e != null, "null e");
        return new BinderValidationStatus<>(binder, e.getFieldValidationErrors(), e.getBeanValidationErrors());
    }

    /**
     * Build a list of descriptions of the validation problems from the given {@link BinderValidationStatus}.
     */
    public static String[] toStrings(BinderValidationStatus<?> status) {
        Preconditions.checkArgument(status != null, "null status");
        return Stream.concat(
            status.getFieldValidationStatuses().stream()
              .filter(BindingValidationStatus::isError)
              .map(Notifications::toString),
            status.getBeanValidationResults().stream()
              .map(ValidationResult::getErrorMessage))
          .toArray(String[]::new);
    }

    /**
     * Build a description of the validation problem from the given {@link BindingValidationStatus}.
     */
    public static String toString(BindingValidationStatus<?> status) {

        // Sanity check
        Preconditions.checkArgument(status != null, "null status");

        // Get error message
        String message = status.getMessage().get();

        // Prepend property caption, if possible
        final String caption;
        try {
            caption = MapPropertySet.propertyDefinitionForBinding(status.getBinding()).getCaption();
        } catch (IllegalArgumentException e) {
            return message;
        }
        return String.format("%s: %s", caption, message);
    }

    /**
     * Show an info notification.
     *
     * @param message main message
     * @param details extra message(s), or null
     * @throws IllegalArgumentException if {@code message} is null
     */
    public static void info(String message, String... details) {
        Notifications.notify(NotificationVariant.LUMO_CONTRAST,
          Notification.Position.BOTTOM_END, NOTIFY_INFO_DURATION, message, details);
    }

    /**
     * Show a success notification.
     *
     * @param message main message
     * @param details extra message(s), or null
     * @throws IllegalArgumentException if {@code message} is null
     */
    public static void success(String message, String... details) {
        Notifications.notify(NotificationVariant.LUMO_SUCCESS,
          Notification.Position.BOTTOM_END, NOTIFY_SUCCESS_DURATION, message, details);
    }

    /**
     * Show an error notification.
     *
     * @param message main message
     * @param details extra message(s), or null
     * @throws IllegalArgumentException if {@code message} is null
     */
    public static void error(String message, String... details) {
        Notifications.notify(NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE, NOTIFY_ERROR_DURATION, message, details);
    }

    /**
     * Show a notification.
     *
     * @param variant type of message
     * @param position position on screen
     * @param duration how long to stay on screen, or zero for infinity
     * @param message main message
     * @param details extra message(s), or null
     * @throws IllegalArgumentException if any parameter other than {@code details} is null
     * @throws IllegalStateException if there is Vaadin {@link UI} associated with the current thread
     */
    public static void notify(NotificationVariant variant,
      Notification.Position position, int duration, String message, String... details) {

        // Sanity check
        Preconditions.checkArgument(variant != null, "null variant");
        Preconditions.checkArgument(position != null, "null position");
        Preconditions.checkArgument(message != null, "null message");
        Preconditions.checkState(UI.getCurrent() != null, "no current UI");

        // Build notification
        final Notification notification = new Notification();
        notification.addThemeVariants(variant);
        notification.setPosition(position);
        notification.setDuration(duration);

        // Add top row
        final HorizontalLayout topRow = new HorizontalLayout();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.add(new Div(new Text(message)));
        final Button closeButton = new Button(new Icon("lumo", "cross"), e -> notification.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute(ElementConstants.ARIA_LABEL_ATTRIBUTE_NAME, "Close");
        topRow.add(closeButton);
        notification.add(topRow);

        // Add subsequent rows, if any
        if (details != null) {
            final List<Div> extraDivs = Stream.of(details)
              .filter(Objects::nonNull)
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .map(Text::new)
              .map(Div::new)
              .collect(Collectors.toList());
            if (!extraDivs.isEmpty()) {
                final VerticalLayout extraLayout = new VerticalLayout();
                extraLayout.setAlignItems(FlexComponent.Alignment.START);
                extraDivs.forEach(extraLayout::add);
                notification.add(extraLayout);
            }
        }

        // Display notification
        notification.open();
    }
}
