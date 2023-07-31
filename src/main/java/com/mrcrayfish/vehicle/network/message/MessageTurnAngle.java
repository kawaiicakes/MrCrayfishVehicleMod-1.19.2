package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageTurnAngle implements IMessage<MessageTurnAngle>
{
	private float angle;

	public MessageTurnAngle() {}

	public MessageTurnAngle(float angle)
	{
		this.angle = angle;
	}

	@Override
	public void encode(MessageTurnAngle message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.angle);
	}

	@Override
	public MessageTurnAngle decode(FriendlyByteBuf buffer)
	{
		return new MessageTurnAngle(buffer.readFloat());
	}

	@Override
	public void handle(MessageTurnAngle message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleTurnAngleMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getAngle()
	{
		return this.angle;
	}
}
