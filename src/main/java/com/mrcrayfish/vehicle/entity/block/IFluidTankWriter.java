package com.mrcrayfish.vehicle.entity.block;

import net.minecraft.nbt.CompoundTag;

/**
 * Author: MrCrayfish
 */
public interface IFluidTankWriter
{
    void writeTanks(CompoundTag compound);

    boolean areTanksEmpty();
}