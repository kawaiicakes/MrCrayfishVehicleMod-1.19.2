package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class Spacer extends AbstractWidget
{
    public Spacer(int widthIn)
    {
        super(0, 0, widthIn, 20, Component.empty());
    }

    public static Spacer of(int width)
    {
        return new Spacer(width);
    }

    @Override
    public void renderButton(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        GuiComponent.fill(matrixStack,
                this.x + this.width / 2, this.y,
                this.x + this.width / 2 + 1,
                this.y + this.height,
                0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return false;
    }

    @Override //TODO add narration
    public void updateNarration(@NotNull NarrationElementOutput p_169152_) {
    }
}
