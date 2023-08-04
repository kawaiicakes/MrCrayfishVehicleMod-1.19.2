package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class IndustrialFuelDrumTileEntity extends FuelDrumTileEntity
{
    public IndustrialFuelDrumTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.INDUSTRIAL_FUEL_DRUM.get(), pos, state, ModBlocks.INDUSTRIAL_FUEL_DRUM.get().getCapacity());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new IndustrialFuelDrumTileEntity(pos, state);
    }
}
