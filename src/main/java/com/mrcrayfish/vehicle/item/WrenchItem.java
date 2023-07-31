package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class WrenchItem extends Item
{
    public WrenchItem(Item.Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
    {
        if(Screen.hasShiftDown())
        {
            list.addAll(RenderUtil.lines(new TranslatableContents(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            list.add(new TranslatableContents("vehicle.info_help")).withStyle(ChatFormatting.YELLOW));
        }
    }
}