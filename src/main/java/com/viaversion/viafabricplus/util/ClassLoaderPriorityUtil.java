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

package com.viaversion.viafabricplus.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.Logger;

/**
 * Detects legacy Fabric-style library overrides. NeoForge's transforming module
 * graph is immutable by the time the client entrypoint runs; overrides must be
 * supplied by rebuilding the embedded dependencies, not by mutating a loader.
 */
public final class ClassLoaderPriorityUtil {

    private static final Set<String> overridingJars = new HashSet<>();

    /**
     * Loads all overriding jars
     */
    public static void loadOverridingJars(final Path path, final Logger logger) {
        try {
            overridingJars.clear();

            final Path jars = path.resolve("jars");
            if (!Files.exists(jars)) {
                Files.createDirectory(jars);
                return;
            }

            final File[] files = jars.toFile().listFiles();
            if (files == null) {
                return;
            }

            final File[] jarFiles = Arrays.stream(files)
                .filter(file -> file.getName().endsWith(".jar"))
                .toArray(File[]::new);
            if (jarFiles.length == 0) {
                return;
            }

            logger.warn("Ignoring {} library override(s) in {}: NeoForge does not support Fabric-style jars overrides. Update the embedded dependencies and rebuild instead.", jarFiles.length, jars);
        } catch (Throwable e) {
            logger.error("Failed to load overriding jars", e);
        }
    }

    public static Set<String> getOverridingJars() {
        return overridingJars;
    }

}
