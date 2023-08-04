package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class GolfCartEntity extends HelicopterEntity
{
    public GolfCartEntity(EntityType<? extends GolfCartEntity> type, Level worldIn)
    {
        super(type, worldIn);
        //TODO figure out electric vehicles
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu containerMenu, int magicNumber1, int magicNumber2) {
        //FIXME: proper impl.
    }
}
