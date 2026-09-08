/*
 * ViaNeoForgePlus - GPL-3.0-or-later.
 */
package com.viaversion.vianeoforgeplus;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusEntrypoint;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/** NeoForge bridge for the upstream 5.x lifecycle. Providers are discovered before settings load. */
public final class Entrypoints {
    private static List<ViaFabricPlusEntrypoint> providers;

    private Entrypoints() {
    }

    public static void bootstrap() {
        if (providers != null) throw new IllegalStateException("ViaNeoForgePlus already initialized");
        final List<ViaFabricPlusEntrypoint> discovered = new ArrayList<>();
        discovered.add(new ViaFabricPlusImpl());
        ServiceLoader.load(ViaFabricPlusEntrypoint.class, Entrypoints.class.getClassLoader())
            .forEach(discovered::add);
        providers = List.copyOf(discovered);
        invoke(ViaFabricPlusEntrypoint::onPreLoading);
        ViaFabricPlusImpl.impl().logger().info("Loaded {} ViaNeoForgePlus lifecycle provider(s)", providers.size());
    }

    public static void invoke(final Consumer<ViaFabricPlusEntrypoint> callback) {
        if (providers == null) throw new IllegalStateException("ViaNeoForgePlus has not initialized");
        providers.forEach(callback);
    }
}
