package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.MotorcycleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class DirtBikeEntity extends MotorcycleEntity
{
    public DirtBikeEntity(EntityType<? extends DirtBikeEntity> type, Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu containerMenu, int magicNumber1, int magicNumber2) {
        //FIXME: proper impl.
    }
}
