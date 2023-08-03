package com.mrcrayfish.vehicle.entity.block;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.init.ModTileEntities;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankTileEntity extends TileFluidHandlerSynced
{
    public GasPumpTankTileEntity()
    {
        super(ModTileEntities.GAS_PUMP_TANK.get(), Config.SERVER.gasPumpCapacity.get());
    }
}