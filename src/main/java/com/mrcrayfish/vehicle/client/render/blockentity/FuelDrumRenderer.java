package com.mrcrayfish.vehicle.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mrcrayfish.vehicle.tileentity.FuelDrumTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.mojang.math.Matrix4f;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: MrCrayfish
 */
public class FuelDrumRenderer implements BlockEntityRenderer<FuelDrumTileEntity> {
    public static final RenderType LABEL_BACKGROUND = RenderType.create("vehicle:fuel_drum_label_background", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().createCompositeState(false));
    public static final RenderType LABEL_FLUID = RenderType.create("vehicle:fuel_drum_label_fluid", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, true)).createCompositeState(false));
    private final BlockEntityRenderDispatcher renderer;

    public FuelDrumRenderer(BlockEntityRendererProvider.Context context) {
        this.renderer = context.getBlockEntityRenderDispatcher();
    }
    @ParametersAreNonnullByDefault
    @Override
    public void render(FuelDrumTileEntity fuelDrumTileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int lightTexture, int overlayTexture)
    {
        assert Minecraft.getInstance().player != null;
        if(Minecraft.getInstance().player.isCrouching())
        {
            if(fuelDrumTileEntity.hasFluid() && this.renderer.cameraHitResult.getType() == HitResult.Type.BLOCK)
            {
                BlockHitResult result = (BlockHitResult) this.renderer.cameraHitResult;
                if(result.getBlockPos().equals(fuelDrumTileEntity.getBlockPos()))
                {
                    this.drawFluidLabel(this.renderer, fuelDrumTileEntity.getFluidTank(), matrixStack, renderTypeBuffer);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void drawFluidLabel(BlockEntityRenderDispatcher fontRendererIn, FluidTank tank, PoseStack matrixStack, MultiBufferSource renderTypeBuffer)
    {
        if(tank.getFluid().isEmpty())
            return;

        FluidStack stack = tank.getFluid();
        AtomicReference<ResourceLocation> stackLocation = new AtomicReference<>();
        stack.getFluid().getFluidType().initializeClient((iClient) -> stackLocation.set(iClient.getFlowingTexture()));
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(stackLocation.get());
        if(sprite != null)
        {
            float level = tank.getFluidAmount() / (float) tank.getCapacity();
            float width = 30F;
            float fuelWidth = width * level;
            float remainingWidth = width - fuelWidth;
            float offsetWidth = width / 2.0F;

            matrixStack.pushPose();
            matrixStack.translate(0.5, 1.25, 0.5);
            matrixStack.mulPose(this.renderer.camera.rotation());
            matrixStack.scale(-0.025F, -0.025F, 0.025F);

            VertexConsumer backgroundBuilder = renderTypeBuffer.getBuffer(LABEL_BACKGROUND);

            /* Background */
            Matrix4f matrix = matrixStack.last().pose();
            backgroundBuilder.vertex(matrix, -offsetWidth - 1.0F, -2.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth - 1.0F, 5.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + width + 1.0F, 5.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + width + 1.0F, -2.0F, -0.01F).color(0.5F, 0.5F, 0.5F, 1.0F).endVertex();

            matrixStack.translate(0, 0, -0.05);

            /* Remaining */
            matrix = matrixStack.last().pose();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth, -1.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth, 4.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth + remainingWidth, 4.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();
            backgroundBuilder.vertex(matrix, -offsetWidth + fuelWidth + remainingWidth, -1.0F, 0.0F).color(0.4F, 0.4F, 0.4F, 1.0F).endVertex();

            float minU = sprite.getU0();
            float maxU = minU + (sprite.getU1() - minU) * level;
            float minV = sprite.getV0();
            float maxV = minV + (sprite.getV1() - minV) * 4 * 0.0625F;

            /* Fluid Texture */
            VertexConsumer fluidBuilder = renderTypeBuffer.getBuffer(LABEL_FLUID);
            fluidBuilder.vertex(matrix, -offsetWidth, -1.0F, 0.0F).uv(minU, maxV).endVertex();
            fluidBuilder.vertex(matrix, -offsetWidth, 4.0F, 0.0F).uv(minU, minV).endVertex();
            fluidBuilder.vertex(matrix, -offsetWidth + fuelWidth, 4.0F, 0.0F).uv(maxU, minV).endVertex();
            fluidBuilder.vertex(matrix, -offsetWidth + fuelWidth, -1.0F, 0.0F).uv(maxU, maxV).endVertex();

            /* Fluid Name */
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            String name = stack.getDisplayName().getString();
            int nameWidth = fontRendererIn.font.width(name) / 2;
            fontRendererIn.font.draw(matrixStack, name, -nameWidth, -14, -1);

            matrixStack.popPose();
        }
    }
}
