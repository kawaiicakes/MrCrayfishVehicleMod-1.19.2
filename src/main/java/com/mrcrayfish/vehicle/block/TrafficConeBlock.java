package com.mrcrayfish.vehicle.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public class TrafficConeBlock extends ObjectBlock
{
    private static final VoxelShape COLLISION_SHAPE = Block.box(2, 0, 2, 14, 18, 14);
    private static final VoxelShape SELECTION_SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public TrafficConeBlock()
    {
        super(BlockBehaviour.Properties.of(Material.CLAY, MaterialColor.TERRACOTTA_ORANGE).strength(0.5F));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return SELECTION_SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return COLLISION_SHAPE;
    }
}
