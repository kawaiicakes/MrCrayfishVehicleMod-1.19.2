package com.mrcrayfish.vehicle.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.text.Style;
import net.minecraft.ChatFormatting;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class RenderUtil
{
    /**
     * Draws a textured modal rectangle with more precision than GuiScreen's methods. This will only
     * work correctly if the bound texture is 256x256.
     */
    public static void drawTexturedModalRect(double x, double y, int textureX, int textureY, double width, double height)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(x, y + height, 0).uv(((float) textureX * 0.00390625F), ((float) (textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.vertex(x + width, y + height, 0).uv(((float) (textureX + width) * 0.00390625F), ((float) (textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.vertex(x + width, y, 0).uv(((float) (textureX + width) * 0.00390625F), ((float) textureY * 0.00390625F)).endVertex();
        bufferbuilder.vertex(x + 0, y, 0).uv(((float) textureX * 0.00390625F), ((float) textureY * 0.00390625F)).endVertex();
        tesselator.end();
    }

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors (ARGB format).
     */
    public static void drawGradientRectHorizontal(int left, int top, int right, int bottom, int leftColor, int rightColor)
    {
        float redStart = (float)(leftColor >> 24 & 255) / 255.0F;
        float greenStart = (float)(leftColor >> 16 & 255) / 255.0F;
        float blueStart = (float)(leftColor >> 8 & 255) / 255.0F;
        float alphaStart = (float)(leftColor & 255) / 255.0F;
        float redEnd = (float)(rightColor >> 24 & 255) / 255.0F;
        float greenEnd = (float)(rightColor >> 16 & 255) / 255.0F;
        float blueEnd = (float)(rightColor >> 8 & 255) / 255.0F;
        float alphaEnd = (float)(rightColor & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex((double)right, (double)top, 0).color(greenEnd, blueEnd, alphaEnd, redEnd).endVertex();
        bufferbuilder.vertex((double)left, (double)top, 0).color(greenStart, blueStart, alphaStart, redStart).endVertex();
        bufferbuilder.vertex((double)left, (double)bottom, 0).color(greenStart, blueStart, alphaStart, redStart).endVertex();
        bufferbuilder.vertex((double)right, (double)bottom, 0).color(greenEnd, blueEnd, alphaEnd, redEnd).endVertex();
        tesselator.end();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void scissor(int x, int y, int width, int height) //TODO might need fixing. I believe I rewrote this in a another mod
    {
        Minecraft mc = Minecraft.getInstance();
        int scale = (int) mc.getWindow().getGuiScale();
        GL11.glScissor(x * scale, mc.getWindow().getScreenHeight() - y * scale - height * scale, Math.max(0, width * scale), Math.max(0, height * scale));
    }

    public static IForgeBakedModel getModel(ItemStack stack)
    {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
    }

    public static void renderColoredModel(IForgeBakedModel model, ItemTransforms.TransformType transformType, boolean leftHanded, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int color, int lightTexture, int overlayTexture)
    {
        matrixStack.pushPose();
        net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, leftHanded);
        matrixStack.translate(-0.5, -0.5, -0.5);
        if(!model.isCustomRenderer())
        {
            VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(Sheets.cutoutBlockSheet());
            renderModel(model, ItemStack.EMPTY, color, lightTexture, overlayTexture, matrixStack, vertexBuilder);
        }
        matrixStack.popPose();
    }

    public static void renderDamagedVehicleModel(IForgeBakedModel model, ItemTransforms.TransformType transformType, boolean leftHanded, PoseStack matrixStack, int stage, int color, int lightTexture, int overlayTexture)
    {
        matrixStack.pushPose();
        net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, leftHanded);
        matrixStack.translate(-0.5, -0.5, -0.5);
        if(!model.isCustomRenderer())
        {
            Minecraft mc = Minecraft.getInstance();
            PoseStack.Entry entry = matrixStack.last();
            VertexConsumer vertexBuilder = new MatrixApplyingVertexBuilder(mc.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(stage)), entry.pose(), entry.normal());
            renderModel(model, ItemStack.EMPTY, color, lightTexture, overlayTexture, matrixStack, vertexBuilder);
        }
        matrixStack.popPose();
    }

    public static void renderModel(ItemStack stack, ItemTransforms.TransformType transformType, boolean leftHanded, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int lightTexture, int overlayTexture, IForgeBakedModel model)
    {
        if(!stack.isEmpty())
        {
            matrixStack.pushPose();
            boolean isGui = transformType == ItemTransforms.TransformType.GUI;
            boolean tridentFlag = isGui || transformType == ItemTransforms.TransformType.GROUND || transformType == ItemTransforms.TransformType.FIXED;
            if(stack.getItem() == Items.TRIDENT && tridentFlag)
            {
                model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
            }

            model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, leftHanded);
            matrixStack.translate(-0.5, -0.5, -0.5);
            if(!model.isCustomRenderer() && (stack.getItem() != Items.TRIDENT || tridentFlag))
            {
                RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, false); //TODO test what this flag does
                if(isGui && Objects.equals(renderType, Sheets.translucentCullBlockSheet()))
                {
                    renderType = Sheets.translucentCullBlockSheet();
                }
                VertexConsumer vertexBuilder = ItemRenderer.getFoilBuffer(renderTypeBuffer, renderType, true, stack.hasFoil());
                renderModel(model, stack, -1, lightTexture, overlayTexture, matrixStack, vertexBuilder);
            }
            else
            {
                stack.getItem().getItemStackTileEntityRenderer().renderByItem(stack, transformType, matrixStack, renderTypeBuffer, lightTexture, overlayTexture);
            }

            matrixStack.popPose();
        }
    }

    private static void renderModel(IForgeBakedModel model, ItemStack stack, int color, int lightTexture, int overlayTexture, PoseStack matrixStack, VertexConsumer vertexBuilder)
    {
        Random random = new Random();
        for(Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderQuads(matrixStack, vertexBuilder, model.getQuads(null, direction, random), stack, color, lightTexture, overlayTexture);
        }
        random.setSeed(42L);
        renderQuads(matrixStack, vertexBuilder, model.getQuads(null, null, random), stack, color, lightTexture, overlayTexture);
    }

    private static void renderQuads(PoseStack matrixStack, VertexConsumer vertexBuilder, List<BakedQuad> quads, ItemStack stack, int color, int lightTexture, int overlayTexture)
    {
        boolean useItemColor = !stack.isEmpty() && color == -1;
        PoseStack.Entry entry = matrixStack.last();
        for(BakedQuad quad : quads)
        {
            int tintColor = 0xFFFFFF;
            if(quad.isTinted())
            {
                if(useItemColor)
                {
                    tintColor = Minecraft.getInstance().getItemColors().getColor(stack, quad.getTintIndex());
                }
                else
                {
                    tintColor = color;
                }
            }
            float red = (float) (tintColor >> 16 & 255) / 255.0F;
            float green = (float) (tintColor >> 8 & 255) / 255.0F;
            float blue = (float) (tintColor & 255) / 255.0F;
            vertexBuilder.addVertexData(entry, quad, red, green, blue, lightTexture, overlayTexture, true);
        }
    }

    public static List<Component> lines(FormattedText text, int maxWidth)
    {
        List<FormattedText> lines = Minecraft.getInstance().font.getSplitter().splitLines(text, maxWidth, Style.EMPTY);
        return lines.stream().map(t -> MutableComponent.create(new LiteralContents(t.getString())).withStyle(ChatFormatting.GRAY)).collect(Collectors.toList());
    }
}
