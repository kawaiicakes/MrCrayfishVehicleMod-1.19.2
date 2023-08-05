package com.mrcrayfish.vehicle.recipe;

import com.google.common.collect.Lists;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipe;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipes;
import com.mrcrayfish.vehicle.entity.block.WorkstationTileEntity;
import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import com.mrcrayfish.vehicle.item.IDyeable;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class RecipeColorSprayCan extends ShapelessRecipe {
    public RecipeColorSprayCan(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inventory, @NotNull Level world)
    {
        ItemStack dyeableItem = ItemStack.EMPTY;
        List<ItemStack> dyes = Lists.newArrayList();

        for(int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack stack = inventory.getItem(i);
            if(!stack.isEmpty())
            {
                if(stack.getItem() instanceof IDyeable)
                {
                    if(!dyeableItem.isEmpty())
                    {
                        return false;
                    }
                    dyeableItem = stack.copy();
                }
                else
                {
                    if(!stack.is(Tags.Items.DYES))
                    {
                        return false;
                    }
                    dyes.add(stack);
                }
            }
        }

        return !dyeableItem.isEmpty() && !dyes.isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inventory)
    {
        ItemStack dyeableItem = ItemStack.EMPTY;
        List<DyeItem> dyes = Lists.newArrayList();

        for(int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack stack = inventory.getItem(i);
            if(!stack.isEmpty())
            {
                if(stack.getItem() instanceof IDyeable)
                {
                    if(!dyeableItem.isEmpty())
                    {
                        return ItemStack.EMPTY;
                    }
                    dyeableItem = stack.copy();
                }
                else
                {
                    if(!(stack.getItem() instanceof DyeItem))
                    {
                        return ItemStack.EMPTY;
                    }
                    dyes.add((DyeItem) stack.getItem());
                }
            }
        }

        return !dyeableItem.isEmpty() && !dyes.isEmpty() ? IDyeable.dyeStack(dyeableItem, dyes) : ItemStack.EMPTY;
    }

}
