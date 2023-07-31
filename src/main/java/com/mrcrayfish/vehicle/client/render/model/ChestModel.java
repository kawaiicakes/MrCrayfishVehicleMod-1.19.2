package com.mrcrayfish.vehicle.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Calendar;

/**
 * Author: MrCrayfish
 */
public class ChestModel
{
    private final ModelPart top;
    private final ModelPart base;
    private final ModelPart latch;
    private final boolean christmas;

    public ChestModel()
    {
        Calendar c = Calendar.getInstance();
        this.christmas = c.get(Calendar.MONTH) + 1 == 12 && c.get(Calendar.DAY_OF_MONTH) >= 24 && c.get(Calendar.DAY_OF_MONTH) <= 26;
        this.base = new ModelPart(64, 64, 0, 19);
        this.base.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
        this.top = new ModelPart(64, 64, 0, 0);
        this.top.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
        this.top.y = 9.0F;
        this.top.z = 1.0f;
        this.latch = new ModelPart(64, 64, 0, 0);
        this.latch.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.latch.y = 8.0F;
    }

    public void render(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, Pair<Float, Float> lidProgressPair, int light, float partialTicks)
    {
        float lidProgress = Mth.lerp(partialTicks, lidProgressPair.getLeft(), lidProgressPair.getRight());
        lidProgress = 1.0F - lidProgress;
        lidProgress = 1.0F - lidProgress * lidProgress * lidProgress;
        Material renderMaterial = this.christmas ? Sheets.CHEST_XMAS_LOCATION : Sheets.CHEST_LOCATION;
        VertexConsumer builder = renderMaterial.buffer(renderTypeBuffer, RenderType::entityCutout);
        this.renderChest(matrixStack, builder, lidProgress, light);
    }

    private void renderChest(PoseStack matrixStack, VertexConsumer builder, float openProgress, int lightTexture)
    {
        this.top.xRot = -(openProgress * ((float) Math.PI / 2F));
        this.latch.xRot = this.top.xRot;
        this.top.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
        this.latch.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
        this.base.render(matrixStack, builder, lightTexture, OverlayTexture.NO_OVERLAY);
    }
}
