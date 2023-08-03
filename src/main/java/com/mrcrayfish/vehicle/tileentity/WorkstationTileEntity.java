package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.inventory.IStorageBlock;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class WorkstationTileEntity extends TileEntitySynced implements IStorageBlock
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    public WorkstationTileEntity()
    {
        super(ModTileEntities.WORKSTATION.get());
    }

    @Override
    public NonNullList<ItemStack> getInventory()
    {
        return this.inventory;
    }

    @Override
    public void load(BlockState state, CompoundTag compound)
    {
        super.load(state, compound);
        ItemStackHelper.loadAllItems(compound, this.inventory);
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        ItemStackHelper.saveAllItems(compound, this.inventory);
        return super.saveAdditional(compound);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack)
    {
        return index != 0 || (stack.getItem() instanceof DyeItem && this.inventory.get(index).getCount() < 1);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("container.vehicle.workstation");
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, Inventory playerInventory, Player playerEntity)
    {
        return new WorkstationContainer(windowId, playerInventory, this);
    }
}
