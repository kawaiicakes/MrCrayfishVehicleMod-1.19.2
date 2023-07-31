package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.entity.EntityJack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class JackRenderer extends EntityRenderer<EntityJack>
{
    public JackRenderer(EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityJack entity)
    {
        return null;
    }

    @Override
    public void render(EntityJack jack, float p_225623_2_, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light) {}
}
