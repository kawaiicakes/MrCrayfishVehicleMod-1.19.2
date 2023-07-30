package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageThrottle implements IMessage<MessageThrottle>
{
	private float power;

	public MessageThrottle() {}

	public MessageThrottle(float power)
	{
		this.power = power;
	}

	@Override
	public void encode(MessageThrottle message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.power);
	}

	@Override
	public MessageThrottle decode(FriendlyByteBuf buffer)
	{
		return new MessageThrottle(buffer.readFloat());
	}

	@Override
	public void handle(MessageThrottle message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleThrottleMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getPower()
	{
		return this.power;
	}
}
