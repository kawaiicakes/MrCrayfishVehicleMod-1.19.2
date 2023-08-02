package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModRecipeTypes //FIXME can't I just use a deferred register here?
    //See history for previous impl... UPDATE: oh, this was definitely written prior to when DeferredRegisters were a thing. 1.12.x lol
    //It would still be prudent to see if it's kept written this way for some other reason.
{
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.RECIPE_TYPES, Reference.MOD_ID
    );

    public static final RegistryObject<RecipeType<FluidExtractorRecipe>> FLUID_EXTRACTOR = REGISTER
            .register("fluid_extractor", () -> register(Reference.MOD_ID + ":fluid_extractor"));
    public static final RegistryObject<RecipeType<FluidMixerRecipe>> FLUID_MIXER = REGISTER
            .register("fluid_mixer", () -> register(Reference.MOD_ID + ":fluid_mixer"));
    public static final RegistryObject<RecipeType<WorkstationRecipe>> WORKSTATION = REGISTER
            .register("workstation", () -> register(Reference.MOD_ID + ":workstation"));

    static <T extends Recipe<?>> RecipeType<T> register(final String key)
    {
        return RecipeType.simple(new ResourceLocation(key));
    }

    // Does nothing, just forces static fields to initialize
    public static void init() {}
}
