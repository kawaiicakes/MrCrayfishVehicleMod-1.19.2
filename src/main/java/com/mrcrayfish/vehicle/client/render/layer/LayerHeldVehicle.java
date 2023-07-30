package com.mrcrayfish.vehicle.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.handler.HeldVehicleHandler;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class LayerHeldVehicle extends LayerRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    private VehicleEntity vehicle;
    private CachedVehicle cachedVehicle;
    private float width = -1.0F;

    public LayerHeldVehicle(IEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super(renderer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light, AbstractClientPlayer player, float v, float v1, float partialTicks, float v3, float v4, float v5)
    {
        CompoundTag tagCompound = HeldVehicleDataHandler.getHeldVehicle(player);
        if(!tagCompound.isEmpty())
        {
            if(this.cachedVehicle == null)
            {
                Optional<EntityType<?>> optional = EntityType.byString(tagCompound.getString("id"));
                if(optional.isPresent())
                {
                    EntityType<?> entityType = optional.get();
                    Entity entity = entityType.create(player.level);
                    if(entity instanceof VehicleEntity)
                    {
                        entity.load(tagCompound);
                        this.vehicle = (VehicleEntity) entity;
                        this.width = entity.getBbWidth();
                        this.cachedVehicle = new CachedVehicle(this.vehicle);
                    }
                }
            }
            if(this.cachedVehicle != null)
            {
                matrixStack.pushPose();
                HeldVehicleHandler.AnimationCounter counter = HeldVehicleHandler.idToCounter.get(player.getUUID());
                if(counter != null)
                {
                    float width = this.width / 2;
                    matrixStack.translate(0F, 1F - counter.getProgress(partialTicks), -0.5F * Math.sin(Math.PI * counter.getProgress(partialTicks)) - width * (1.0F - counter.getProgress(partialTicks)));
                }
                Vec3 heldOffset = this.cachedVehicle.getProperties().getHeldOffset();
                matrixStack.translate(heldOffset.x * 0.0625D, heldOffset.y * 0.0625D, heldOffset.z * 0.0625D);
                matrixStack.mulPose(Axis.POSITIVE_X.rotationDegrees(180F));
                matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(-90F));
                matrixStack.translate(0F, player.isCrouching() ? 0.3125F : 0.5625F, 0F);
                ((AbstractVehicleRenderer<VehicleEntity>)this.cachedVehicle.getRenderer()).setupTransformsAndRender(this.vehicle, matrixStack, renderTypeBuffer, partialTicks, light);
                matrixStack.popPose();
            }
        }
        else
        {
            this.cachedVehicle = null;
            this.width = -1.0F;
        }
    }
}
