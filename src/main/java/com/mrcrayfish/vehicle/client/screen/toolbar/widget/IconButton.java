package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import com.mojang.blaze3d.vertex.*;
import com.mrcrayfish.vehicle.client.screen.toolbar.IToolbarLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class IconButton extends Button implements IToolbarLabel
{
    private IconProvider icon;
    private Component label;

    public IconButton(int width, int height, @Nullable IconProvider icon, Component label, OnPress onPress)
    {
        super(0, 0, width, height, MutableComponent.create(ComponentContents.EMPTY), onPress);
        this.icon = icon;
        this.label = label;
    }

    public IconButton setLabel(Component label)
    {
        this.label = label;
        return this;
    }

    @Override
    public Component getLabel()
    {
        return this.label;
    }

    public IconButton setIcon(@Nullable IconProvider icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public void renderButton(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.getBlockEntityRenderDispatcher().font;
        int combinedWidth = this.icon != null ? 10 : 0;
        String message = this.getMessage().getContents().toString();
        if(!message.isEmpty())
        {
            combinedWidth += font.width(message);
            if(this.icon != null)
            {
                combinedWidth += 4;
            }
        }
        if(this.icon != null)
        {
            mc.getTextureManager().bindForSetup(this.icon.getTextureLocation());
            this.drawIcon(this.x + this.width / 2 - combinedWidth / 2, this.y + 5, this.icon.getU(), this.icon.getV());
        }
        if(!message.isEmpty())
        {
            font.drawShadow(matrixStack, message, this.x + (float) this.width / 2 - (float) combinedWidth / 2 + 10 + (this.icon == null ? 0 : 4), this.y + 6, 0xFFFFFF);
        }
    }

    protected void drawIcon(int x, int y, int u, int v)
    {
        int size = 10;
        float uScale = 1.0F / 100.0F;
        float vScale = 1.0F / 100.0F;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(x, y + size, 0).uv(u * uScale, (v + size) * vScale).endVertex();
        buffer.vertex(x + size, y + size, 0).uv((u + size) * uScale, (v + size) * vScale).endVertex();
        buffer.vertex(x + size, y, 0).uv((u + size) * uScale, v * vScale).endVertex();
        buffer.vertex(x, y, 0).uv(u * uScale, v * vScale).endVertex();
        tesselator.end();
    }

    public interface IconProvider
    {
        ResourceLocation getTextureLocation();

        int getU();

        int getV();
    }
}
