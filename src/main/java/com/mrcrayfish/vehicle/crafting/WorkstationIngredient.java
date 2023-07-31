package com.mrcrayfish.vehicle.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class CompoundIngredient extends Ingredient
{
    private final ItemValue itemList;
    private final int count;

    protected CompoundIngredient(Stream<? extends ItemValue> itemList, int count)
    {
        super(itemList);
        this.itemList = null;
        this.count = count;
    }

    private CompoundIngredient(ItemValue itemList, int count)
    {
        super(Stream.of(itemList));
        this.itemList = itemList;
        this.count = count;
    }

    public int getCount()
    {
        return this.count;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    public static CompoundIngredient fromJson(JsonObject object)
    {
        Value value = valueFromJson(object);
        int count = GsonHelper.getAsInt(object, "count", 1);
        return new CompoundIngredient(Stream.of(value), count);
    }

    @Override
    public JsonElement toJson()
    {
        JsonObject object = this.itemList.serialize();
        object.addProperty("count", this.count);
        return object;
    }

    public static CompoundIngredient of(ItemLike provider, int count)
    {
        return new CompoundIngredient(new Ingredient(new ItemStack(provider)), count);
    }

    public static CompoundIngredient of(ItemStack stack, int count)
    {
        return new CompoundIngredient(new Ingredient.SingleItemList(stack), count);
    }

    public static CompoundIngredient of(TagKey<Item> tag, int count)
    {
        return new CompoundIngredient(new Ingredient.TagList(tag), count);
    }

    public static CompoundIngredient of(ResourceLocation id, int count)
    {
        return new CompoundIngredient(new MissingSingleItemList(id), count);
    }

    public static class Serializer implements IIngredientSerializer<CompoundIngredient>
    {
        public static final CompoundIngredient.Serializer INSTANCE = new CompoundIngredient.Serializer();

        @Override
        public CompoundIngredient parse(FriendlyByteBuf buffer)
        {
            int itemCount = buffer.readVarInt();
            int count = buffer.readVarInt();
            Stream<Ingredient.SingleItemList> values = Stream.generate(() ->
                    new SingleItemList(buffer.readItem())).limit(itemCount);
            return new CompoundIngredient(values, count);
        }

        @Override
        public CompoundIngredient parse(JsonObject object)
        {
            return CompoundIngredient.fromJson(object);
        }

        @Override
        public void write(FriendlyByteBuf buffer, CompoundIngredient ingredient)
        {
            buffer.writeVarInt(ingredient.getItems().length);
            buffer.writeVarInt(ingredient.count);
            for(ItemStack stack : ingredient.getItems())
            {
                buffer.writeItem(stack);
            }
        }
    }

    //

    /**
     * Allows ability to define an ingredient from another mod without depending. Serializes the data
     * to be read by the regular {@link SingleItemList}. Only use this for generating data.
     */
    public static class MissingSingleItemList extends ItemValue {
        private final ResourceLocation id;

        public MissingSingleItemList(ResourceLocation id)
        {
            this.id = id;
        }

        @Override
        public Collection<ItemStack> getItems()
        {
            return Collections.emptyList();
        }

        @Override
        public JsonObject serialize()
        {
            JsonObject object = new JsonObject();
            object.addProperty("item", this.id.toString());
            return object;
        }
    }
}
