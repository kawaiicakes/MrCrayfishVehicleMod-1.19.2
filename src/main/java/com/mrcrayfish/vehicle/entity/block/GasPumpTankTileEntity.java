package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankTileEntity extends TileFluidHandlerSynced
{
    public GasPumpTankTileEntity()
    {
        super(ModTileEntities.GAS_PUMP_TANK.get(), null, null, Config.SERVER.gasPumpCapacity.get());
    }
    public GasPumpTankTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.GAS_PUMP_TANK.get(), pos, state, Config.SERVER.gasPumpCapacity.get());
    }

    /**
     * Any class extending <code>TileFluidHandlerSynced</code> MUST override this method such that its body contains
     * the constructor of the subclass. The constructor of the subclass MUST call <code>super</code> and pass
     * arguments appropriately.
     * <br>
     * Similarly, any class extending subclasses of <code>TileEntitySynced</code> must copy the described behaviour.
     * Any method making a call to the constructor of <code>TileEntitySynced</code> or anything extending it -
     * specifically in other classes - should instead consider doing so inside an override of this method.
     *
     * @param pos   a <code>BlockPos</code> automatically passed by the game.
     * @param state a <code>BlockState</code> automatically passed by the game.
     * @return a new <code>BlockEntity</code> instance representing this object.
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GasPumpTankTileEntity(pos, state);
    }
}