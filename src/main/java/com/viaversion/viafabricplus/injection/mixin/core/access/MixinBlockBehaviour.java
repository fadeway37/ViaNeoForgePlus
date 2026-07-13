/*
 * This file is part of ViaNeoForgePlus, a NeoForge port of ViaFabricPlus.
 * Licensed under the GNU General Public License, version 3 or later.
 */

package com.viaversion.viafabricplus.injection.mixin.core.access;

import com.viaversion.viafabricplus.injection.access.block.IBlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockBehaviour.class)
public abstract class MixinBlockBehaviour implements IBlockBehaviour {

    @Override
    @Invoker("getCloneItemStack")
    public abstract ItemStack viaFabricPlus$invokeGetCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData);

}
