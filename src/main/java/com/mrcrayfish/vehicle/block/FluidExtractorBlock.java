package com.mrcrayfish.vehicle.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mrcrayfish.vehicle.tileentity.FluidExtractorTileEntity;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: MrCrayfish
 */
@ParametersAreNonnullByDefault
public class FluidExtractorBlock extends RotatedObjectBlock
{
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public FluidExtractorBlock()
    {
        super(BlockBehaviour.Properties.of(Material.HEAVY_METAL).strength(1.0F).noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(ENABLED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult result)
    {
        if(!world.isClientSide)
        {
            ItemStack stack = playerEntity.getItemInHand(hand);
            if(stack.getItem() == Items.BUCKET)
            {
                FluidUtil.interactWithFluidHandler(playerEntity, hand, world, pos, result.getDirection());
                return InteractionResult.SUCCESS;
            }

            BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof FluidExtractorTileEntity)
            {
                TileEntityUtil.sendUpdatePacket(tileEntity, (ServerPlayer) playerEntity);
                NetworkHooks.openScreen((ServerPlayer) playerEntity, (MenuProvider) tileEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(state.getBlock() != newState.getBlock())
        {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if(tileentity instanceof Container)
            {
                Containers.dropContents(worldIn, pos, (Container) tileentity);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FluidExtractorTileEntity();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    static class BlockStateBase extends BlockBehaviour.BlockStateBase { //definitely look into factory methods.
        //this implementation is more than scuffed; I'm not even sure it works.
        protected BlockStateBase(Block p_60608_, ImmutableMap<Property<?>, Comparable<?>> p_60609_, MapCodec<BlockState> p_60610_) {
            super(p_60608_, p_60609_, p_60610_);
        }

        @Override
        public boolean hasBlockEntity() {return true;}

        @Override
        protected BlockState asState() {
            return null;
            /*
            So, BlockState actually extends BlockStateBase, and it overrides #asState...
            All it does is return this...
            It's also non-nullable. fuck.
             */
        }
    }
}