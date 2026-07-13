/*
 * This file is part of ViaNeoForgePlus, a NeoForge port of ViaFabricPlus.
 * Copyright (C) 2021-2026 the original ViaFabricPlus authors and contributors.
 * Licensed under the GNU General Public License, version 3 or later.
 */

package com.viaversion.vianeoforgeplus;

import com.viaversion.viafabricplus.features.world.footstep_particle.FootStepParticle1_12_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.screen.impl.SettingsScreen;
import com.viaversion.viafabricplus.util.network.DataCustomPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge discovery entrypoint. The actual early client bootstrap remains in
 * ViaFabricPlus' main mixin because the protocol translator must be installed
 * before Minecraft creates its first network connection.
 */
@Mod(value = ViaNeoForgePlus.MOD_ID, dist = Dist.CLIENT)
public final class ViaNeoForgePlus {

    public static final String MOD_ID = "vianeoforgeplus";

    public ViaNeoForgePlus(final IEventBus modBus, final ModContainer modContainer) {
        final IConfigScreenFactory configScreenFactory = (container, parent) -> SettingsScreen.INSTANCE.get(parent);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
        FootStepParticle1_12_2.register(modBus);
        modBus.addListener(DataCustomPayload::registerPayloads);
        ProtocolTranslator.registerEvents();
    }

}
