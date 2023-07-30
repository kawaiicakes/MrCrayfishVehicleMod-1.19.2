package com.mrcrayfish.vehicle.common.entity;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageSyncHeldVehicle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class HeldVehicleDataHandler
{
    @CapabilityInject(IHeldVehicle.class)
    public static final Capability<IHeldVehicle> CAPABILITY_HELD_VEHICLE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IHeldVehicle.class, new Storage(), HeldVehicle::new);
        MinecraftForge.EVENT_BUS.register(new HeldVehicleDataHandler());
    }

    public static boolean isHoldingVehicle(Player player)
    {
        IHeldVehicle handler = getHandler(player);
        if(handler != null)
        {
            return !handler.getVehicleTag().isEmpty();
        }
        return false;
    }

    public static CompoundTag getHeldVehicle(Player player)
    {
        IHeldVehicle handler = getHandler(player);
        if(handler != null)
        {
            return handler.getVehicleTag();
        }
        return new CompoundTag();
    }

    public static void setHeldVehicle(Player player, CompoundTag vehicleTag)
    {
        IHeldVehicle handler = getHandler(player);
        if(handler != null)
        {
            handler.setVehicleTag(vehicleTag);
        }
        if(!player.level.isClientSide)
        {
            PacketHandler.getPlayChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new MessageSyncHeldVehicle(player.getId(), vehicleTag));
        }
    }

    @Nullable
    public static IHeldVehicle getHandler(Player player)
    {
        return player.getCapability(CAPABILITY_HELD_VEHICLE, Direction.DOWN).orElse(null);
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            event.addCapability(new ResourceLocation(Reference.MOD_ID, "held_vehicle"), new Provider());
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        if(event.isWasDeath())
            return;

        CompoundTag vehicleTag = getHeldVehicle(event.getOriginal());
        if(!vehicleTag.isEmpty())
        {
            setHeldVehicle(event.getPlayer(), vehicleTag);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
        if(event.getTarget() instanceof Player)
        {
            Player player = (Player) event.getTarget();
            CompoundTag vehicleTag = getHeldVehicle(player);
            PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()), new MessageSyncHeldVehicle(player.getId(), vehicleTag));
        }
    }

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof Player && !event.getWorld().isClientSide)
        {
            Player player = (Player) entity;
            CompoundTag vehicleTag = getHeldVehicle(player);
            PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageSyncHeldVehicle(player.getId(), vehicleTag));
        }
    }

    public interface IHeldVehicle
    {
        void setVehicleTag(CompoundTag tagCompound);
        CompoundTag getVehicleTag();
    }

    public static class HeldVehicle implements IHeldVehicle
    {
        private CompoundTag compound = new CompoundTag();

        @Override
        public void setVehicleTag(CompoundTag tagCompound)
        {
            this.compound = tagCompound;
        }

        @Override
        public CompoundTag getVehicleTag()
        {
            return compound;
        }
    }

    public static class Storage implements Capability.IStorage<IHeldVehicle>
    {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IHeldVehicle> capability, IHeldVehicle instance, Direction side)
        {
            return instance.getVehicleTag();
        }

        @Override
        public void readNBT(Capability<IHeldVehicle> capability, IHeldVehicle instance, Direction side, INBT nbt)
        {
            instance.setVehicleTag((CompoundTag) nbt);
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag>
    {
        final IHeldVehicle INSTANCE = CAPABILITY_HELD_VEHICLE.getDefaultInstance();

        @Override
        public CompoundTag serializeNBT()
        {
            return (CompoundTag) CAPABILITY_HELD_VEHICLE.getStorage().writeNBT(CAPABILITY_HELD_VEHICLE, INSTANCE, null);
        }

        @Override
        public void deserializeNBT(CompoundTag compound)
        {
            CAPABILITY_HELD_VEHICLE.getStorage().readNBT(CAPABILITY_HELD_VEHICLE, INSTANCE, null, compound);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
        {
            return CAPABILITY_HELD_VEHICLE.orEmpty(cap, LazyOptional.of(() -> INSTANCE));
        }
    }
}
