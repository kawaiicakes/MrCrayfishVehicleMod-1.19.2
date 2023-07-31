package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class TileFluidHandlerSynced extends TileFluidHandler
{
    public TileFluidHandlerSynced(@Nonnull TileEntityType<?> tileEntityTypeIn, int capacity)
    {
        super(tileEntityTypeIn);
        this.tank = new FluidTank(capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                TileFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    public TileFluidHandlerSynced(@Nonnull TileEntityType<?> tileEntityTypeIn, int capacity, Predicate<FluidStack> validator)
    {
        super(tileEntityTypeIn);
        this.tank = new FluidTank(capacity, validator)
        {
            @Override
            protected void onContentsChanged()
            {
                TileFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    public void syncFluidToClient()
    {
        if(this.level != null && !this.level.isClientSide)
        {
            CompoundTag compound = new CompoundTag();
            super.save(compound);
            TileEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    public void syncFluidToPlayer(ServerPlayer player)
    {
        if(this.level != null && !this.level.isClientSide)
        {
            CompoundTag compound = new CompoundTag();
            super.save(compound);
            TileEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.save(new CompoundTag());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        this.load(null, pkt.getTag());
    }

    public FluidTank getFluidTank()
    {
        return this.tank;
    }
}