/*
 * This file is part of ViaFabricPlus - https://github.com/ViaVersion/ViaFabricPlus
 * Copyright (C) 2021-2026 the original authors
 *                         - Florian Reuth <git@florianreuth.de>
 *                         - RK_01/RaphiMC
 * Copyright (C) 2023-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viaversion.viafabricplus;

import com.viaversion.viafabricplus.api.ViaFabricPlusAPI;
import com.viaversion.viafabricplus.api.entrypoint.ViaFabricPlusEntrypoint;
import com.viaversion.viafabricplus.api.settings.impl.AdvancedSettings;
import com.viaversion.viafabricplus.api.settings.impl.GeneralSettings;
import com.viaversion.viafabricplus.api.settings.impl.VisualSettings;
import com.viaversion.viafabricplus.features.FeaturesLoading;
import com.viaversion.viafabricplus.protocoltranslator.ConversionsImpl;
import com.viaversion.viafabricplus.protocoltranslator.LimitationsImpl;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslationImpl;
import com.viaversion.viafabricplus.screen.ScreensImpl;
import com.viaversion.viafabricplus.settings.SettingsImpl;
import com.viaversion.viafabricplus.util.ClassLoaderPriorityUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ViaFabricPlusImpl implements ViaFabricPlusAPI, ViaFabricPlusEntrypoint {

    private static ViaFabricPlusImpl INSTANCE;

    private final Logger logger = LogManager.getLogger("ViaNeoForgePlus");
    private final Path path = FMLPaths.CONFIGDIR.get().resolve("vianeoforgeplus");

    private final SettingsImpl settings = new SettingsImpl();
    private final ProtocolTranslationImpl protocolTranslation = new ProtocolTranslationImpl();
    private final ConversionsImpl conversions = new ConversionsImpl();
    private LimitationsImpl limitations;
    private ScreensImpl screens;

    private final String version;
    private final String implVersion;
    private CompletableFuture<Void> loadingFuture;

    public ViaFabricPlusImpl() {
        INSTANCE = this;
        // Main's HEAD precedes mod construction; the discovery metadata is already available.
        this.version = FMLLoader.getCurrentOrNull().getLoadingModList().getMods().stream()
            .filter(mod -> mod.getModId().equals("vianeoforgeplus"))
            .findFirst().orElseThrow().getVersion().toString();
        final String manifestVersion = ViaFabricPlusImpl.class.getPackage().getImplementationVersion();
        this.implVersion = manifestVersion != null ? manifestVersion : this.version;
    }

    @Override
    public void onPreLoading() {
        ViaFabricPlus.init(this);
        try {
            Files.createDirectories(this.path);
        } catch (final IOException e) {
            this.logger.error("Failed to create ViaFabricPlus directory", e);
        }

        ClassLoaderPriorityUtil.loadOverridingJars(this.path, logger);
        this.settings.init();
        FeaturesLoading.onPreLoading();

    }

    @Override
    public void onPostRegistryLoading() {
        FeaturesLoading.onPostRegistryLoading();
        // NeoForge freezes its deferred registries before this phase. Initialize
        // the legacy particle ID before Via's asynchronous mappings access it.
        this.loadingFuture = this.protocolTranslation.init(this.path);
    }

    @Override
    public void onPostGameLoading() {
        this.limitations = new LimitationsImpl();
        this.screens = new ScreensImpl();

        FeaturesLoading.onPostGameLoading();
        this.loadingFuture.join();
        this.settings.postInit();
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String implVersion() {
        return this.implVersion;
    }

    @Override
    public Path path() {
        return this.path;
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public SettingsImpl settings() {
        return this.settings;
    }

    public GeneralSettings options() {
        return this.settings.general();
    }

    public VisualSettings visuals() {
        return this.settings.visual();
    }

    public AdvancedSettings advanced() {
        return this.settings.advanced();
    }

    @Override
    public ProtocolTranslationImpl protocolTranslation() {
        return this.protocolTranslation;
    }

    @Override
    public ConversionsImpl conversions() {
        return this.conversions;
    }

    @Override
    public LimitationsImpl limitations() {
        return this.limitations;
    }

    @Override
    public ScreensImpl screens() {
        return this.screens;
    }

    public static ViaFabricPlusImpl impl() {
        return INSTANCE;
    }

}
