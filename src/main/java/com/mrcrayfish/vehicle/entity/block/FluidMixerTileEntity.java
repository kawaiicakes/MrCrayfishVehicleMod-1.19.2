package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.block.FluidMixerBlock;
import com.mrcrayfish.vehicle.block.RotatedObjectBlock;
import com.mrcrayfish.vehicle.crafting.FluidEntry;
import com.mrcrayfish.vehicle.crafting.FluidMixerRecipe;
import com.mrcrayfish.vehicle.crafting.ModRecipeTypes;
import com.mrcrayfish.vehicle.init.ModFluids;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.inventory.container.FluidMixerContainer;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation") //lots of deprecated holder calls. Oh well.
public class FluidMixerTileEntity extends TileEntitySynced implements Container, EntityBlock, MenuProvider, IFluidTankWriter
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

    private final FluidTank tankBlaze = new FluidTank(Config.SERVER.mixerInputCapacity.get(), this::isValidFluid);
    private final FluidTank tankEnderSap = new FluidTank(Config.SERVER.mixerInputCapacity.get(), this::isValidFluid);
    private final FluidTank tankFuelium = new FluidTank(Config.SERVER.mixerOutputCapacity.get(), stack -> stack.getFluid() == ModFluids.FUELIUM.get());

    private static final int SLOT_FUEL = 0;
    public static final int SLOT_INGREDIENT = 1;

    private FluidMixerRecipe currentRecipe = null;
    private int remainingFuel;
    private int fuelMaxProgress;
    private int extractionProgress;
    private boolean mixing = false;

    private MutableComponent customName;

    protected final ContainerData fluidMixerData = new ContainerData()
    {
        public int get(int index)
        {
            return switch (index) {
                case 0 -> extractionProgress;
                case 1 -> remainingFuel;
                case 2 -> fuelMaxProgress;
                case 3 -> tankBlaze.getFluidAmount();
                case 4 -> tankEnderSap.getFluidAmount();
                case 5 -> tankFuelium.getFluidAmount();
                case 6 -> tankBlaze.getFluid().getFluid().builtInRegistryHolder().key().location().hashCode();
                case 7 -> tankEnderSap.getFluid().getFluid().builtInRegistryHolder().key().location().hashCode();
                case 8 -> tankFuelium.getFluid().getFluid().builtInRegistryHolder().key().location().hashCode();
                default -> 0;
            };
        }

        public void set(int index, int value)
        {
            switch (index) {
                case 0 -> extractionProgress = value;
                case 1 -> remainingFuel = value;
                case 2 -> fuelMaxProgress = value;
                case 3 -> {
                    if (!tankBlaze.isEmpty() || tankBlaze.getFluid().getRawFluid() != Fluids.EMPTY) {
                        tankBlaze.getFluid().setAmount(value);
                    }
                }
                case 4 -> {
                    if (!tankEnderSap.isEmpty() || tankEnderSap.getFluid().getRawFluid() != Fluids.EMPTY) {
                        tankEnderSap.getFluid().setAmount(value);
                    }
                }
                case 5 -> {
                    if (!tankFuelium.isEmpty() || tankFuelium.getFluid().getRawFluid() != Fluids.EMPTY) {
                        tankFuelium.getFluid().setAmount(value);
                    }
                }
                case 6 -> updateFluid(tankBlaze, value);
                case 7 -> updateFluid(tankEnderSap, value);
                case 8 -> updateFluid(tankFuelium, value);
            }
        }

        public int getCount()
        {
            return 9;
        }
    };

    public FluidMixerTileEntity()
    {
        super(ModTileEntities.FLUID_MIXER.get(), null, null);
    }

    public FluidMixerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.FLUID_MIXER.get(), pos, state);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FluidMixerTileEntity(pos, state);
    }

    @Override
    public int getContainerSize()
    {
        return 7;
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : this.inventory)
        {
            if(!stack.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int index)
    {
        return this.inventory.get(index);
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count)
    {
        ItemStack stack = ContainerHelper.removeItem(this.inventory, index, count);
        if(!stack.isEmpty())
        {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index)
    {
        return ContainerHelper.takeItem(this.inventory, index);
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack)
    {
        this.inventory.set(index, stack);
        if(stack.getCount() > this.getMaxStackSize())
        {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        assert this.level != null;
        return this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack)
    {
        if(index == 0)
        {
            return ForgeHooks.getBurnTime(stack, ModRecipeTypes.FLUID_MIXER.get()) > 0;
        }
        else if(index == 1)
        {
            return this.isValidIngredient(stack);
        }
        return false;
    }

    @Override
    public void clearContent()
    {
        this.inventory.clear();
    }

    @Nullable
    @ParametersAreNonnullByDefault
    @Override //FIXME: does this actually properly tick the instances of this entity?
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModTileEntities.FLUID_MIXER.get() ? FluidMixerTileEntity::tickServer : null;
    }

    private static <T extends BlockEntity> void tickServer(Level level, BlockPos pos, BlockState state, T tInstance)
    {
        if(level != null && !level.isClientSide() && tInstance instanceof FluidMixerTileEntity instance)
        {
            ItemStack ingredient = instance.getItem(SLOT_INGREDIENT);
            ItemStack fuel = instance.getItem(SLOT_FUEL);

            if(instance.currentRecipe == null && !ingredient.isEmpty())
            {
                instance.currentRecipe = instance.getRecipe().orElse(null);
            }
            else if(!instance.canMix(instance.currentRecipe))
            {
                instance.currentRecipe = null;
                instance.extractionProgress = 0;
            }

            if(instance.canMix(instance.currentRecipe))
            {
                instance.updateFuel(fuel);

                if(instance.remainingFuel > 0)
                {
                    instance.setMixing(true);

                    if(instance.extractionProgress++ == Config.SERVER.mixerMixTime.get())
                    {
                        FluidMixerRecipe recipe = instance.currentRecipe;
                        instance.tankFuelium.fill(recipe.getResult().createStack(), IFluidHandler.FluidAction.EXECUTE);
                        instance.tankBlaze.drain(recipe.getFluidAmount(instance.tankBlaze.getFluid().getFluid()), IFluidHandler.FluidAction.EXECUTE);
                        instance.tankEnderSap.drain(recipe.getFluidAmount(instance.tankEnderSap.getFluid().getFluid()), IFluidHandler.FluidAction.EXECUTE);
                        instance.shrinkItem(SLOT_INGREDIENT);
                        instance.extractionProgress = 0;
                        instance.currentRecipe = null;
                    }
                }
                else
                {
                    instance.extractionProgress = 0;
                    instance.setMixing(false);
                }
            }
            else
            {
                instance.extractionProgress = 0;
                instance.setMixing(false);
            }

            if(instance.remainingFuel > 0)
            {
                instance.remainingFuel--;
                instance.updateFuel(fuel);

                // Updates the enabled state of the fluid extractor
                if(instance.remainingFuel == 0)
                {
                    instance.setMixing(false);
                }
            }
        }
    }

    @SuppressWarnings("unused") //FIXME unused
    private void updateFuel(ItemStack fuel)
    {
        if(!fuel.isEmpty() && ForgeHooks.getBurnTime(fuel, ModRecipeTypes.FLUID_MIXER.get()) > 0 && this.remainingFuel == 0 && this.canMix(this.currentRecipe))
        {
            this.fuelMaxProgress = ForgeHooks.getBurnTime(fuel, ModRecipeTypes.FLUID_MIXER.get());
            this.remainingFuel = this.fuelMaxProgress;
            this.shrinkItem(SLOT_FUEL);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canMix()
    {
        ItemStack ingredient = this.getItem(SLOT_INGREDIENT);
        if(!ingredient.isEmpty() && !this.tankBlaze.getFluid().isEmpty() && !this.tankEnderSap.getFluid().isEmpty())
        {
            if(this.currentRecipe == null)
            {
                this.currentRecipe = this.getRecipe().orElse(null);
            }
        }
        else
        {
            this.currentRecipe = null;
        }
        return this.canMix(this.currentRecipe) && this.remainingFuel >= 0; //FIXME potential null invocation error?
    }

    private void shrinkItem(int index)
    {
        ItemStack stack = this.getItem(index);
        stack.shrink(1);
        if(stack.isEmpty())
        {
            this.setItem(index, ItemStack.EMPTY);
        }
    }

    private boolean canMix(@Nullable FluidMixerRecipe recipe)
    {
        if(recipe == null)
            return false;
        ItemStack ingredient = this.getItem(SLOT_INGREDIENT);
        if(ingredient.getItem() != recipe.getIngredient().getItem())
            return false;
        if(this.tankBlaze.getFluid().isEmpty())
            return false;
        if(this.tankEnderSap.getFluid().isEmpty())
            return false;
        if(this.tankBlaze.getFluidAmount() < recipe.getFluidAmount(this.tankBlaze.getFluid().getFluid()))
            return false;
        if(this.tankEnderSap.getFluidAmount() < recipe.getFluidAmount(this.tankEnderSap.getFluid().getFluid()))
            return false;
        if(this.tankFuelium.getFluidAmount() >= this.tankFuelium.getCapacity())
            return false;
        return this.tankFuelium.getFluidAmount() + recipe.getResult().amount() <= this.tankFuelium.getCapacity();
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("Items", Tag.TAG_LIST))
        {
            this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(compound, this.inventory);
        }
        if(compound.contains("CustomName", Tag.TAG_STRING))
        {
            this.customName = Component.literal(compound.getString("CustomName"));
        }
        if(compound.contains("TankBlaze", Tag.TAG_COMPOUND))
        {
            CompoundTag tagCompound = compound.getCompound("TankBlaze");
            //FluidUtils.fixEmptyTag(tagCompound); //TODO might not need
            this.tankBlaze.readFromNBT(tagCompound);
        }
        if(compound.contains("TankEnderSap", Tag.TAG_COMPOUND))
        {
            CompoundTag tagCompound = compound.getCompound("TankEnderSap");
            //FluidUtils.fixEmptyTag(tagCompound);
            this.tankEnderSap.readFromNBT(tagCompound);
        }
        if(compound.contains("TankFuelium", Tag.TAG_COMPOUND))
        {
            CompoundTag tagCompound = compound.getCompound("TankFuelium");
            //FluidUtils.fixEmptyTag(tagCompound);
            this.tankFuelium.readFromNBT(tagCompound);
        }
        if(compound.contains("RemainingFuel", Tag.TAG_INT))
        {
            this.remainingFuel = compound.getInt("RemainingFuel");
        }
        if(compound.contains("FuelMaxProgress", Tag.TAG_INT))
        {
            this.fuelMaxProgress = compound.getInt("FuelMaxProgress");
        }
        if(compound.contains("ExtractionProgress", Tag.TAG_INT))
        {
            this.extractionProgress = compound.getInt("ExtractionProgress");
        }
    }
    public CompoundTag saveHelper(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);

        ContainerHelper.saveAllItems(compound, this.inventory);

        if(this.hasCustomName())
        {
            compound.putString("CustomName", String.valueOf(this.customName));
        }

        this.writeTanks(compound);

        compound.putInt("RemainingFuel", this.remainingFuel);
        compound.putInt("FuelMaxProgress", this.fuelMaxProgress);
        compound.putInt("ExtractionProgress", this.extractionProgress);
        return compound;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag()
    {
        CompoundTag tag = this.saveHelper(this.saveWithFullMetadata());
        this.writeTanks(tag);
        return tag;
    }

    @Override
    public void writeTanks(CompoundTag compound)
    {
        CompoundTag tagTankBlaze = new CompoundTag();
        this.tankBlaze.writeToNBT(tagTankBlaze);
        compound.put("TankBlaze", tagTankBlaze);

        CompoundTag tagTankEnderSap = new CompoundTag();
        this.tankEnderSap.writeToNBT(tagTankEnderSap);
        compound.put("TankEnderSap", tagTankEnderSap);

        CompoundTag tagTankFuelium = new CompoundTag();
        this.tankFuelium.writeToNBT(tagTankFuelium);
        compound.put("TankFuelium", tagTankFuelium);
    }

    @Override
    public boolean areTanksEmpty()
    {
        return this.tankBlaze.isEmpty() && this.tankEnderSap.isEmpty() && this.tankFuelium.isEmpty();
    }

    public @NotNull MutableComponent getName()
    {
        return this.hasCustomName() ? this.customName : Component.translatable("container.fluid_mixer");
    }

    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.getString().isEmpty();
    }

    @Override
    public @NotNull Component getDisplayName()
    {
        return this.hasCustomName()
                ? MutableComponent.create(new LiteralContents(this.getName().toString()))
                : Component.translatable(this.getName().toString());
    }

    @Nullable
    public FluidStack getBlazeFluidStack()
    {
        return this.tankBlaze.getFluid();
    }

    @Nullable
    public FluidStack getEnderSapFluidStack()
    {
        return this.tankEnderSap.getFluid();
    }

    @Nullable
    public FluidStack getFueliumFluidStack()
    {
        return this.tankFuelium.getFluid();
    }

    public int getExtractionProgress()
    {
        return this.fluidMixerData.get(0);
    }

    public int getRemainingFuel()
    {
        return this.fluidMixerData.get(1);
    }

    public int getFuelMaxProgress()
    {
        return this.fluidMixerData.get(2);
    }

    public int getBlazeLevel()
    {
        return this.fluidMixerData.get(3);
    }

    public int getEnderSapLevel()
    {
        return this.fluidMixerData.get(4);
    }

    public int getFueliumLevel()
    {
        return this.fluidMixerData.get(5);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity)
    {
        return new FluidMixerContainer(windowId, playerInventory, this);
    }

    public ContainerData getFluidMixerData()
    {
        return fluidMixerData;
    }

    public void updateFluid(FluidTank tank, int fluidHash)
    {
        Optional<Fluid> optional = ForgeRegistries.FLUIDS.getValues().stream().filter(fluid -> fluid.builtInRegistryHolder().key().location().hashCode() == fluidHash).findFirst();
        optional.ifPresent(fluid -> tank.setFluid(new FluidStack(fluid, tank.getFluidAmount())));
    }

    public Optional<FluidMixerRecipe> getRecipe()
    {
        assert this.level != null;
        return this.level.getRecipeManager().getRecipeFor(ModRecipeTypes.FLUID_MIXER.get(), this, this.level);
    }

    private boolean isValidIngredient(ItemStack ingredient)
    {
        assert this.level != null;
        List<FluidMixerRecipe> recipes = this.level.getRecipeManager().getRecipes().stream().filter(recipe -> recipe.getType() == ModRecipeTypes.FLUID_MIXER.get()).map(recipe -> (FluidMixerRecipe) recipe).toList();
        return recipes.stream().anyMatch(recipe -> InventoryUtil.areItemStacksEqualIgnoreCount(ingredient, recipe.getIngredient()));
    }

    private boolean isValidFluid(FluidStack stack)
    {
        assert this.level != null;
        List<FluidMixerRecipe> recipes = this.level.getRecipeManager().getRecipes().stream().filter(recipe -> recipe.getType() == ModRecipeTypes.FLUID_MIXER.get()).map(recipe -> (FluidMixerRecipe) recipe).toList();
        return recipes.stream().anyMatch(recipe ->
        {
            for(FluidEntry entry : recipe.getInputs())
            {
                if(entry.fluid() == stack.getFluid())
                {
                    return true;
                }
            }
            return false;
        });
    }

    public FluidTank getEnderSapTank()
    {
        return tankEnderSap;
    }

    public FluidTank getBlazeTank()
    {
        return tankBlaze;
    }

    public FluidTank getFueliumTank()
    {
        return tankFuelium;
    }

    private final net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(this::createUnSidedHandler);

    @Nonnull
    protected net.minecraftforge.items.IItemHandler createUnSidedHandler()
    {
        return new net.minecraftforge.items.wrapper.InvWrapper(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing)
    {
        if(cap == ForgeCapabilities.FLUID_HANDLER)
        {
            assert this.level != null;
            BlockState state = this.level.getBlockState(this.worldPosition);
            if(state.getProperties().contains(RotatedObjectBlock.DIRECTION))
            {
                Direction direction = state.getValue(RotatedObjectBlock.DIRECTION);
                if(facing == direction.getCounterClockWise())
                {
                    return LazyOptional.of(() -> this.tankBlaze).cast();
                }
                if(facing == direction)
                {
                    return LazyOptional.of(() -> this.tankEnderSap).cast();
                }
                if(facing == direction.getClockWise())
                {
                    return LazyOptional.of(() -> this.tankFuelium).cast();
                }
            }
            return LazyOptional.empty();
        }
        else if(!this.remove && cap == ForgeCapabilities.ITEM_HANDLER)
        {
            return this.itemHandler.cast();
        }
        return super.getCapability(cap, facing);
    }

    @SuppressWarnings("unused") //FIXME: unused
    private void setMixing(boolean state)
    {
        if(this.mixing != state)
        {
            this.mixing = state;
            assert this.level != null;
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FluidMixerBlock.ENABLED, state), Block.UPDATE_NEIGHBORS);
        }
    }
}









