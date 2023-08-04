package com.mrcrayfish.vehicle.block;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.entity.block.VehicleCrateTileEntity;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.BlockMarker;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes; 
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault //FIXME: aggressive annotation
public class VehicleCrateBlock extends RotatedObjectBlock
{
    public static final List<ResourceLocation> REGISTERED_CRATES = new ArrayList<>();
    private static final VoxelShape PANEL = box(0, 0, 0, 16, 2, 16);

    public VehicleCrateBlock()
    {
        super(BlockBehaviour.Properties.of(Material.METAL, DyeColor.LIGHT_GRAY).dynamicShape().noOcclusion().strength(1.5F, 5.0F));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
    {
        REGISTERED_CRATES.forEach(resourceLocation ->
        {
            CompoundTag blockEntityTag = new CompoundTag();
            blockEntityTag.putString("Vehicle", resourceLocation.toString());
            blockEntityTag.putBoolean("Creative", true);
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("BlockEntityTag", blockEntityTag);
            ItemStack stack = new ItemStack(ModBlocks.VEHICLE_CRATE.get());
            stack.setTag(itemTag);
            items.add(stack);
        });
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof VehicleCrateTileEntity && ((VehicleCrateTileEntity)te).isOpened())
            return PANEL;
        return Shapes.block();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos)
    {
        return this.isBelowBlockTopSolid(reader, pos) && this.canOpen(reader, pos);
    }

    private boolean canOpen(LevelReader reader, BlockPos pos)
    {
        for(Direction side : Direction.Plane.HORIZONTAL)
        {
            BlockPos adjacentPos = pos.relative(side);
            BlockState state = reader.getBlockState(adjacentPos);
            if(state.isAir())
                continue;
            if(!state.getMaterial().isReplaceable() || this.isBelowBlockTopSolid(reader, adjacentPos))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isBelowBlockTopSolid(LevelReader reader, BlockPos pos)
    {
        return reader.getBlockState(pos.below()).isFaceSturdy(reader, pos.below(), Direction.UP);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level world, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult result)
    {
        if(result.getDirection() == Direction.UP && playerEntity.getItemInHand(hand).getItem() == ModItems.WRENCH.get())
        {
            this.openCrate(world, pos, state, playerEntity);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack)
    {
        if(livingEntity instanceof Player && ((Player) livingEntity).isCreative())
        {
            this.openCrate(world, pos, state, livingEntity);
        }
    }

    private void openCrate(Level world, BlockPos pos, BlockState state, LivingEntity placer)
    {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof VehicleCrateTileEntity && this.canOpen(world, pos))
        {
            if(world.isClientSide)
            {
                this.spawnCrateOpeningParticles((ClientLevel) world, pos, state);
            }
            else
            {
                ((VehicleCrateTileEntity) tileEntity).open(placer.getUUID());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnCrateOpeningParticles(ClientLevel world, BlockPos pos, BlockState state)
    {
        double y = 0.875;
        double x, z;
        BlockMarker.Provider factory = new BlockMarker.Provider();
        for(int j = 0; j < 4; ++j)
        {
            for(int l = 0; l < 4; ++l)
            {
                x = (j + 0.5D) / 4.0D;
                z = (l + 0.5D) / 4.0D;
                Minecraft.getInstance().particleEngine.add(
                        Objects.requireNonNull(factory.createParticle(
                                new BlockParticleOption(ParticleTypes.BLOCK, state),
                                world,
                                pos.getX() + x,
                                pos.getY() + y,
                                pos.getZ() + z,
                                x - 0.5D,
                                y - 0.5D,
                                z - 0.5D)
                        ));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new VehicleCrateTileEntity();
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter reader, @NotNull List<Component> list, @NotNull TooltipFlag advanced)
    {
        Component vehicleName = EntityType.PIG.getDescription();
        CompoundTag tagCompound = stack.getTag();
        if(tagCompound != null)
        {
            if(tagCompound.contains("BlockEntityTag", Tag.TAG_COMPOUND))
            {
                CompoundTag blockEntityTag = tagCompound.getCompound("BlockEntityTag");
                String entityType = blockEntityTag.getString("Vehicle");
                if(!Strings.isNullOrEmpty(entityType))
                {
                    vehicleName = EntityType.byString(entityType).orElse(EntityType.PIG).getDescription();
                }
            }
        }
        if(Screen.hasShiftDown())
        {
            list.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info", vehicleName), 150));
        }
        else
        {
            list.add(vehicleName.copy().withStyle(ChatFormatting.BLUE));
            list.add(Component.translatable("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static ItemStack create(ResourceLocation entityId, int color, ItemStack engine, ItemStack wheel)
    {
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.putString("Vehicle", entityId.toString());
        blockEntityTag.putInt("Color", color);
        blockEntityTag.put("EngineStack", engine.save(new CompoundTag()));
        blockEntityTag.put("WheelStack", wheel.save(new CompoundTag()));
        CompoundTag itemTag = new CompoundTag();
        itemTag.put("BlockEntityTag", blockEntityTag);
        ItemStack stack = new ItemStack(ModBlocks.VEHICLE_CRATE.get());
        stack.setTag(itemTag);
        return stack;
    }

    public static synchronized void registerVehicle(ResourceLocation id)
    {
        if(!REGISTERED_CRATES.contains(id))
        {
            REGISTERED_CRATES.add(id);
            Collections.sort(REGISTERED_CRATES);
        }
    }

    @SuppressWarnings("NullableProblems")
    static class BlockStateBase extends BlockBehaviour.BlockStateBase {
        protected BlockStateBase(Block p_60608_, ImmutableMap<Property<?>, Comparable<?>> p_60609_, MapCodec<BlockState> p_60610_) {
            super(p_60608_, p_60609_, p_60610_);
        }

        @Override
        protected BlockState asState() {
            return null;
        }

        @Override
        public boolean hasBlockEntity() {return true;}
    }
}
