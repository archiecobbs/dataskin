/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.viewer.data;

import com.google.common.base.Preconditions;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

/**
 * Support superclass for {@link DataHooks} implementations that handles listener registrations.
 */
public abstract class AbstractDataHooks implements DataHooks {

    protected final HashSet<Consumer<? super DataHooks>> listeners = new HashSet<>();

    @Override
    public Runnable addNavNodesListener(Consumer<? super DataHooks> listener) {
        Preconditions.checkState(listener != null, "null listener");
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
        return () -> this.removeNavNodesListener(listener);
    }

    protected void removeNavNodesListener(Consumer<? super DataHooks> listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    protected void notifyListeners() {
        final List<Consumer<? super DataHooks>> listenerList;
        synchronized (this.listeners) {
            listenerList = this.listeners.stream().collect(Collectors.toList());
        }
        listenerList.forEach(listener -> {
            try {
                listener.accept(this);
            } catch (Throwable t) {
                LoggerFactory.getLogger(this.getClass()).error("exception from DataHooks listener " + listener, t);
            }
        });
    }
}
