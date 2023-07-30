package com.mrcrayfish.vehicle.client.model;

import com.mrcrayfish.vehicle.client.render.complex.ComplexModel;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public interface IComplexModel
{
    ResourceLocation getModelLocation();

    IForgeBakedModel getBaseModel();

    @Nullable
    ComplexModel getComplexModel();
}
