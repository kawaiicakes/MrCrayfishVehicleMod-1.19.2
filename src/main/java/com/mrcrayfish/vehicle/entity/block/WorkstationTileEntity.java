package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.inventory.IStorageBlock;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: MrCrayfish
 */
public class WorkstationTileEntity extends TileEntitySynced implements IForgeBlockEntity, IStorageBlock
{
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    public WorkstationTileEntity()
    {
        super(ModTileEntities.WORKSTATION.get(), null, null);
    }
    public WorkstationTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.WORKSTATION.get(), pos, state);
    }

    @Override
    public NonNullList<ItemStack> getInventory()
    {
        return this.inventory;
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        ContainerHelper.loadAllItems(compound, this.inventory);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound)
    {
        ContainerHelper.saveAllItems(compound, this.inventory);
        super.saveAdditional(compound);
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack)
    {
        return index != 0 || (stack.getItem() instanceof DyeItem && this.inventory.get(index).getCount() < 1);
    }

    @Override
    public @NotNull Component getDisplayName()
    {
        return Component.translatable("container.vehicle.workstation");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity)
    {
        return new WorkstationContainer(windowId, playerInventory, this);
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
        return new WorkstationTileEntity(pos, state);
    }

    @Override
    @ParametersAreNonnullByDefault
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
