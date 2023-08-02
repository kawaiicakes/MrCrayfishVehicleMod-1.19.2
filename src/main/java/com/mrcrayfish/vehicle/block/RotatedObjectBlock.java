package com.mrcrayfish.vehicle.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings({"NullableProblems", "deprecation"})
public abstract class RotatedObjectBlock extends ObjectBlock implements EntityBlock
{
    public static final DirectionProperty DIRECTION = HorizontalDirectionalBlock.FACING;

    public RotatedObjectBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(DIRECTION, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DIRECTION);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(DIRECTION, rotation.rotate(state.getValue(DIRECTION)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(DIRECTION)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return null;
    }

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
