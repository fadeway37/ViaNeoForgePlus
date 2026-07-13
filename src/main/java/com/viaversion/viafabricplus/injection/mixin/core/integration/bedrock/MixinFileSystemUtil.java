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

package com.viaversion.viafabricplus.injection.mixin.core.integration.bedrock;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.raphimc.viabedrock.api.util.FileSystemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FileSystemUtil.class, remap = false)
public abstract class MixinFileSystemUtil {

    @Inject(method = "getFilesInDirectory", at = @At("HEAD"), cancellable = true)
    private static void readIndexedDirectory(final String directory, final CallbackInfoReturnable<Map<Path, byte[]>> cir) throws IOException {
        final ClassLoader classLoader = FileSystemUtil.class.getClassLoader();
        if (classLoader.getResource(directory) != null) {
            return;
        }

        final Module module = FileSystemUtil.class.getModule();
        if (!module.isNamed() || module.getLayer() == null) {
            return;
        }

        final ResolvedModule resolvedModule = module.getLayer().configuration()
            .findModule(module.getName())
            .orElse(null);
        if (resolvedModule == null) {
            return;
        }

        final String prefix = directory + "/";
        final Map<Path, byte[]> files = new LinkedHashMap<>();
        try (ModuleReader moduleReader = resolvedModule.reference().open()) {
            final List<String> entries;
            try (Stream<String> resources = moduleReader.list()) {
                entries = resources
                    .filter(name -> name.startsWith(prefix))
                    .filter(name -> name.indexOf('/', prefix.length()) == -1)
                    .sorted()
                    .toList();
            }

            for (final String entry : entries) {
                try (InputStream resourceStream = moduleReader.open(entry)
                    .orElseThrow(() -> new IOException("Missing ViaBedrock module resource: " + entry))) {
                    files.put(Path.of(entry), resourceStream.readAllBytes());
                }
            }
        }
        cir.setReturnValue(files);
    }

}
