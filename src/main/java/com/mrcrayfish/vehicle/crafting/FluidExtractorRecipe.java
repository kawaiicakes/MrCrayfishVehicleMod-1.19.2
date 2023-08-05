package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.entity.block.FluidExtractorTileEntity;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorRecipe implements Recipe<FluidExtractorTileEntity>
{
    private final ResourceLocation id;
    private final ItemStack ingredient;
    private final FluidEntry result;

    public FluidExtractorRecipe(ResourceLocation id, ItemStack ingredient, FluidEntry result)
    {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
    }

    public ItemStack getIngredient()
    {
        return ingredient;
    }

    public FluidEntry getResult()
    {
        return result;
    }

    @Override
    public boolean matches(FluidExtractorTileEntity fluidExtractor, @NotNull Level worldIn)
    {
        ItemStack source = fluidExtractor.getItem(FluidExtractorTileEntity.SLOT_FLUID_SOURCE);
        return InventoryUtil.areItemStacksEqualIgnoreCount(source, this.ingredient);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull FluidExtractorTileEntity inv)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.FLUID_EXTRACTOR.get();
    }

    @Override
    public @NotNull RecipeType<?> getType()
    {
        return ModRecipeTypes.FLUID_EXTRACTOR.get();
    }
}
