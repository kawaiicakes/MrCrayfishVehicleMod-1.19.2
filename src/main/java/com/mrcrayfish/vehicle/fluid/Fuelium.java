package com.mrcrayfish.vehicle.fluid;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModFluids;
import com.mrcrayfish.vehicle.init.ModItems;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: MrCrayfish
 */
@ParametersAreNonnullByDefault
public abstract class Fuelium extends ForgeFlowingFluid
{
    public Fuelium()
    {
        super(new Properties(() -> ModFluids.FUELIUM.get().getFluidType(), ModFluids.FLOWING_FUELIUM,
                new Properties(new ResourceLocation(Reference.MOD_ID, "block/fuelium_still"),
                        new ResourceLocation(Reference.MOD_ID, "block/fuelium_flowing"))
                        .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY).density(900).viscosity(900))
                .block(ModBlocks.FUELIUM.get())); //wtf is going on here?
    }

    @Override
    public Item getBucket()
    {
        return ModItems.FUELIUM_BUCKET.get();
    }

    public static class Source extends Fuelium
    {
        @Override
        public boolean isSource(FluidState state)
        {
            return true;
        }

        @Override
        public int getAmount(FluidState state)
        {
            return 8;
        }
    }

    public static class Flowing extends Fuelium
    {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
        {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state)
        {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state)
        {
            return false;
        }
    }
}
