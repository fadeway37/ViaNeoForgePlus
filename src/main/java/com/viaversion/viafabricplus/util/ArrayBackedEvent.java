/*
 * This file is part of ViaNeoForgePlus, a NeoForge port of ViaFabricPlus.
 * Licensed under the GNU General Public License, version 3 or later.
 */

package com.viaversion.viafabricplus.util;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Small loader-independent callback event used by the public ViaFabricPlus API.
 */
public final class ArrayBackedEvent<T> {

    private final List<T> listeners = new CopyOnWriteArrayList<>();
    private final Function<List<T>, T> invokerFactory;
    private volatile T invoker;

    public ArrayBackedEvent(final Function<List<T>, T> invokerFactory) {
        this.invokerFactory = Objects.requireNonNull(invokerFactory, "invokerFactory");
        this.invoker = invokerFactory.apply(List.of());
    }

    public void register(final T listener) {
        this.listeners.add(Objects.requireNonNull(listener, "listener"));
        this.rebuildInvoker();
    }

    public T invoker() {
        return this.invoker;
    }

    private void rebuildInvoker() {
        this.invoker = this.invokerFactory.apply(List.copyOf(this.listeners));
    }

}
