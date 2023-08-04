package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

public abstract class TileEntitySynced extends BlockEntity implements EntityBlock, IForgeBlockEntity
{
    //Don't override this!
    public TileEntitySynced(@NotNull BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Any class extending <code>TileEntitySynced</code>> MUST override this method such that its body contains
     * the constructor of the subclass. The constructor of the subclass MUST call <code>super</code> and pass
     * arguments appropriately.
     * <br>
     * Similarly, any class extending subclasses of <code>TileEntitySynced</code> must copy the described behaviour.
     * Any method making a call to the constructor of <code>TileEntitySynced</code> or anything extending it -
     * specifically in other classes - should instead consider doing so inside an override of this method.
     * @param pos   a <code>BlockPos</code> automatically passed by the game.
     * @param state a <code>BlockState</code> automatically passed by the game.
     * @return      a new <code>BlockEntity</code> instance representing this object.
     */
    @NotNull
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state);

    @Override
    @ParametersAreNonnullByDefault
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type);

    public void syncToClient()
    {
        this.setChanged();
        TileEntityUtil.sendUpdatePacket(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag()
    {
        return this.saveWithFullMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        this.load(Objects.requireNonNull(pkt.getTag()));
    }

    /**
     * Gets a {@link CompoundTag} that can be used to store custom data for this block entity.
     * It will be written, and read from disc, so it persists over world saves.
     *
     * @return A compound tag for custom persistent data
     */
    @Override
    public @NotNull CompoundTag getPersistentData() {
        return super.getPersistentData();
    }
}