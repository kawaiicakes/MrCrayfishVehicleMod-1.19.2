package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.PlaneEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class SportsPlaneEntity extends PlaneEntity
{
    public SportsPlaneEntity(EntityType<? extends SportsPlaneEntity> type, Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling()
    {
        return this.getBoundingBox().inflate(1.5);
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu container, int p_150525_, int p_150526_) {
    }
}
