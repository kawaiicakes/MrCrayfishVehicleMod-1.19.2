package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageCycleSeats implements IMessage<MessageCycleSeats>
{
    public MessageCycleSeats() {}

    @Override
    public void encode(MessageCycleSeats message, FriendlyByteBuf buffer) {}

    @Override
    public MessageCycleSeats decode(FriendlyByteBuf buffer)
    {
        return new MessageCycleSeats();
    }

    @Override
    public void handle(MessageCycleSeats message, Supplier<NetworkEvent.Context> supplier)
    {
        if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER)
        {
            supplier.get().enqueueWork(() ->
            {
                ServerPlayerEntity player = supplier.get().getSender();
                if(player != null)
                {
                    ServerPlayHandler.handleCycleSeatsMessage(player, message);
                }
            });
            supplier.get().setPacketHandled(true);
        }
    }
}
