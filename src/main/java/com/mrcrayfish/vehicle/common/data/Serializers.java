package com.mrcrayfish.vehicle.common.data;

import com.mrcrayfish.obfuscate.common.data.IDataSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class Serializers
{
    public static final IDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = new IDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf buffer, Optional<BlockPos> optional) {
            buffer.writeBoolean(optional.isPresent());
            optional.ifPresent(buffer::writeBlockPos);
        }

        @Override
        public Optional<BlockPos> read(FriendlyByteBuf buffer) {
            if (buffer.readBoolean()) {
                return Optional.of(buffer.readBlockPos());
            }
            return Optional.empty();
        }

        @Override
        public CompoundTag write(Optional<BlockPos> value) {
            CompoundTag compound = new CompoundTag();
            compound.putBoolean("Present", value.isPresent());
            value.ifPresent(blockPos -> compound.putLong("BlockPos", value.get().asLong()));
            return compound;
        }

        @Override
        public Optional<BlockPos> read(Tag tag) {
            return Optional.empty();
        }
    };
}
