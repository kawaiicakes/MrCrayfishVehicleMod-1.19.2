package com.mrcrayfish.framework.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.IUnbakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Open Model format from Framework. Allows larger models and removes rotation step restriction.
 *
 * Author: MrCrayfish
 */
public class OpenModel implements BlockModel<OpenModel>
{
    private final BlockModel model;

    public OpenModel(BlockModel model)
    {
        this.model = model;
    }

    @Override
    public BakedModel bake(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation)
    {
        return this.model.bake(bakery, this.model, spriteGetter, modelTransform, modelLocation, true);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
    {
        return this.model.getMaterials(modelGetter, missingTextureErrors);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Loader implements IModelLoader<OpenModel>
    {
        @Override
        public void onResourceManagerReload(ResourceManager manager) {}

        @Override
        public OpenModel read(JsonDeserializationContext context, JsonObject object)
        {
            return new OpenModel(Deserializer.INSTANCE.deserialize(object, BlockModel.class, context));
        }

        @SubscribeEvent
        public static void onModelRegister(ModelEvent event)
        {
            ModelLoaderRegistry.registerLoader(new ResourceLocation("framework", "open_model"), new Loader());
        }
    }

    public static class Deserializer extends BlockModel.Deserializer
    {
        private static final ModelPart.Deserializer BLOCK_PART_DESERIALIZER = new ModelPart.Deserializer();
        private static final Deserializer INSTANCE = new Deserializer();

        /**
         * Reads the bl
         */
        @Override
        protected List<ModelPart> getElements(JsonDeserializationContext context, JsonObject object)
        {
            try
            {
                List<ModelPart> list = new ArrayList<>();
                for(JsonElement element : Objects.requireNonNull(GsonHelper.getAsJsonArray(object, "components", new JsonArray())))
                {
                    list.add(this.readBlockElement(element, context));
                }
                return list;
            }
            catch(Exception e)
            {
                throw new JsonParseException(e);
            }
        }

        /**
         * Reads a block element without restrictions on the size and rotation angle.
         */
        private ModelPart readBlockElement(JsonElement element, JsonDeserializationContext context)
        {
            JsonObject object = element.getAsJsonObject();

            // Get copy of custom size and angle properties
            Vector3f from = ExtraJSONUtils.getAsVector3f(object, "from");
            Vector3f to = ExtraJSONUtils.getAsVector3f(object, "to");
            JsonObject rotation = GsonHelper.getAsJsonObject(object, "rotation", new JsonObject());
            float angle = GsonHelper.getAsFloat(rotation, "angle", 0F);

            // Make valid for vanilla block element deserializer
            JsonArray zero = new JsonArray();
            zero.add(0F);
            zero.add(0F);
            zero.add(0F);
            object.add("from", zero);
            object.add("to", zero);
            rotation.addProperty("angle", 0F);

            // Read vanilla element and construct new element with custom properties
            ModelPart e = BLOCK_PART_DESERIALIZER.deserialize(element, ModelPart.class, context);
            PartPose r = new PartPose(e.getInitialPose(), e.rotation.axis, angle, e.rotation.rescale);
            return new ModelPart(from, to, e.faces, r, e.shade);
        }
    }
}
