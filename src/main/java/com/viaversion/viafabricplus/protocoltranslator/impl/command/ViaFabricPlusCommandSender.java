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

package com.viaversion.viafabricplus.protocoltranslator.impl.command;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class ViaFabricPlusCommandSender implements ViaCommandSender {

    private final CommandSourceStack source;

    public ViaFabricPlusCommandSender(final CommandSourceStack source) {
        this.source = source;
    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public void sendMessage(String s) {
        this.source.sendSystemMessage(Component.nullToEmpty(s.replace("/viaversion", "/vianeoforgeplus")));
    }

    @Override
    public UUID getUUID() {
        return Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : new UUID(0L, 0L);
    }

    @Override
    public String getName() {
        return Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "ViaNeoForgePlus";
    }

}
