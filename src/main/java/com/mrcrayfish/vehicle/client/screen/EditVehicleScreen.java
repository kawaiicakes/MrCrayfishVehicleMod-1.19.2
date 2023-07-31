package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Collections;

/**
 * Author: MrCrayfish
 */
public class EditVehicleScreen extends ContainerScreen<EditVehicleContainer>
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("vehicle:textures/gui/edit_vehicle.png");

    private final PlayerInventory playerInventory;
    private final IInventory vehicleInventory;
    private final CachedVehicle cachedVehicle;

    private Framebuffer framebuffer;
    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public EditVehicleScreen(EditVehicleContainer container, PlayerInventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.vehicleInventory = container.getVehicleInventory();
        this.cachedVehicle = new CachedVehicle(container.getVehicle());
        this.imageHeight = 184;
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, left, top, 0, 0, this.imageWidth, this.imageHeight);

        if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
        {
            if(this.vehicleInventory.getItem(0).isEmpty())
            {
                this.blit(matrixStack, left + 8, top + 17, 176, 0, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(0).isEmpty())
        {
            this.blit(matrixStack, left + 8, top + 17, 176, 32, 16, 16);
        }

        if(this.cachedVehicle.getProperties().canChangeWheels())
        {
            if(this.vehicleInventory.getItem(1).isEmpty())
            {
                this.blit(matrixStack, left + 8, top + 35, 176, 16, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(1).isEmpty())
        {
            this.blit(matrixStack, left + 8, top + 35, 176, 32, 16, 16);
        }

        if(this.framebuffer != null)
        {
            this.framebuffer.bindRead();
            int startX = left + 26;
            int startY = top + 17;
            RenderSystem.disableCull();
            Matrix4f pose = matrixStack.last().pose();
            BufferBuilder builder = Tessellator.getInstance().getBuilder();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(pose, startX, startY, this.getBlitOffset()).uv(0, 1).endVertex();
            builder.vertex(pose, startX, startY + 70, this.getBlitOffset()).uv(0, 0).endVertex();
            builder.vertex(pose, startX + 142, startY + 70, this.getBlitOffset()).uv(1, 0).endVertex();
            builder.vertex(pose, startX + 142, startY, this.getBlitOffset()).uv(1, 1).endVertex();
            builder.end();
            RenderSystem.enableAlphaTest();
            WorldVertexBufferUploader.end(builder);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.font.draw(matrixStack, this.title.getString(), 8, 6, 4210752);
        minecraft.font.draw(matrixStack, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752);

        if(this.showHelp)
        {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            minecraft.font.draw(matrixStack, I18n.get("container.edit_vehicle.window_help"), 56, 38, 0xFFFFFF);
            RenderSystem.popMatrix();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderVehicleToBuffer(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        Matrix4f projectionMatrix = Matrix4f.perspective(30, 142.0F / 70.0F, 0.5F, 200.0F);
        RenderSystem.multMatrix(projectionMatrix);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderHelper.setupLevel(matrixStack.last().pose());

        AbstractVehicleRenderer renderer = this.cachedVehicle.getRenderer();
        if(renderer != null)
        {
            this.bindFrameBuffer();

            matrixStack.pushPose();
            PoseStack.Entry last = matrixStack.last();
            last.pose().setIdentity();
            last.normal().setIdentity();
            matrixStack.translate(0, -20, -150);
            matrixStack.translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
            matrixStack.translate(0, this.windowY - (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);

            Quaternion quaternion = Axis.POSITIVE_X.rotationDegrees(20F);
            quaternion.mul(Axis.NEGATIVE_X.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
            quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));
            quaternion.mul(Axis.POSITIVE_Y.rotationDegrees(45F));
            matrixStack.mulPose(quaternion);

            matrixStack.scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
            matrixStack.scale(22F, 22F, 22F);

            Transform position = this.cachedVehicle.getProperties().getDisplayTransform();
            matrixStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
            matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees((float) position.getRotX()));
            matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees((float) position.getRotY()));
            matrixStack.mulPose(Axis.POSITIVE_Z.rotationDegrees((float) position.getRotZ()));
            matrixStack.translate(position.getX(), position.getY(), position.getZ());

            MultiBufferSource.Impl renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
            renderer.setupTransformsAndRender(this.menu.getVehicle(), matrixStack, renderTypeBuffer, Minecraft.getInstance().getFrameTime(), 15728880);
            renderTypeBuffer.endBatch();

            matrixStack.popPose();

            this.unbindFrameBuffer();
        }

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.popMatrix();
        RenderHelper.setupFor3DItems();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(scroll < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scroll > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseGrabbed = false;
                this.windowX += (mouseX - this.mouseClickedX);
                this.windowY -= (mouseY - this.mouseClickedY);
            }
            else if(mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseGrabbed = false;
                this.windowRotationX += (mouseX - this.mouseClickedX);
                this.windowRotationY -= (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderVehicleToBuffer(matrixStack, mouseX, mouseY, partialTicks);
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        this.renderTooltip(matrixStack, mouseX, mouseY);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.vehicleInventory.getItem(0).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 16, 18, 18))
            {
                if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
                {
                    this.renderTooltip(matrixStack, Lists.transform(Collections.singletonList(MutableComponent.create(new LiteralContents("Engine")), Component::getVisualOrderText), mouseX, mouseY); //TODO localise
                }
                else
                {
                    this.renderTooltip(matrixStack, Lists.transform(Arrays.asList(MutableComponent.create(new LiteralContents("Engine"), MutableComponent.create(new LiteralContents(ChatFormatting.GRAY + "Not applicable")), Component::getVisualOrderText), mouseX, mouseY); //TODO localise
                }
            }
        }

        if(this.vehicleInventory.getItem(1).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 34, 18, 18))
            {
                if(this.cachedVehicle.getProperties().canChangeWheels())
                {
                    this.renderTooltip(matrixStack, Lists.transform(Collections.singletonList(MutableComponent.create(new LiteralContents("Wheels")), Component::getVisualOrderText), mouseX, mouseY);
                }
                else
                {
                    this.renderTooltip(matrixStack, Lists.transform(Arrays.asList(MutableComponent.create(new LiteralContents("Wheels"), MutableComponent.create(new LiteralContents(ChatFormatting.GRAY + "Not applicable")), Component::getVisualOrderText), mouseX, mouseY);
                }
            }
        }
    }

    private void bindFrameBuffer()
    {
        Minecraft minecraft = Minecraft.getInstance();
        MainWindow window = minecraft.getWindow();
        int windowWidth = (int) (142 * window.getGuiScale());
        int windowHeight = (int) (70 * window.getGuiScale());
        if(this.framebuffer == null)
        {
            this.framebuffer = new Framebuffer(windowWidth, windowHeight, true, Minecraft.ON_OSX);
            this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
        else if(this.framebuffer.width != windowWidth || this.framebuffer.height != windowHeight)
        {
            this.framebuffer.destroyBuffers();
            this.framebuffer.resize(windowWidth, windowHeight, Minecraft.ON_OSX);
        }
        this.framebuffer.clear(Minecraft.ON_OSX);
        this.framebuffer.bindWrite(true);
    }

    private void unbindFrameBuffer()
    {
        if(this.framebuffer != null)
        {
            this.framebuffer.unbindWrite();
        }
        // Rebind the main buffer
        this.minecraft.getMainRenderTarget().bindWrite(true);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        if(this.framebuffer != null)
        {
            this.framebuffer.destroyBuffers();
        }
    }
}
