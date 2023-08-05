package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ItemTrafficCone extends BlockItem
{
    public ItemTrafficCone(Block block)
    {
        super(block, new Item.Properties().tab(VehicleMod.CREATIVE_TAB));
    }

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack)
    {
        return EquipmentSlot.HEAD;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        if(Screen.hasShiftDown())
        {
            tooltip.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            tooltip.add(Component.translatable("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }
}
