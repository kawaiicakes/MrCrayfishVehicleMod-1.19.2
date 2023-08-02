package com.mrcrayfish.vehicle.entity.trailer;

import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageAttachTrailer;
import com.mrcrayfish.vehicle.network.message.MessageEntityFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;

/**
 * Author: MrCrayfish
 */
public class FluidTrailerEntity extends TrailerEntity implements IEntityAdditionalSpawnData
{
    protected FluidTank tank = new FluidTank(FluidAttributes.BUCKET_VOLUME * 100)
    {
        @Override
        protected void onContentsChanged()
        {
            syncTank();
        }
    };

    public FluidTrailerEntity(EntityType<? extends FluidTrailerEntity> type, Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if(!level.isClientSide && !player.isCrouching())
        {
            if(FluidUtil.interactWithFluidHandler(player, hand, tank))
            {
                return InteractionResult.SUCCESS;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        if(compound.contains("Tank", Tag.TAG_COMPOUND))
        {
            this.tank.readFromNBT(compound.getCompound("Tank"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        CompoundTag tankTag = new CompoundTag();
        this.tank.writeToNBT(tankTag);
        compound.put("Tank", tankTag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return LazyOptional.of(() -> this.tank).cast();
        return super.getCapability(cap);
    }

    public FluidTank getTank()
    {
        return this.tank;
    }

    public void syncTank()
    {
        if(!this.level.isClientSide)
        {
            PacketHandler.getPlayChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new MessageEntityFluid(this.getId(), this.tank.getFluid()));
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        super.writeSpawnData(buffer);
        buffer.writeNbt(this.tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        super.readSpawnData(buffer);
        this.tank.readFromNBT(buffer.readNbt());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerInteractionBoxes()
    {
        EntityRayTracer.instance().registerInteractionBox(ModEntities.FLUID_TRAILER.get(), () -> {
            return createScaledBoundingBox(-7.0, -0.5, 12.0, 7.0, 3.5, 24.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().sendToServer(new MessageAttachTrailer(entity.getId()));
                Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);
    }
}
