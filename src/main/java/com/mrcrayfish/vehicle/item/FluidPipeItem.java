package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.VehicleMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.world.item.Item;

/**
 * Author: MrCrayfish
 */
public class FluidPipeItem extends BlockItem
{
    public FluidPipeItem(Block block)
    {
        super(block, new Item.Properties().tab(VehicleMod.CREATIVE_TAB));
    }
}
