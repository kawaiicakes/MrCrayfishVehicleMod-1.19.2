package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class BoostTileEntity extends TileEntitySynced
{
    private float speedMultiplier;

    public BoostTileEntity() {
        this(null, null);
    }
    public BoostTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.BOOST.get(), pos, state);
    }

    public BoostTileEntity(float defaultSpeedMultiplier, BlockPos pos, BlockState state)
    {
        super(ModTileEntities.BOOST.get(), pos, state);
        this.speedMultiplier = defaultSpeedMultiplier;
    }

    /**
     * Any class extending <code>TileEntitySynced</code>> MUST override this method such that its body passes a
     * <code>BlockEntityType</code> of the respective type to the constructor of TileEntitySynced.
     * @param pos   a <code>BlockPos</code> automatically passed by the game.
     * @param state a <code>BlockState</code> automatically passed by the game.
     * @return      a new <code>BlockEntity</code> instance representing this object.
     */
    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BoostTileEntity(pos, state);
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("SpeedMultiplier", Tag.TAG_FLOAT))
        {
            this.speedMultiplier = compound.getFloat("SpeedMultiplier");
        }
    }
}

