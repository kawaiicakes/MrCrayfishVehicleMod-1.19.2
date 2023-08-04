package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class FuelDrumTileEntity extends TileFluidHandlerSynced
{
    public FuelDrumTileEntity()
    {
        super(ModTileEntities.FUEL_DRUM.get(), null, null, ModBlocks.FUEL_DRUM.get().getCapacity());
    }
    public FuelDrumTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.FUEL_DRUM.get(), pos, state, ModBlocks.FUEL_DRUM.get().getCapacity());
    }
    public FuelDrumTileEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state, int capacity)
    {
        super(tileEntityType, pos, state, capacity);
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FuelDrumTileEntity(ModTileEntities.FUEL_DRUM.get(), pos, state, ModBlocks.FUEL_DRUM.get().getCapacity());
    }


    public boolean hasFluid()
    {
        return !this.tank.getFluid().isEmpty();
    }

    public int getAmount()
    {
        return this.tank.getFluidAmount();
    }

    public int getCapacity()
    {
        return this.tank.getCapacity();
    }
}
