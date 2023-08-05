package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.crafting.FluidEntry;
import com.mrcrayfish.vehicle.inventory.container.FluidExtractorContainer;
import com.mrcrayfish.vehicle.entity.block.FluidExtractorTileEntity;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorScreen extends AbstractContainerScreen<FluidExtractorContainer>
{
    private static final ResourceLocation GUI = new ResourceLocation("vehicle:textures/gui/fluid_extractor.png");

    private final Inventory playerInventory;
    private final FluidExtractorTileEntity fluidExtractorTileEntity;

    public FluidExtractorScreen(FluidExtractorContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.fluidExtractorTileEntity = container.getFluidExtractor();
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack); //TODO do I need this?
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.fluidExtractorTileEntity.getFluidStackTank() != null)
        {
            FluidStack stack = this.fluidExtractorTileEntity.getFluidStackTank();
            if(this.isMouseWithinRegion(startX + 127, startY + 14, 16, 59, mouseX, mouseY))
            {
                if(stack.getAmount() > 0)
                {
                    this.renderTooltip(matrixStack, Lists.transform(Arrays.asList(MutableComponent.create(new LiteralContents(stack.getDisplayName().getString())), MutableComponent.create(new LiteralContents(ChatFormatting.GRAY.toString() + this.fluidExtractorTileEntity.getFluidLevel() + "/" + this.fluidExtractorTileEntity.getCapacity() + " mB"))), Component::getVisualOrderText), mouseX, mouseY);
                }
                else
                {
                    this.renderTooltip(matrixStack, Lists.transform(Collections.singletonList(MutableComponent.create(new LiteralContents("No Fluid"))), Component::getVisualOrderText), mouseX, mouseY);
                }
            }
        }

        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack matrixStack, int mouseX, int mouseY)
    {
        assert this.minecraft != null;
        this.minecraft.font.draw(matrixStack, this.fluidExtractorTileEntity.getDisplayName().getString(), 8, 6, 4210752);
        this.minecraft.font.draw(matrixStack, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752);
    }

    @Override
    protected void renderBg(@NotNull PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); //TODO: color4f
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindForSetup(GUI);
        this.blit(matrixStack, startX, startY, 0, 0, this.imageWidth, this.imageHeight);

        if(this.fluidExtractorTileEntity.getRemainingFuel() >= 0)
        {
            int remainingFuel = (int) (14 * (this.fluidExtractorTileEntity.getRemainingFuel() / (double) this.fluidExtractorTileEntity.getFuelMaxProgress()));
            this.blit(matrixStack, startX + 64, startY + 53 + 14 - remainingFuel, 176, 14 - remainingFuel, 14, remainingFuel + 1);
        }

        if(this.fluidExtractorTileEntity.canExtract())
        {
            int left = startX + 93 + 1;
            int top = startY + 34;
            int right = left + 23 - 1;
            int bottom = top + 16;
            FluidEntry fluidExtract = this.fluidExtractorTileEntity.getCurrentRecipe().getResult();
            int fluidColor = -1;
            if(fluidExtract != null)
            {
                fluidColor = (255 << 24) | FluidUtils.getAverageFluidColor(fluidExtract.fluid());
            }
            RenderUtil.drawGradientRectHorizontal(left, top, right, bottom, -1, fluidColor);
            this.blit(matrixStack, startX + 93, startY + 34, 176, 14, 23, 16);
            double extractionPercentage = this.fluidExtractorTileEntity.getExtractionProgress() / (double) Config.SERVER.extractorExtractTime.get();
            int extractionProgress = (int) (22 * extractionPercentage + 1);
            this.blit(matrixStack, startX + 93 + extractionProgress, startY + 34, 93 + extractionProgress, 34, 23 - extractionProgress, 17);
        }

        this.drawFluidTank(this.fluidExtractorTileEntity.getFluidStackTank(), matrixStack, startX + 127, startY + 14, this.fluidExtractorTileEntity.getFluidLevel() / (double) this.fluidExtractorTileEntity.getCapacity(), 59);
    }

    @SuppressWarnings("SameParameterValue") //TODO: height is always the same
    private void drawFluidTank(FluidStack fluid, PoseStack matrixStack, int x, int y, double level, int height)
    {
        FluidUtils.drawFluidTankInGUI(fluid, x, y, level, height);
        Minecraft.getInstance().getTextureManager().bindForSetup(GUI);
        this.blit(matrixStack, x, y, 176, 44, 16, 59);
    }

    @SuppressWarnings("SameParameterValue") //TODO: again, some params are always the same
    private boolean isMouseWithinRegion(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
