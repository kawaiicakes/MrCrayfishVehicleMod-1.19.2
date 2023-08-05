package com.mrcrayfish.vehicle.recipe;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class RecipeRefillSprayCan extends ShapelessRecipe {
    public RecipeRefillSprayCan(ResourceLocation id)
    {
        super(id, ("refill_spray_can"), );
    }

    @Override
    public boolean matches(CraftingContainer inventory, @NotNull Level worldIn)
    {
        ItemStack sprayCan = ItemStack.EMPTY;
        ItemStack emptySprayCan = ItemStack.EMPTY;

        for(int i = 0; i < inventory.getContainerSize(); i++)
        {
            ItemStack stack = inventory.getItem(i);
            if(!stack.isEmpty())
            {
                if(stack.getItem() instanceof SprayCanItem)
                {
                    if(((SprayCanItem) stack.getItem()).hasColor(stack))
                    {
                        if(!sprayCan.isEmpty())
                        {
                            return false;
                        }
                        sprayCan = stack.copy();
                    }
                    else
                    {
                        if(!emptySprayCan.isEmpty())
                        {
                            return false;
                        }
                        emptySprayCan = stack.copy();
                    }
                }
            }
        }
        return !sprayCan.isEmpty() && !emptySprayCan.isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inventory)
    {
        ItemStack sprayCan = ItemStack.EMPTY;
        ItemStack emptySprayCan = ItemStack.EMPTY;

        for(int i = 0; i < inventory.getContainerSize(); i++)
        {
            ItemStack stack = inventory.getItem(i);
            if(!stack.isEmpty())
            {
                if(stack.getItem() instanceof SprayCanItem)
                {
                    if(((SprayCanItem) stack.getItem()).hasColor(stack))
                    {
                        if(!sprayCan.isEmpty())
                        {
                            return ItemStack.EMPTY;
                        }
                        sprayCan = stack.copy();
                    }
                    else
                    {
                        if(!emptySprayCan.isEmpty())
                        {
                            return ItemStack.EMPTY;
                        }
                        emptySprayCan = stack.copy();
                    }
                }
            }
        }

        if(!sprayCan.isEmpty() && !emptySprayCan.isEmpty())
        {
            ItemStack copy = sprayCan.copy();
            ((SprayCanItem) copy.getItem()).refill(copy);
            return copy;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return null;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return null;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.REFILL_SPRAY_CAN.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return null;
    }
}
