package com.mrcrayfish.vehicle.entity.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.obfuscate.common.data.SyncedPlayerData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.HermiteInterpolator;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class GasPumpTileEntity extends TileEntitySynced implements BlockEntityRenderer<GasPumpTileEntity>
{
    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return null;
    }
    private int fuelingEntityId;
    private Player fuelingEntity;

    private HermiteInterpolator cachedSpline;
    private boolean recentlyUsed;
    public GasPumpTileEntity()
    {
        super(ModTileEntities.GAS_PUMP.get(), null, null);
    }

    public GasPumpTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.GAS_PUMP.get(), pos, state);
    }

    public HermiteInterpolator getCachedSpline()
    {
        return cachedSpline;
    }

    public void setCachedSpline(HermiteInterpolator cachedSpline)
    {
        this.cachedSpline = cachedSpline;
    }

    public boolean isRecentlyUsed()
    {
        return recentlyUsed;
    }

    public void setRecentlyUsed(boolean recentlyUsed)
    {
        this.recentlyUsed = recentlyUsed;
    }

    @Nullable
    public FluidTank getTank()
    {
        assert this.level != null;
        BlockEntity tileEntity = this.level.getBlockEntity(this.worldPosition.below());
        if(tileEntity instanceof GasPumpTankTileEntity)
        {
            return ((GasPumpTankTileEntity) tileEntity).getFluidTank();
        }
        return null;
    }

    public Player getFuelingEntity()
    {
        return this.fuelingEntity;
    }

    public void setFuelingEntity(@Nullable Player entity)
    {
        assert this.level != null;
        if(!this.level.isClientSide)
        {
            if(this.fuelingEntity != null)
            {
                SyncedPlayerData.instance().set(this.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
            }
            this.fuelingEntity = null;
            this.fuelingEntityId = -1;
            if(entity != null)
            {
                this.fuelingEntityId = entity.getId();
                SyncedPlayerData.instance().set(entity, ModDataKeys.GAS_PUMP, Optional.of(this.getBlockPos()));
            }
            this.syncToClient();
        }
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModTileEntities.GAS_PUMP.get() ? GasPumpTileEntity::tick : null;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T tInstance)
        {
            if (tInstance instanceof GasPumpTileEntity instance){
                if (instance.fuelingEntityId != -1) {
                    if (instance.fuelingEntity == null) {
                        assert instance.level != null;
                        Entity entity = instance.level.getEntity(instance.fuelingEntityId);
                        if (entity instanceof Player) {
                            instance.fuelingEntity = (Player) entity;
                        } else if (!instance.level.isClientSide) {
                            instance.fuelingEntityId = -1;
                            instance.syncFuelingEntity();
                        }
                    }
                } else {
                    assert instance.level != null;
                    if (instance.level.isClientSide && instance.fuelingEntity != null) {
                        instance.fuelingEntity = null;
                    }
                }

                assert instance.level != null;
                if (!instance.level.isClientSide && instance.fuelingEntity != null) {
                    if (Math.sqrt(instance.fuelingEntity.distanceToSqr(instance.worldPosition.getX() + 0.5, instance.worldPosition.getY() + 0.5, instance.worldPosition.getZ() + 0.5)) > Config.SERVER.maxHoseDistance.get() || !instance.fuelingEntity.isAlive()) {
                        if (instance.fuelingEntity.isAlive()) {
                            instance.level.playSound(null, instance.fuelingEntity.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                        SyncedPlayerData.instance().set(instance.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
                        instance.fuelingEntityId = -1;
                        instance.fuelingEntity = null;
                        instance.syncFuelingEntity();
                    }
                }
            }
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("FuelingEntity", Tag.TAG_INT))
        {
            this.fuelingEntityId = compound.getInt("FuelingEntity");
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound)
    {
        compound.putInt("FuelingEntity", this.fuelingEntityId);
        this.saveWithFullMetadata();
    }

    public CompoundTag saveHelper(CompoundTag compound)
    {
        saveAdditional(compound);
        return this.saveWithFullMetadata();
    }

    private void syncFuelingEntity()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt("FuelingEntity", this.fuelingEntityId);
        TileEntityUtil.sendUpdatePacket(this, this.saveHelper(compound));
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void render(@NotNull GasPumpTileEntity p_112307_, float p_112308_, @NotNull PoseStack p_112309_, @NotNull MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        //FIXME: render impl.
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getViewDistance()
    {
        return 65536;
    }
}
