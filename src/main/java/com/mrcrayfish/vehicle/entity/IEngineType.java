package com.mrcrayfish.vehicle.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Author: MrCrayfish
 */
public interface IEngineType
{
    ResourceLocation getId();

    int hashCode();

    default TranslatableContents getEngineName()
    {
        return Component.translatable(this.getId().getNamespace() + ".engine_type." + this.getId().getPath() + ".name");
    }
}
