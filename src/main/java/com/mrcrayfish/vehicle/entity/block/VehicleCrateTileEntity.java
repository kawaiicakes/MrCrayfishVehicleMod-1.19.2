package com.mrcrayfish.vehicle.entity.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.entity.EngineTier;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class VehicleCrateTileEntity extends TileEntitySynced implements BlockEntityRenderer<VehicleCrateTileEntity>
{
    private static final Random RAND = new Random();

    private ResourceLocation entityId;
    private int color = VehicleEntity.DYE_TO_COLOR[0];
    private ItemStack engineStack = ItemStack.EMPTY;
    private ItemStack wheelStack = ItemStack.EMPTY;
    private boolean opened = false;
    private int timer;
    private UUID opener;

    @OnlyIn(Dist.CLIENT)
    private Entity entity;

    public VehicleCrateTileEntity()
    {
        super(ModTileEntities.VEHICLE_CRATE.get(), null, null);
    }
    public VehicleCrateTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.VEHICLE_CRATE.get(), pos, state);
    }

    public void setEntityId(ResourceLocation entityId)
    {
        this.entityId = entityId;
        this.setChanged();
    }

    public ResourceLocation getEntityId()
    {
        return entityId;
    }

    public void open(UUID opener)
    {
        if(this.entityId != null)
        {
            this.opened = true;
            this.opener = opener;
            this.syncToClient();
        }
    }

    public boolean isOpened()
    {
        return opened;
    }

    public int getTimer()
    {
        return timer;
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT) //FIXME: validate naive suppression of unchecked cast
    public <E extends Entity> E getEntity()
    {
        return (E) entity;
    }

    @ParametersAreNonnullByDefault
    @Nullable //FIXME: y'know, I should probably make sure this is actually how you're supposed to make shit tick... (no pun intended)
    //the calls creating several new instances of tickers for the same thing makes me a bit queasy... is there a way to have one 'main' ticker?
    //Would it be worth it?
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModTileEntities.VEHICLE_CRATE.get() ? VehicleCrateTileEntity::tick : null;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T tInstance)
    {
        if (tInstance instanceof VehicleCrateTileEntity instance){
            if (instance.opened) {
                instance.timer += 5;
                if (instance.level != null && instance.level.isClientSide()) {
                    if (instance.entityId != null && instance.entity == null) {
                        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(instance.entityId);
                        if (entityType != null) {
                            instance.entity = entityType.create(instance.level);
                            if (instance.entity != null) {
                                VehicleHelper.playSound(SoundEvents.ITEM_BREAK, instance.worldPosition, 1.0F, 0.5F);
                                List<SynchedEntityData.DataItem<?>> entryList = instance.entity.getEntityData().getAll();
                                if (entryList != null) {
                                    entryList.forEach(dataEntry -> instance.entity.onSyncedDataUpdated(dataEntry.getAccessor()));
                                }
                                if (instance.entity instanceof VehicleEntity vehicleEntity) {
                                    vehicleEntity.setColor(instance.color);
                                    if (!instance.wheelStack.isEmpty()) {
                                        vehicleEntity.setWheelStack(instance.wheelStack);
                                    }
                                }
                                if (instance.entity instanceof PoweredVehicleEntity entityPoweredVehicle) {
                                    if (instance.engineStack != null) {
                                        entityPoweredVehicle.setEngineStack(instance.engineStack);
                                    }
                                }
                            } else {
                                instance.entityId = null;
                            }
                        } else {
                            instance.entityId = null;
                        }
                    }
                    if (instance.timer == 90 || instance.timer == 110 || instance.timer == 130 || instance.timer == 150) {
                        float pitch = (float) (0.9F + 0.2F * RAND.nextDouble());
                        VehicleHelper.playSound(ModSounds.BLOCK_VEHICLE_CRATE_PANEL_LAND.get(), instance.worldPosition, 1.0F, pitch);
                    }
                    if (instance.timer == 150) {
                        VehicleHelper.playSound(SoundEvents.GENERIC_EXPLODE, instance.worldPosition, 1.0F, 1.0F);
                        instance.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, false, instance.worldPosition.getX() + 0.5, instance.worldPosition.getY() + 0.5, instance.worldPosition.getZ() + 0.5, 0, 0, 0);
                    }
                }
                if (!instance.level.isClientSide && instance.timer > 250) {
                    BlockState state2 = instance.level.getBlockState(instance.worldPosition);
                    Direction facing = state2.getValue(VehicleCrateBlock.DIRECTION);
                    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(instance.entityId);
                    if (entityType != null) {
                        Entity entity = entityType.create(instance.level);
                        if (entity != null) {
                            if (entity instanceof VehicleEntity vehicleEntity) {
                                vehicleEntity.setColor(instance.color);
                                if (!instance.wheelStack.isEmpty()) {
                                    vehicleEntity.setWheelStack(instance.wheelStack);
                                }
                            }
                            if (instance.opener != null && entity instanceof PoweredVehicleEntity poweredVehicle) {
                                poweredVehicle.setOwner(instance.opener);
                                if (!instance.engineStack.isEmpty()) {
                                    poweredVehicle.setEngineStack(instance.engineStack);
                                }
                            }
                            entity.absMoveTo(instance.worldPosition.getX() + 0.5, instance.worldPosition.getY(), instance.worldPosition.getZ() + 0.5, facing.get2DDataValue() * 90F + 180F, 0F);
                            entity.setYHeadRot(facing.get2DDataValue() * 90F + 180F);
                            instance.level.addFreshEntity(entity);
                        }
                        instance.level.setBlockAndUpdate(instance.worldPosition, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("Vehicle", Tag.TAG_STRING))
        {
            this.entityId = new ResourceLocation(compound.getString("Vehicle"));
        }
        if(compound.contains("Color", Tag.TAG_INT))
        {
            this.color = compound.getInt("Color");
        }
        if(compound.contains("EngineStack", Tag.TAG_COMPOUND))
        {
            this.engineStack = ItemStack.of(compound.getCompound("EngineStack"));
        }
        else if(compound.getBoolean("Creative"))
        {
            VehicleProperties properties = VehicleProperties.get(this.entityId);
            EngineItem engineItem = VehicleRegistry.getEngineItem(properties.getExtended(PoweredProperties.class).getEngineType(), EngineTier.IRON);
            this.engineStack = engineItem != null ? new ItemStack(engineItem) : ItemStack.EMPTY;
        }
        if(compound.contains("WheelStack", Tag.TAG_COMPOUND))
        {
            this.wheelStack = ItemStack.of(compound.getCompound("WheelStack"));
        }
        else
        {
            this.wheelStack = new ItemStack(ModItems.STANDARD_WHEEL.get());
        }
        if(compound.contains("Opener", Tag.TAG_STRING))
        {
            this.opener = compound.getUUID("Opener");
        }
        if(compound.contains("Opened", Tag.TAG_BYTE))
        {
            this.opened = compound.getBoolean("Opened");
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound)
    {
        if(this.entityId != null)
        {
            compound.putString("Vehicle", this.entityId.toString());
        }
        if(this.opener != null)
        {
            compound.putUUID("Opener", this.opener);
        }
        if(!this.engineStack.isEmpty())
        {
            CommonUtils.writeItemStackToTag(compound, "EngineStack", this.engineStack);
        }
        if(!this.wheelStack.isEmpty())
        {
            CommonUtils.writeItemStackToTag(compound, "WheelStack", this.wheelStack);
        }
        compound.putInt("Color", this.color);
        compound.putBoolean("Opened", this.opened);
        super.saveAdditional(compound);
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void render(@NotNull VehicleCrateTileEntity entity, float numberLOL, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int number2, int number3) {
        //FIXME: proper impl
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getViewDistance()
    {
        return 65536;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new VehicleCrateTileEntity(pos, state);
    }
}
