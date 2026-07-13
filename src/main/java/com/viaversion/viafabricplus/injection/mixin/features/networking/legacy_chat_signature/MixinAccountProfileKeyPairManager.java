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

package com.viaversion.viafabricplus.injection.mixin.features.networking.legacy_chat_signature;

import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.authlib.minecraft.UserApiService;
import com.viaversion.viafabricplus.features.networking.legacy_chat_signature.LegacyKeyPairResponse;
import com.viaversion.viafabricplus.injection.access.networking.legacy_chat_signature.IProfilePublicKey_Data;
import net.minecraft.client.multiplayer.AccountProfileKeyPairManager;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AccountProfileKeyPairManager.class)
public abstract class MixinAccountProfileKeyPairManager {

    @Redirect(
        method = "fetchProfileKeyPair",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/minecraft/UserApiService;getKeyPair()Lcom/mojang/authlib/yggdrasil/response/KeyPairResponse;",
            remap = false
        )
    )
    private KeyPairResponse fetchLegacyKeyPair(final UserApiService userApiService) {
        return LegacyKeyPairResponse.fetch(userApiService);
    }

    @Inject(method = "parsePublicKey", at = @At("RETURN"))
    private static void trackLegacyKey(KeyPairResponse response, CallbackInfoReturnable<ProfilePublicKey.Data> cir) {
        final byte[] legacySignature = LegacyKeyPairResponse.legacySignature(response);
        if (legacySignature != null) {
            ((IProfilePublicKey_Data) (Object) cir.getReturnValue()).viafabricplus$setLegacyPublicKeySignature(legacySignature);
        }
    }

}
