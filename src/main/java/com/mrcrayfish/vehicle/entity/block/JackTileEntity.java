package com.mrcrayfish.vehicle.entity.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.block.JackBlock;
import com.mrcrayfish.vehicle.entity.EntityJack;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class JackTileEntity extends TileEntitySynced implements BlockEntityRenderer<JackTileEntity>
{
    public static final int MAX_LIFT_PROGRESS = 20;

    private EntityJack jack = null;

    private boolean activated = false;
    public int prevLiftProgress;
    public int liftProgress;

    public JackTileEntity()
    {
        super(ModTileEntities.JACK.get(), null, null);
    }
    public JackTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.JACK.get(), pos, state);
    }

    public void setVehicle(VehicleEntity vehicle)
    {
        this.jack = new EntityJack(ModEntities.JACK.get(), this.level, this.worldPosition, 11 * 0.0625, vehicle.getYRot());
        vehicle.startRiding(this.jack, true);
        this.jack.rideTick();
        assert this.level != null;
        this.level.addFreshEntity(this.jack);
    }

    @Nullable
    public EntityJack getJack()
    {
        return this.jack;
    }

    @Override
    @ParametersAreNonnullByDefault
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModTileEntities.JACK.get() && !level.isClientSide() ? JackTileEntity::tick : null;
    }

    //FIXME: assumption this method is server ticking only
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T tInstance)
    {
        if (tInstance instanceof JackTileEntity instance){
            if (!instance.activated && instance.liftProgress == 0 && instance.prevLiftProgress == 1) {
                assert instance.level != null;
                instance.level.setBlock(instance.worldPosition, instance.getBlockState().setValue(JackBlock.ENABLED, false), Block.UPDATE_NEIGHBORS);
            }

            instance.prevLiftProgress = instance.liftProgress;

            if (instance.jack == null) {
                assert instance.level != null;
                List<EntityJack> jacks = instance.level.getEntitiesOfClass(EntityJack.class, new AABB(instance.worldPosition));
                if (jacks.size() > 0) {
                    instance.jack = jacks.get(0);
                }
            }

            if (instance.jack != null && (instance.jack.getPassengers().isEmpty() || !instance.jack.isAlive())) {
                instance.jack = null;
            }

            if (instance.jack != null) {
                if (instance.jack.getPassengers().size() > 0) {
                    if (!instance.activated) {
                        assert instance.level != null;
                        instance.level.playSound(null, instance.worldPosition, ModSounds.BLOCK_JACK_HEAD_UP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        instance.activated = true;
                        instance.level.setBlock(instance.worldPosition, instance.getBlockState().setValue(JackBlock.ENABLED, true), Block.UPDATE_NEIGHBORS);
                    }
                } else if (instance.activated) {
                    assert instance.level != null;
                    instance.level.playSound(null, instance.worldPosition, ModSounds.BLOCK_JACK_HEAD_DOWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    instance.activated = false;
                }
            } else if (instance.activated) {
                assert instance.level != null;
                instance.level.playSound(null, instance.worldPosition, ModSounds.BLOCK_JACK_HEAD_DOWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                instance.activated = false;
            }

            if (instance.activated) {
                if (instance.liftProgress < MAX_LIFT_PROGRESS) {
                    instance.liftProgress++;
                    instance.moveCollidedEntities();
                }
            } else if (instance.liftProgress > 0) {
                instance.liftProgress--;
                instance.moveCollidedEntities();
            }
        }
    }

    private void moveCollidedEntities()
    {
        assert this.level != null;
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if(state.getBlock() instanceof JackBlock)
        {
            AABB boundingBox = state.getShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
            List<Entity> list = this.level.getEntities(this.jack, boundingBox);
            if(!list.isEmpty())
            {
                for(Entity entity : list)
                {
                    if(entity.getPistonPushReaction() != PushReaction.IGNORE)
                    {
                        AABB entityBoundingBox = entity.getBoundingBox();
                        double posY = boundingBox.maxY - entityBoundingBox.minY;
                        entity.move(MoverType.PISTON, new Vec3(0.0, posY, 0.0));
                    }
                }
            }
        }
    }

    public float getProgress()
    {
        return (float) this.liftProgress / (float) MAX_LIFT_PROGRESS;
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void render(@NotNull JackTileEntity entity, float numberLol, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int p_112311_, int p_112312_) {
        //FIXME: render impl.
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getViewDistance()
    {
        return 65536;
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
        return new JackTileEntity(pos, state);
    }
}
