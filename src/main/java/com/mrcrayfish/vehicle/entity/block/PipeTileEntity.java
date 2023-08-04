package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class PipeTileEntity extends TileEntitySynced
{
    protected Set<BlockPos> pumps = new HashSet<>();
    protected boolean[] disabledConnections = new boolean[Direction.values().length];

    public PipeTileEntity()
    {
        super(ModTileEntities.FLUID_PIPE.get(), null, null);
    }

    public PipeTileEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state)
    {
        super(tileEntityType, pos, state);
    }

    @Override
    @ParametersAreNonnullByDefault
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    public void addPump(BlockPos pos)
    {
        this.pumps.add(pos);
    }

    public void removePump(BlockPos pos)
    {
        this.pumps.remove(pos);
    }

    public Set<BlockPos> getPumps()
    {
        return this.pumps;
    }

    public boolean[] getDisabledConnections()
    {
        return this.disabledConnections;
    }

    public void setConnectionState(Direction direction, boolean state)
    {
        this.disabledConnections[direction.get3DDataValue()] = state;
        this.syncDisabledConnections();
    }

    public boolean isConnectionDisabled(Direction direction)
    {
        return this.disabledConnections[direction.get3DDataValue()];
    }

    public void syncDisabledConnections()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundTag compound = new CompoundTag();
            this.writeConnections(compound);
            TileEntityUtil.sendUpdatePacket(this, this.saveHelper(compound));
        }
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("DisabledConnections", Tag.TAG_BYTE_ARRAY))
        {
            byte[] connections = compound.getByteArray("DisabledConnections");
            for(int i = 0; i < connections.length; i++)
            {
                this.disabledConnections[i] = connections[i] == (byte) 1;
            }
        }
    }
    public CompoundTag saveHelper(CompoundTag compound)
    {
        this.saveAdditional(compound);
        return compound;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound)
    {
        this.writeConnections(compound);
        super.saveAdditional(this.saveHelper(compound));
    }

    private void writeConnections(CompoundTag compound)
    {
        byte[] connections = new byte[this.disabledConnections.length];
        for(int i = 0; i < connections.length; i++)
        {
            connections[i] = (byte) (this.disabledConnections[i] ? 1 : 0);
        }
        compound.putByteArray("DisabledConnections", connections);
    }

    /**
     * Any class extending <code>TileEntitySynced</code>> MUST override this method such that its body contains
     * the constructor of the subclass. The constructor of the subclass MUST call <code>super</code> and pass
     * arguments appropriately.
     * <br>
     * Similarly, any class extending subclasses of <code>TileEntitySynced</code> must copy the described behaviour.
     * Any method making a call to the constructor of <code>TileEntitySynced</code> or anything extending it -
     * specifically in other classes - should instead consider doing so inside an override of this method.
     *
     * @param pos   a <code>BlockPos</code> automatically passed by the game.
     * @param state a <code>BlockState</code> automatically passed by the game.
     * @return a new <code>BlockEntity</code> instance representing this object.
     */
    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PipeTileEntity(ModTileEntities.FLUID_PIPE.get(), pos, state);
    }
}
