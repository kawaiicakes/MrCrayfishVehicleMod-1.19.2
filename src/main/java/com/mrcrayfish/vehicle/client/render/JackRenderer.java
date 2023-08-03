package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.entity.EntityJack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: MrCrayfish
 */
@ParametersAreNonnullByDefault
public class JackRenderer extends EntityRenderer<EntityJack>
{
    public JackRenderer(EntityRendererProvider<EntityJack> renderManager)
    {
        super((EntityRendererProvider.Context) renderManager);
    }

    @Override //TODO proper impl of this method
    public @NotNull ResourceLocation getTextureLocation(EntityJack entity)
    {
        return null;
    }

    @Override
    public void render(EntityJack jack, float p_225623_2_, float partialTicks, @Nullable PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light) {}
}
