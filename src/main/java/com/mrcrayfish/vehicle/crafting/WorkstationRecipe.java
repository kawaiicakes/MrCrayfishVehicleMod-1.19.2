package com.mrcrayfish.vehicle.crafting;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.entity.block.WorkstationTileEntity;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
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
public class WorkstationRecipe implements Recipe<WorkstationTileEntity>
{
    private final ResourceLocation id;
    private final EntityType<?> vehicle;
    private final ImmutableList<WorkstationIngredient> materials;

    public WorkstationRecipe(ResourceLocation id, EntityType<?> vehicle, ImmutableList<WorkstationIngredient> materials)
    {
        this.id = id;
        this.vehicle = vehicle;
        this.materials = materials;
    }

    public EntityType<?> getVehicle()
    {
        return this.vehicle;
    }

    public ImmutableList<WorkstationIngredient> getMaterials()
    {
        return this.materials;
    }

    @Override
    public boolean matches(@NotNull WorkstationTileEntity inv, @NotNull Level worldIn)
    {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull WorkstationTileEntity inv)
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
        return ModRecipeSerializers.WORKSTATION.get();
    }

    @Override
    public @NotNull RecipeType<?> getType()
    {
        return ModRecipeTypes.WORKSTATION.get();
    }

    public boolean hasMaterials(Player player)
    {
        for(WorkstationIngredient ingredient : this.getMaterials())
        {
            if(!InventoryUtil.hasWorkstationIngredient(player, ingredient))
            {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials(Player player)
    {
        for(WorkstationIngredient ingredient : this.getMaterials())
        {
            InventoryUtil.removeWorkstationIngredient(player, ingredient);
        }
    }
}
