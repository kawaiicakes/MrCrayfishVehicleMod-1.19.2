package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageSyncHeldVehicle implements IMessage<MessageSyncHeldVehicle>
{
    private int entityId;
    private CompoundTag vehicleTag;

    public MessageSyncHeldVehicle() {}

    public MessageSyncHeldVehicle(int entityId, CompoundTag vehicleTag)
    {
        this.entityId = entityId;
        this.vehicleTag = vehicleTag;
    }

    @Override
    public void encode(MessageSyncHeldVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(message.entityId);
        buffer.writeNbt(message.vehicleTag);
    }

    @Override
    public MessageSyncHeldVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageSyncHeldVehicle(buffer.readVarInt(), buffer.readNbt());
    }

    @Override
    public void handle(MessageSyncHeldVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
        {
            IMessage.enqueueTask(supplier, () -> ClientPlayHandler.handleSyncHeldVehicle(message));
        }
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public CompoundTag getVehicleTag()
    {
        return this.vehicleTag;
    }
}
