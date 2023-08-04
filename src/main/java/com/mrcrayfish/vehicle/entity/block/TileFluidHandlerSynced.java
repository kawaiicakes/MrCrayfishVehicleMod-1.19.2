package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class TileFluidHandlerSynced extends FluidHandlerBlockEntity implements EntityBlock
{
    public TileFluidHandlerSynced(@Nonnull BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int capacity)
    {
        super(tileEntityTypeIn, pos, state);
        this.tank = new FluidTank(capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                TileFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    public TileFluidHandlerSynced(@Nonnull BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int capacity, Predicate<FluidStack> validator)
    {
        super(tileEntityTypeIn, pos, state);
        this.tank = new FluidTank(capacity, validator)
        {
            @Override
            protected void onContentsChanged()
            {
                TileFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    /**
     * Any class extending <code>TileFluidHandlerSynced</code> MUST override this method such that its body contains
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
    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state);

    public void syncFluidToClient()
    {
        if(this.level != null && !this.level.isClientSide)
        {
            CompoundTag compound = new CompoundTag();
            super.saveAdditional(compound);
            TileEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    @SuppressWarnings("unused") //FIXME: unused
    public void syncFluidToPlayer(ServerPlayer player)
    {
        if(this.level != null && !this.level.isClientSide)
        {
            CompoundTag compound = new CompoundTag();
            super.saveAdditional(compound);
            TileEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag()
    {
        this.saveAdditional(new CompoundTag());
        return this.saveWithoutMetadata();
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

    public FluidTank getFluidTank()
    {
        return this.tank;
    }
}