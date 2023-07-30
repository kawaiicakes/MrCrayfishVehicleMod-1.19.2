package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessagePickupVehicle implements IMessage<MessagePickupVehicle>
{
    private int entityId;

    public MessagePickupVehicle()
    {
    }

    public MessagePickupVehicle(Entity targetEntity)
    {
        this.entityId = targetEntity.getId();
    }

    public MessagePickupVehicle(int entityId)
    {
        this.entityId = entityId;
    }

    @Override
    public void encode(MessagePickupVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
    }

    @Override
    public MessagePickupVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessagePickupVehicle(buffer.readInt());
    }

    @Override
    public void handle(MessagePickupVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayerEntity player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handlePickupVehicleMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}