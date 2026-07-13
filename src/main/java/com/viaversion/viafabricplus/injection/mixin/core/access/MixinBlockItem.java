/*
 * This file is part of ViaNeoForgePlus, a NeoForge port of ViaFabricPlus.
 * Licensed under the GNU General Public License, version 3 or later.
 */

package com.viaversion.viafabricplus.injection.mixin.core.access;

import com.viaversion.viafabricplus.injection.access.item.IBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem implements IBlockItem {

    @Override
    @Invoker("getPlacementState")
    public abstract BlockState viaFabricPlus$invokeGetPlacementState(BlockPlaceContext context);

}
