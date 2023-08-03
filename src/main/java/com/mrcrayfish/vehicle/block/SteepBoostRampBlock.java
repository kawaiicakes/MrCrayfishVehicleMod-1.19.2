package com.mrcrayfish.vehicle.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.entity.block.BoostTileEntity;
import com.mrcrayfish.vehicle.util.Bounds;
import com.mrcrayfish.vehicle.util.StateHelper;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public class SteepBoostRampBlock extends RotatedObjectBlock
{
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    private static final AABB COLLISION_BASE = new AABB(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);
    private static final AABB[] COLLISION_ONE = new Bounds(1, 1, 0, 16, 2, 16).getRotatedBounds();
    private static final AABB[] COLLISION_TWO = new Bounds(2, 2, 0, 16, 3, 16).getRotatedBounds();
    private static final AABB[] COLLISION_THREE = new Bounds(3, 3, 0, 16, 4, 16).getRotatedBounds();
    private static final AABB[] COLLISION_FOUR = new Bounds(4, 4, 0, 16, 5, 16).getRotatedBounds();
    private static final AABB[] COLLISION_FIVE = new Bounds(5, 5, 0, 16, 6, 16).getRotatedBounds();
    private static final AABB[] COLLISION_SIX = new Bounds(6, 6, 0, 16, 7, 16).getRotatedBounds();
    private static final AABB[] COLLISION_SEVEN = new Bounds(7, 7, 0, 16, 8, 16).getRotatedBounds();
    private static final AABB[] COLLISION_EIGHT = new Bounds(8, 9, 0, 16, 9, 16).getRotatedBounds();
    private static final AABB[] COLLISION_NINE = new Bounds(9, 10, 0, 16, 10, 16).getRotatedBounds();
    private static final AABB[] COLLISION_TEN = new Bounds(10, 11, 0, 16, 11, 16).getRotatedBounds();
    private static final AABB[] COLLISION_ELEVEN = new Bounds(11, 12, 0, 16, 12, 16).getRotatedBounds();
    private static final AABB[] COLLISION_TWELVE = new Bounds(12, 13, 0, 16, 13, 16).getRotatedBounds();
    private static final AABB[] COLLISION_THIRTEEN = new Bounds(13, 14, 0, 16, 14, 16).getRotatedBounds();
    private static final AABB[] COLLISION_FOURTEEN = new Bounds(14, 15, 0, 16, 15, 16).getRotatedBounds();
    private static final AABB[] COLLISION_FIFTEEN = new Bounds(15, 15, 0, 16, 16, 16).getRotatedBounds();

    public SteepBoostRampBlock()
    {
        super(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F));
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        if(entity instanceof PoweredVehicleEntity && entity.getControllingPassenger() != null)
        {
            Direction facing = state.getValue(DIRECTION);
            if(facing == entity.getDirection())
            {
                float speedMultiplier = 0.0F;
                BlockEntity tileEntity = world.getBlockEntity(pos);
                if(tileEntity instanceof BoostTileEntity)
                {
                    speedMultiplier = ((BoostTileEntity) tileEntity).getSpeedMultiplier();
                }

                PoweredVehicleEntity poweredVehicle = (PoweredVehicleEntity) entity;
                if(!poweredVehicle.isBoosting())
                {
                    world.playSound(null, pos, ModSounds.BLOCK_BOOST_PAD_BOOST.get(), SoundSource.BLOCKS, 2.0F, 0.5F);
                }
                poweredVehicle.setBoosting(true);
                poweredVehicle.setLaunching(3);
                //poweredVehicle.currentSpeed = poweredVehicle.getActualMaxSpeed();
                poweredVehicle.setSpeedMultiplier(speedMultiplier);
                //Vec3 motion = poweredVehicle.getDeltaMovement(); more commented out code lol
                //poweredVehicle.setDeltaMovement(new Vec3(motion.x, poweredVehicle.currentSpeed / 20F + 0.1, motion.z));
            }
        }
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos)
    {
        return this.getRampState(state, world, pos, state.getValue(DIRECTION));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.getRampState(this.defaultBlockState(), context.getLevel(), context.getClickedPos(), context.getHorizontalDirection());
    }

    private BlockState getRampState(BlockState state, LevelReader world, BlockPos pos, Direction facing)
    {
        state = state.setValue(LEFT, false);
        state = state.setValue(RIGHT, false);
        if(StateHelper.getBlock(world, pos, facing, StateHelper.RelativeDirection.LEFT) == this)
        {
            if(StateHelper.getRotation(world, pos, facing, StateHelper.RelativeDirection.LEFT) == StateHelper.RelativeDirection.DOWN)
            {
                state = state.setValue(RIGHT, true);
            }
        }
        if(StateHelper.getBlock(world, pos, facing, StateHelper.RelativeDirection.RIGHT) == this)
        {
            if(StateHelper.getRotation(world, pos, facing, StateHelper.RelativeDirection.RIGHT) == StateHelper.RelativeDirection.DOWN)
            {
                state = state.setValue(LEFT, true);
            }
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT);
        builder.add(RIGHT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BoostTileEntity(1.0F);
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
