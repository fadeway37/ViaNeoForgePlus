/*
 * This file is part of ViaNeoForgePlus, a NeoForge port of ViaFabricPlus.
 * Licensed under the GNU General Public License, version 3 or later.
 */

package com.viaversion.viafabricplus.injection.access.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockBehaviour {

    ItemStack viaFabricPlus$invokeGetCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData);

}
