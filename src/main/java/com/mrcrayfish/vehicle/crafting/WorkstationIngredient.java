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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class WorkstationIngredient extends Ingredient
    //TODO reassess need for this class
{
    private final ItemValue itemList;
    private final int count;

    private WorkstationIngredient(ItemValue itemList, int count)
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
    public @NotNull IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    public static WorkstationIngredient fromJson(JsonObject object)
    {
        Value value = valueFromJson(object);
        int count = GsonHelper.getAsInt(object, "count", 1);
        return new WorkstationIngredient((ItemValue) Stream.of(value), count);
    }

    @Override
    public @NotNull JsonElement toJson()
    {
        JsonObject object = this.itemList.serialize();
        object.addProperty("count", this.count);
        return object;
    }

    public static WorkstationIngredient of(ItemLike provider, int count)
    { //TODO make sure this ghetto impl actually works lmfao
        return new WorkstationIngredient(new Ingredient.ItemValue(
                Arrays.stream(Ingredient.of(provider).getItems()).findAny().orElseThrow()), count);
    }

    public static WorkstationIngredient of(ItemStack stack, int count)
    {
        return new WorkstationIngredient(new Ingredient.ItemValue(stack), count);
    }

    public static WorkstationIngredient of(TagKey<Item> tag, int count)
    { //TODO make sure this ghetto impl actually works lmfao
        return new WorkstationIngredient(new ItemValue(new TagValue(tag).getItems().stream().findAny().orElseThrow()), count);
    }

    public static WorkstationIngredient of(ResourceLocation id, int count)
    {
        return new WorkstationIngredient(new MissingSingleItemList(id), count);
    }

    public static class Serializer implements IIngredientSerializer<WorkstationIngredient>
    {
        public static final WorkstationIngredient.Serializer INSTANCE = new WorkstationIngredient.Serializer();

        @Override
        public @NotNull WorkstationIngredient parse(FriendlyByteBuf buffer)
        {
            int itemCount = buffer.readVarInt();
            int count = buffer.readVarInt();
            Stream<Ingredient.ItemValue> values = Stream.generate(() ->
                    new ItemValue(buffer.readItem())).limit(itemCount);
            return new WorkstationIngredient((ItemValue) values, count);
        }

        @Override
        public @NotNull WorkstationIngredient parse(@NotNull JsonObject object)
        {
            return WorkstationIngredient.fromJson(object);
        }

        @Override
        public void write(FriendlyByteBuf buffer, WorkstationIngredient ingredient)
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
     * to be read by the regular {@link Ingredient.ItemValue}. Only use this for generating data.
     */
    public static class MissingSingleItemList extends ItemValue {
        private final ResourceLocation id;

        public MissingSingleItemList(ResourceLocation id)
        {
            super(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(id)).getDefaultInstance());
            this.id = id;
        }

        @Override
        public @NotNull Collection<ItemStack> getItems()
        {
            return Collections.emptyList();
        }

        @Override
        public @NotNull JsonObject serialize()
        {
            JsonObject object = new JsonObject();
            object.addProperty("item", this.id.toString());
            return object;
        }
    }
}
