package com.mrcrayfish.vehicle.entity;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public interface IEngineType
{
    ResourceLocation getId();

    int hashCode();

    default TranslatableContents getEngineName()
    {
        return new TranslatableContents(this.getId().getNamespace() + ".engine_type." + this.getId().getPath() + ".name");
    }
}
