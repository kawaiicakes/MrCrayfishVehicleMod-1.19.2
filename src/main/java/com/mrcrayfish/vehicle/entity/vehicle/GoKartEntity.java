package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class GoKartEntity extends LandVehicleEntity
{
    @SuppressWarnings("deprecation") //FIXME: deprecated $maxUpStep field.
    public GoKartEntity(EntityType<? extends GoKartEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.maxUpStep = 0.625F;
    }


    @Override
    public boolean shouldRenderFuelPort()
    {
        return false;
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu containerMenu, int magicNumber1, int magicNumber2) {
        //FIXME: proper impl.
    }
}
