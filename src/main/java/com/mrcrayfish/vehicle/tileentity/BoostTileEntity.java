package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.Constants;

/**
 * Author: MrCrayfish
 */
public class BoostTileEntity extends TileEntitySynced
{
    private float speedMultiplier;

    public BoostTileEntity()
    {
        super(ModTileEntities.BOOST.get());
    }

    public BoostTileEntity(float defaultSpeedMultiplier)
    {
        super(ModTileEntities.BOOST.get());
        this.speedMultiplier = defaultSpeedMultiplier;
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }

    @Override
    public void load(BlockState state, CompoundTag compound)
    {
        super.load(state, compound);
        if(compound.contains("SpeedMultiplier", Tag.TAG_FLOAT))
        {
            this.speedMultiplier = compound.getFloat("SpeedMultiplier");
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        compound.putFloat("SpeedMultiplier", this.speedMultiplier);
        return super.saveAdditional(compound);
    }
}

