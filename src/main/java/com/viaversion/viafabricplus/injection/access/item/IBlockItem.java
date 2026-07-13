/*
 * This file is part of ViaNeoForgePlus, a NeoForge port of ViaFabricPlus.
 * Licensed under the GNU General Public License, version 3 or later.
 */

package com.viaversion.viafabricplus.injection.access.item;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockItem {

    BlockState viaFabricPlus$invokeGetPlacementState(BlockPlaceContext context);

}
