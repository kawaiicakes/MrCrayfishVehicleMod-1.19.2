package com.mrcrayfish.vehicle.entity.properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.client.CameraProperties;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticProperties;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.datagen.VehiclePropertiesProvider;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.Wheel;
import com.mrcrayfish.vehicle.network.HandshakeMessages;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class VehicleProperties
{
    private static final double WHEEL_DIAMETER = 8.0;
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(VehicleProperties.class, new Serializer()).create();
    private static final Map<ResourceLocation, VehicleProperties> DEFAULT_VEHICLE_PROPERTIES = new HashMap<>();  // Properties loaded from mod jar
    private static final Map<ResourceLocation, VehicleProperties> NETWORK_VEHICLE_PROPERTIES = new HashMap<>(); // Properties from the server (client only)
    private static final Map<ResourceLocation, ExtendedProperties> GLOBAL_EXTENDED_PROPERTIES = new HashMap<>();
    private static final List<Supplier<VehiclePropertiesProvider>> DYNAMIC_SUPPLIERS = new ArrayList<>();

    public static final float DEFAULT_MAX_HEALTH = 100F;
    public static final float DEFAULT_AXLE_OFFSET = 0F;
    public static final Vec3 DEFAULT_HELD_OFFSET = Vec3.ZERO;
    public static final boolean DEFAULT_CAN_TOW_TRAILERS = false;
    public static final Vec3 DEFAULT_TOW_BAR_OFFSET = Vec3.ZERO;
    public static final Vec3 DEFAULT_TRAILER_OFFSET = Vec3.ZERO;
    public static final boolean DEFAULT_CAN_CHANGE_WHEELS = false;
    public static final boolean DEFAULT_IMMUNE_TO_FALL_DAMAGE = false;
    public static final boolean DEFAULT_CAN_PLAYER_CARRY = true;
    public static final boolean DEFAULT_CAN_FIT_IN_TRAILER = false;
    public static final Transform DEFAULT_BODY_TRANSFORM = Transform.DEFAULT;
    public static final Transform DEFAULT_DISPLAY_TRANSFORM = Transform.DEFAULT;
    public static final boolean DEFAULT_CAN_BE_PAINTED = false;

    private final float maxHealth;
    private final float axleOffset;
    private final float wheelOffset;
    private final Vec3 heldOffset;
    private final boolean canTowTrailers;
    private final Vec3 towBarOffset;
    private final Vec3 trailerOffset;
    private final boolean canChangeWheels;
    private final boolean immuneToFallDamage;
    private final boolean canPlayerCarry;
    private final boolean canFitInTrailer;
    private final List<Wheel> wheels;
    private final Transform bodyTransform;
    private final Transform displayTransform;
    private final List<Seat> seats;
    private final boolean canBePainted;
    private final CameraProperties camera;
    private final ImmutableMap<ResourceLocation, ExtendedProperties> extended;
    private final ImmutableMap<ResourceLocation, CosmeticProperties> cosmetics;

    private VehicleProperties(float maxHealth, float axleOffset, float wheelOffset, Vec3 heldOffset, boolean canTowTrailers, Vec3 towBarOffset, Vec3 trailerOffset, boolean canChangeWheels, boolean immuneToFallDamage, boolean canPlayerCarry, boolean canFitInTrailer, List<Wheel> wheels, Transform bodyTransform, Transform displayTransform, List<Seat> seats, boolean canBePainted, CameraProperties camera, Map<ResourceLocation, ExtendedProperties> extended, Map<ResourceLocation, CosmeticProperties> cosmetics)
    {
        this.maxHealth = maxHealth;
        this.axleOffset = axleOffset;
        this.wheelOffset = wheelOffset;
        this.heldOffset = heldOffset;
        this.canTowTrailers = canTowTrailers;
        this.towBarOffset = towBarOffset;
        this.trailerOffset = trailerOffset;
        this.canChangeWheels = canChangeWheels;
        this.immuneToFallDamage = immuneToFallDamage;
        this.canPlayerCarry = canPlayerCarry;
        this.canFitInTrailer = canFitInTrailer;
        this.wheels = wheels;
        this.bodyTransform = bodyTransform;
        this.displayTransform = displayTransform;
        this.seats = seats;
        this.canBePainted = canBePainted;
        this.camera = camera;
        this.extended = ImmutableMap.copyOf(extended);
        this.cosmetics = ImmutableMap.copyOf(cosmetics);
    }

    public float getMaxHealth()
    {
        return this.maxHealth;
    }

    public float getAxleOffset()
    {
        return this.axleOffset;
    }

    public float getWheelOffset()
    {
        return this.wheelOffset;
    }

    public Vec3 getHeldOffset()
    {
        return this.heldOffset;
    }

    public boolean canTowTrailers()
    {
        return this.canTowTrailers;
    }

    public Vec3 getTowBarOffset()
    {
        return this.towBarOffset;
    }

    public Vec3 getTrailerOffset()
    {
        return this.trailerOffset;
    }

    public List<Wheel> getWheels()
    {
        return this.wheels;
    }

    @Nullable
    public Wheel getFirstFrontWheel()
    {
        return this.wheels.stream().filter(wheel -> wheel.getPosition() == Wheel.Position.FRONT).findFirst().orElse(null);
    }

    @Nullable
    public Wheel getFirstRearWheel()
    {
        return this.wheels.stream().filter(wheel -> wheel.getPosition() == Wheel.Position.REAR).findFirst().orElse(null);
    }

    public Transform getBodyTransform()
    {
        return this.bodyTransform;
    }

    public Transform getDisplayTransform()
    {
        return this.displayTransform;
    }

    public List<Seat> getSeats()
    {
        return ImmutableList.copyOf(this.seats);
    }

    public boolean canChangeWheels()
    {
        return this.canChangeWheels && this.wheels.size() > 0;
    }

    public boolean immuneToFallDamage()
    {
        return this.immuneToFallDamage;
    }

    public boolean canPlayerCarry()
    {
        return this.canPlayerCarry;
    }

    public boolean canFitInTrailer()
    {
        return this.canFitInTrailer;
    }

    public boolean canBePainted()
    {
        return this.canBePainted;
    }

    public CameraProperties getCamera()
    {
        return this.camera;
    }

    @SuppressWarnings("unchecked")
    public <T extends ExtendedProperties> T getExtended(Class<T> properties)
    {
        ResourceLocation id = ExtendedProperties.getId(properties);
        T t = (T) this.extended.get(id);
        if(t != null)
        {
            return t;
        }
        GLOBAL_EXTENDED_PROPERTIES.computeIfAbsent(id, id2 -> ExtendedProperties.create(id2, new JsonObject()));
        return (T) GLOBAL_EXTENDED_PROPERTIES.get(id);
    }

    public ImmutableMap<ResourceLocation, CosmeticProperties> getCosmetics()
    {
        return this.cosmetics;
    }

    public static void loadDefaultProperties()
    {
        for(EntityType<? extends VehicleEntity> entityType : VehicleRegistry.getRegisteredVehicleTypes())
        {
            DEFAULT_VEHICLE_PROPERTIES.computeIfAbsent(entityType.builtInRegistryHolder().key().location(), VehicleProperties::loadDefaultProperties);
        }
    }

    private static VehicleProperties loadDefaultProperties(ResourceLocation id)
    {
        String resource = String.format("/data/%s/vehicles/properties/%s.json", id.getNamespace(), id.getPath());
        try(InputStream is = VehicleProperties.class.getResourceAsStream(resource))
        {
            VehicleProperties properties = loadPropertiesFromStream(is);
            loadDefaultCosmetics(id, properties);
            return properties;
        }
        catch(JsonParseException | IOException e)
        {
            VehicleMod.LOGGER.error("Couldn't load vehicles properties: " + resource, e);
        }
        catch(NullPointerException e)
        {
            VehicleMod.LOGGER.error("Missing vehicle properties file: " + resource, e);
        }
        return null;
    }

    private static void loadDefaultCosmetics(ResourceLocation id, VehicleProperties properties)
    {
        String resource = String.format("/data/%s/vehicles/cosmetics/%s.json", id.getNamespace(), id.getPath());
        try(InputStream is = VehicleProperties.class.getResourceAsStream(resource))
        {
            Objects.requireNonNull(is, "");
            Map<ResourceLocation, List<Pair<ResourceLocation, List<ResourceLocation>>>> modelMap = new HashMap<>();
            CosmeticProperties.deserializeModels(is, modelMap);
            modelMap.forEach((cosmeticId, models) -> {
                CosmeticProperties cosmetic = properties.getCosmetics().get(cosmeticId);
                if(cosmetic == null) throw new JsonParseException("Invalid cosmetic '" + cosmeticId + "' doesn't exist for vehicle '" + id + "'");
                cosmetic.setModelLocations(models.stream().map(Pair::getLeft).collect(Collectors.toList()));
                cosmetic.setDisabledCosmetics(models.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
            });
        }
        catch(JsonParseException | IOException e)
        {
            VehicleMod.LOGGER.error("Couldn't load cosmetic definitions: " + resource, e);
        }
        catch(NullPointerException e)
        {
            VehicleMod.LOGGER.error("Missing cosmetic definitions file: " + resource, e);
        }
    }

    private static VehicleProperties loadPropertiesFromStream(InputStream is)
    {
        return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), VehicleProperties.class);
    }

    public static VehicleProperties get(EntityType<?> entityType)
    {
        return get(entityType.builtInRegistryHolder().key().location());
    }

    public static VehicleProperties get(ResourceLocation id)
    {
        VehicleProperties properties = null;
        if(EffectiveSide.get() == LogicalSide.SERVER)
        {
            Manager manager = Manager.get();
            if(manager != null && manager.getVehicleProperties() != null)
            {
                properties = manager.getVehicleProperties().get(id);
            }
        }
        else if(!NETWORK_VEHICLE_PROPERTIES.isEmpty())
        {
            properties = NETWORK_VEHICLE_PROPERTIES.get(id);
        }
        if(properties == null)
        {
            properties = DEFAULT_VEHICLE_PROPERTIES.get(id);
            if(properties == null)
            {
                throw new IllegalArgumentException("No vehicle properties registered for " + id);
            }
        }
        return properties;
    }

    public static boolean updateNetworkVehicleProperties(HandshakeMessages.S2CVehicleProperties message)
    {
        Map<ResourceLocation, VehicleProperties> propertiesMap = message.getPropertiesMap();

        // We should receive the same amount of properties
        if(DEFAULT_VEHICLE_PROPERTIES.size() != propertiesMap.size())
            return false;

        // Validate that all the keys exist in the default properties, and we don't have anything missing
        for(ResourceLocation key : propertiesMap.keySet())
        {
            if(!DEFAULT_VEHICLE_PROPERTIES.containsKey(key))
            {
                VehicleMod.LOGGER.error("Received properties for vehicle that doesn't exist: {}", key);
                return false;
            }
        }

        // Finally update the network properties
        NETWORK_VEHICLE_PROPERTIES.clear();
        NETWORK_VEHICLE_PROPERTIES.putAll(message.getPropertiesMap());
        return true;
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
    {
        NETWORK_VEHICLE_PROPERTIES.clear();
    }

    /**
     * Registers vehicle properties providers to be used as a way to refresh properties while in
     * game. This helps speed up designing vehicle properties since they are data driven.
     *
     * @param supplier an instance of a vehicle provider
     */
    public static void registerDynamicProvider(Supplier<VehiclePropertiesProvider> supplier)
    {
        if(FMLEnvironment.production)
            return;
        DYNAMIC_SUPPLIERS.add(supplier);
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event)
    {
        if(FMLEnvironment.production)
            return;

        if(event.getKey() != GLFW.GLFW_KEY_F6 || (event.getModifiers() % GLFW.GLFW_KEY_LEFT_CONTROL) <= 0)
            return;

        DYNAMIC_SUPPLIERS.forEach(supplier ->
        {
            VehiclePropertiesProvider provider = supplier.get();
            provider.setScaleWheels(true);
            provider.registerProperties();
            DEFAULT_VEHICLE_PROPERTIES.putAll(provider.getVehiclePropertiesMap());
            NETWORK_VEHICLE_PROPERTIES.putAll(provider.getVehiclePropertiesMap());
        });
        //TODO: localize
        Minecraft.getInstance().gui.setOverlayMessage(MutableComponent.create(new LiteralContents("Refreshed vehicle properties!")), false);
    }

    public static class Serializer implements JsonDeserializer<VehicleProperties>, JsonSerializer<VehicleProperties>
    {
        @Override
        public JsonElement serialize(VehicleProperties properties, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject object = new JsonObject();
            ExtraJSONUtils.write(object, "canBePainted", properties.canBePainted, DEFAULT_CAN_BE_PAINTED);
            ExtraJSONUtils.write(object, "canChangeWheels", properties.canChangeWheels, DEFAULT_CAN_CHANGE_WHEELS);
            ExtraJSONUtils.write(object, "immuneToFallDamage", properties.immuneToFallDamage, DEFAULT_IMMUNE_TO_FALL_DAMAGE);
            ExtraJSONUtils.write(object, "canPlayerCarry", properties.canPlayerCarry, DEFAULT_CAN_PLAYER_CARRY);
            ExtraJSONUtils.write(object, "canFitInTrailer", properties.canFitInTrailer, DEFAULT_CAN_FIT_IN_TRAILER);
            ExtraJSONUtils.write(object, "offsetToGround", properties.axleOffset, DEFAULT_AXLE_OFFSET);
            ExtraJSONUtils.write(object, "heldOffset", properties.heldOffset, DEFAULT_HELD_OFFSET);
            ExtraJSONUtils.write(object, "trailerOffset", properties.trailerOffset, DEFAULT_TRAILER_OFFSET);
            ExtraJSONUtils.write(object, "canTowTrailers", properties.canTowTrailers, DEFAULT_CAN_TOW_TRAILERS);
            ExtraJSONUtils.write(object, "towBarOffset", properties.towBarOffset, DEFAULT_TOW_BAR_OFFSET);
            ExtraJSONUtils.write(object, "displayTransform", properties.displayTransform, DEFAULT_DISPLAY_TRANSFORM);
            ExtraJSONUtils.write(object, "bodyTransform", properties.bodyTransform, DEFAULT_BODY_TRANSFORM);
            this.writeWheels(properties, object);
            this.writeSeats(properties, object);
            this.writeCamera(properties, object);
            this.writeExtended(properties, object);
            this.writeCosmetics(properties, object);
            return object;
        }

        @Override
        public VehicleProperties deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject object = GsonHelper.convertToJsonObject(element, "vehicle property");
            VehicleProperties.Builder builder = VehicleProperties.builder();
            builder.setCanBePainted(GsonHelper.getAsBoolean(object, "canBePainted", DEFAULT_CAN_BE_PAINTED));
            builder.setCanChangeWheels(GsonHelper.getAsBoolean(object, "canChangeWheels", DEFAULT_CAN_CHANGE_WHEELS));
            builder.setImmuneToFallDamage(GsonHelper.getAsBoolean(object, "immuneToFallDamage", DEFAULT_IMMUNE_TO_FALL_DAMAGE));
            builder.setCanPlayerCarry(GsonHelper.getAsBoolean(object, "canPlayerCarry", DEFAULT_CAN_PLAYER_CARRY));
            builder.setCanFitInTrailer(GsonHelper.getAsBoolean(object, "canFitInTrailer", DEFAULT_CAN_FIT_IN_TRAILER));
            builder.setAxleOffset(GsonHelper.getAsFloat(object, "offsetToGround", DEFAULT_AXLE_OFFSET));
            builder.setHeldOffset(ExtraJSONUtils.getAsVector3d(object, "heldOffset", DEFAULT_HELD_OFFSET));
            builder.setTrailerOffset(ExtraJSONUtils.getAsVector3d(object, "trailerOffset", DEFAULT_TRAILER_OFFSET));
            builder.setCanTowTrailers(GsonHelper.getAsBoolean(object, "canTowTrailers", DEFAULT_CAN_TOW_TRAILERS));
            builder.setTowBarOffset(ExtraJSONUtils.getAsVector3d(object, "towBarOffset", DEFAULT_TOW_BAR_OFFSET));
            builder.setDisplayTransform(ExtraJSONUtils.getAsTransform(object, "displayTransform", DEFAULT_DISPLAY_TRANSFORM));
            builder.setBodyTransform(ExtraJSONUtils.getAsTransform(object, "bodyTransform", DEFAULT_BODY_TRANSFORM));
            this.readWheels(builder, object);
            this.readSeats(builder, object);
            this.readCamera(builder, object);
            this.readExtended(builder, object);
            this.readCosmetics(builder, object);
            return builder.build(true);
        }

        private void readWheels(VehicleProperties.Builder builder, JsonObject object)
        {
            if(object.has("wheels"))
            {
                JsonArray wheelArray = GsonHelper.getAsJsonArray(object, "wheels");
                for(JsonElement wheelElement : wheelArray)
                {
                    JsonObject wheelObject = wheelElement.getAsJsonObject();
                    builder.addWheel(Wheel.fromJsonObject(wheelObject));
                }
            }
        }

        private void writeWheels(VehicleProperties properties, JsonObject object)
        {
            if(properties.getWheels().size() > 0)
            {
                JsonArray wheels = new JsonArray();
                for(Wheel wheel : properties.getWheels())
                {
                    wheels.add(wheel.toJsonObject());
                }
                object.add("wheels", wheels);
            }
        }

        private void readSeats(VehicleProperties.Builder builder, JsonObject object)
        {
            if(object.has("seats"))
            {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(object, "seats");
                for(JsonElement element : jsonArray)
                {
                    JsonObject seatObject = element.getAsJsonObject();
                    builder.addSeat(Seat.fromJsonObject(seatObject));
                }
            }
        }

        private void writeSeats(VehicleProperties properties, JsonObject object)
        {
            if(properties.getSeats().size() > 0)
            {
                JsonArray seats = new JsonArray();
                for(Seat seat : properties.getSeats())
                {
                    seats.add(seat.toJsonObject());
                }
                object.add("seats", seats);
            }
        }

        private void readCamera(VehicleProperties.Builder builder, JsonObject object)
        {
            if(object.has("camera"))
            {
                JsonObject cameraObject = GsonHelper.getAsJsonObject(object, "camera", new JsonObject());
                builder.setCamera(CameraProperties.fromJsonObject(cameraObject));
            }
        }

        private void writeCamera(VehicleProperties properties, JsonObject object)
        {
            CameraProperties camera = properties.getCamera();
            if(camera == CameraProperties.DEFAULT_CAMERA)
                return;
            JsonObject cameraObject = camera.toJsonObject();
            if(cameraObject.size() > 0)
            {
                object.add("camera", cameraObject);
            }
        }

        private void readExtended(VehicleProperties.Builder builder, JsonObject object)
        {
            JsonObject extended = GsonHelper.getAsJsonObject(object, "extended", new JsonObject());
            extended.entrySet().stream().filter(entry -> entry.getValue().isJsonObject()).forEach(entry -> {
                ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                JsonObject content = entry.getValue().getAsJsonObject();
                builder.addExtended(ExtendedProperties.create(id, content));
            });
        }

        private void writeExtended(VehicleProperties properties, JsonObject object)
        {
            JsonObject extended = new JsonObject();
            properties.extended.forEach((id, extendedProperties) -> {
                JsonObject content = new JsonObject();
                extendedProperties.serialize(content);
                extended.add(extendedProperties.getId().toString(), content);
            });
            if(extended.size() > 0)
            {
                object.add("extended", extended);
            }
        }

        @SuppressWarnings("ConstantValue")
        private void readCosmetics(VehicleProperties.Builder builder, JsonObject object)
        {
            JsonArray cosmetics = GsonHelper.getAsJsonArray(object, "cosmetics", new JsonArray());
            if(cosmetics != null) //FIXME: always false?
            {
                StreamSupport.stream(cosmetics.spliterator(), false).filter(JsonElement::isJsonObject).forEach(element -> {
                    JsonObject properties = element.getAsJsonObject();
                    builder.addCosmetic(new CosmeticProperties(properties));
                });
            }
        }

        private void writeCosmetics(VehicleProperties properties, JsonObject object)
        {
            JsonArray cosmetics = new JsonArray();
            properties.cosmetics.forEach((id, cosmeticProperties) -> {
                JsonObject content = new JsonObject();
                cosmeticProperties.serialize(content);
                cosmetics.add(content);
            });
            if(cosmetics.size() > 0)
            {
                object.add("cosmetics", cosmetics);
            }
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder
    {
        private float maxHealth = DEFAULT_MAX_HEALTH;
        private float axleOffset = DEFAULT_AXLE_OFFSET;
        private Vec3 heldOffset = DEFAULT_HELD_OFFSET;
        private boolean canTowTrailers = DEFAULT_CAN_TOW_TRAILERS;
        private Vec3 towBarOffset = DEFAULT_TOW_BAR_OFFSET;
        private Vec3 trailerOffset = DEFAULT_TRAILER_OFFSET;
        private boolean canChangeWheels = DEFAULT_CAN_CHANGE_WHEELS;
        private boolean immuneToFallDamage = DEFAULT_IMMUNE_TO_FALL_DAMAGE;
        private boolean canPlayerCarry = DEFAULT_CAN_PLAYER_CARRY;
        private boolean canFitInTrailer = DEFAULT_CAN_FIT_IN_TRAILER;
        private final List<Wheel> wheels = new ArrayList<>();
        private Transform bodyTransform = DEFAULT_BODY_TRANSFORM;
        private Transform displayTransform = DEFAULT_DISPLAY_TRANSFORM;
        private final List<Seat> seats = new ArrayList<>();
        private boolean canBePainted = DEFAULT_CAN_BE_PAINTED;
        private CameraProperties camera = CameraProperties.DEFAULT_CAMERA;
        private final Map<ResourceLocation, ExtendedProperties> extended = new HashMap<>();
        private final Map<ResourceLocation, CosmeticProperties> cosmetics = new HashMap<>();

        @SuppressWarnings("unused") //FIXME: unused
        public Builder setMaxHealth(float maxHealth)
        {
            this.maxHealth = maxHealth;
            return this;
        }

        public Builder setAxleOffset(float axleOffset)
        {
            this.axleOffset = axleOffset;
            return this;
        }

        public Builder setHeldOffset(double x, double y, double z)
        {
            this.heldOffset = new Vec3(x, y, z);
            return this;
        }

        public Builder setHeldOffset(Vec3 vec)
        {
            this.heldOffset = vec;
            return this;
        }

        public Builder setTowBarPosition(double x, double y, double z)
        {
            this.towBarOffset = new Vec3(x, y, z);
            return this;
        }

        public Builder setCanTowTrailers(boolean canTowTrailers)
        {
            this.canTowTrailers = canTowTrailers;
            return this;
        }

        public Builder setTowBarOffset(Vec3 vec)
        {
            this.towBarOffset = vec;
            return this;
        }

        public Builder setTrailerOffset(double x, double y, double z)
        {
            this.trailerOffset = new Vec3(x, y, z);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue") //FIXME: unused return
        public Builder setTrailerOffset(Vec3 vec)
        {
            this.trailerOffset = vec;
            return this;
        }

        public Builder setCanChangeWheels(boolean canChangeWheels)
        {
            this.canChangeWheels = canChangeWheels;
            return this;
        }

        public Builder setImmuneToFallDamage(boolean immuneToFallDamage) //FIXME: unused return
        {
            this.immuneToFallDamage = immuneToFallDamage;
            return this;
        }

        public Builder setCanPlayerCarry(boolean canPlayerCarry) //FIXME: unused return
        {
            this.canPlayerCarry = canPlayerCarry;
            return this;
        }

        public Builder setCanFitInTrailer(boolean canFitInTrailer)
        {
            this.canFitInTrailer = canFitInTrailer;
            return this;
        }

        public Builder addWheel(Wheel wheel) //FIXME: unused return
        {
            this.wheels.add(wheel);
            return this;
        }

        public Builder addWheel(Wheel.Builder builder)
        {
            this.wheels.add(builder.build());
            return this;
        }

        public Builder setBodyTransform(Transform bodyTransform)
        {
            this.bodyTransform = bodyTransform;
            return this;
        }

        public Builder setDisplayTransform(Transform displayTransform)
        {
            this.displayTransform = displayTransform;
            return this;
        }

        public Builder addSeat(Seat seat)
        {
            this.seats.add(seat);
            return this;
        }

        public Builder setCanBePainted(boolean canBePainted)
        {
            this.canBePainted = canBePainted;
            return this;
        }

        public Builder setCamera(CameraProperties.Builder builder)
        {
            this.camera = builder.build();
            return this;
        }

        public Builder setCamera(CameraProperties camera)
        {
            this.camera = camera;
            return this;
        }

        public Builder addExtended(ExtendedProperties properties)
        {
            this.extended.put(properties.getId(), properties);
            return this;
        }

        public Builder addCosmetic(CosmeticProperties properties)
        {
            this.cosmetics.put(properties.getId(), properties);
            return this;
        }

        public Builder addCosmetic(CosmeticProperties.Builder builder)
        {
            CosmeticProperties properties = builder.build();
            this.cosmetics.put(properties.getId(), properties);
            return this;
        }

        public VehicleProperties build(boolean scaleWheels)
        {
            this.validate();
            float wheelOffset = this.calculateWheelOffset();
            List<Wheel> wheels = scaleWheels ? this.generateScaledWheels(wheelOffset) : this.wheels;
            return new VehicleProperties(this.maxHealth, this.axleOffset, wheelOffset, this.heldOffset, this.canTowTrailers, this.towBarOffset, this.trailerOffset, this.canChangeWheels, this.immuneToFallDamage, this.canPlayerCarry, this.canFitInTrailer, wheels, this.bodyTransform, this.displayTransform, this.seats, this.canBePainted, this.camera, this.extended, this.cosmetics);
        }

        private void validate()
        {
            if(this.seats.stream().filter(Seat::isDriver).count() > 1)
            {
                throw new RuntimeException("Unable to build vehicles properties. The maximum amount of drivers seats is one but tried to add more.");
            }
        }

        private List<Wheel> generateScaledWheels(float wheelOffset)
        {
            return this.wheels.stream().map(wheel -> {
                if(!wheel.isAutoScale()) return wheel;
                double scale = (wheelOffset + wheel.getOffsetY()) / (WHEEL_DIAMETER / 2.0);
                double xScale = wheel.getScale().x != 0.0 ? wheel.getScale().x : scale;
                double yScale = wheel.getScale().y != 0.0 ? wheel.getScale().y : scale;
                double zScale = wheel.getScale().z != 0.0 ? wheel.getScale().z : scale;
                Vec3 newScale = new Vec3(xScale, yScale, zScale);
                return wheel.rescale(newScale);
            }).collect(Collectors.toList());
        }

        private float calculateWheelOffset()
        {
            return (float) this.wheels.stream().filter(wheel -> !wheel.isAutoScale()).mapToDouble(this::getWheelOffset).max().orElse(0);
        }

        private float getWheelOffset(Wheel wheel)
        {
            double scaledDiameter = WHEEL_DIAMETER * wheel.getScaleY();
            return (float) (scaledDiameter / 2.0) - wheel.getOffsetY();
        }
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class Manager extends SimplePreparableReloadListener<Map<ResourceLocation, VehicleProperties>>
    {
        private static final String PROPERTIES_DIRECTORY = "vehicles/properties";
        private static final String COSMETICS_DIRECTORY = "vehicles/cosmetics";
        private static final String FILE_SUFFIX = ".json";

        @Nullable
        private static Manager instance;
        private Map<ResourceLocation, VehicleProperties> vehicleProperties;

        @Override
        protected @NotNull Map<ResourceLocation, VehicleProperties> prepare(ResourceManager manager, @NotNull ProfilerFiller profiler)
        {
            Map<ResourceLocation, VehicleProperties> propertiesMap = new HashMap<>();
            manager.listResources(PROPERTIES_DIRECTORY, location -> location.toString().endsWith(FILE_SUFFIX))
                .entrySet()
                .stream()
                .filter(entry -> DEFAULT_VEHICLE_PROPERTIES.containsKey(format(entry.getKey(), PROPERTIES_DIRECTORY)))
                .forEach(entry -> {
                    try
                    {
                        Resource resource = manager.getResource(entry.getKey()).orElseThrow();
                        InputStream stream = resource.open();
                        VehicleProperties properties = loadPropertiesFromStream(stream);
                        propertiesMap.put(format(entry.getKey(), PROPERTIES_DIRECTORY), properties);
                        stream.close();
                    }
                    catch(IOException e)
                    {
                        VehicleMod.LOGGER.error("Couldn't parse vehicle properties {}", entry.getKey());
                    }
                });

            propertiesMap.forEach((id, properties) ->
            {
                // Skips if vehicle has not cosmetics
                if(properties.getCosmetics().isEmpty())
                    return;

                // Loads the cosmetics json for applicable vehicles
                Map<ResourceLocation, List<Pair<ResourceLocation, List<ResourceLocation>>>> modelMap = new HashMap<>();
                manager.listResources(COSMETICS_DIRECTORY, fileName -> fileName.toString().equals(id.getPath() + FILE_SUFFIX))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparing(
                                (entry) -> entry.getKey().getPath(),
                                (n1, n2) -> n1.equals(n2) ? 0 : n1.equals(Reference.MOD_ID) ? 1 : -1))
                                    .forEach(entry -> {
                                ResourceLocation vehicleId = format(entry.getKey(), COSMETICS_DIRECTORY);
                                if(!vehicleId.getNamespace().equals(id.getNamespace()))
                                    return;
                                CosmeticProperties.deserializeModels(entry.getKey(), manager, modelMap);
                });

                // Applies the list of valid model locations to the corresponding cosmetic
                modelMap.forEach((cosmeticId, models) -> {
                    CosmeticProperties cosmetic = properties.getCosmetics().get(cosmeticId);
                    if(cosmetic == null)
                        return;
                    cosmetic.setModelLocations(models.stream().map(Pair::getLeft).collect(Collectors.toList()));
                    cosmetic.setDisabledCosmetics(models.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
                });
            });
            return propertiesMap;
        }

        @Override
        @ParametersAreNonnullByDefault
        protected void apply(Map<ResourceLocation, VehicleProperties> propertiesMap, ResourceManager manager, ProfilerFiller profiler)
        {
            this.vehicleProperties = ImmutableMap.copyOf(propertiesMap);
        }

        @Nullable
        public Map<ResourceLocation, VehicleProperties> getVehicleProperties()
        {
            return this.vehicleProperties;
        }

        private static ResourceLocation format(ResourceLocation location, String directory)
        {
            return new ResourceLocation(location.getNamespace(), location.getPath().substring(directory.length() + 1, location.getPath().length() - FILE_SUFFIX.length()));
        }

        @SubscribeEvent
        public static void addReloadListenerEvent(AddReloadListenerEvent event)
        {
            event.addListener(Manager.instance = new Manager()); // I still think this looks ugly
        }

        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event)
        {
            Manager.instance = null;
        }

        /**
         * Gets the vehicle properties manager. This will be null if the client isn't running an
         * integrated server or the client is connected to a dedicated server.
         */
        @Nullable
        public static Manager get()
        {
            return instance;
        }

        public void writeVehicleProperties(FriendlyByteBuf buffer)
        {
            buffer.writeVarInt(this.vehicleProperties.size());
            this.vehicleProperties.forEach((id, properties) ->
            {
                buffer.writeResourceLocation(id);
                buffer.writeUtf(GSON.toJson(properties));
                writeCosmeticModelLocations(buffer, properties);
            });
        }

        public static ImmutableMap<ResourceLocation, VehicleProperties> readVehicleProperties(FriendlyByteBuf buffer)
        {
            int size = buffer.readVarInt();
            if(size > 0)
            {
                ImmutableMap.Builder<ResourceLocation, VehicleProperties> builder = ImmutableMap.builder();
                for(int i = 0; i < size; i++)
                {
                    ResourceLocation id = buffer.readResourceLocation();
                    String json = buffer.readUtf();
                    VehicleProperties properties = GSON.fromJson(json, VehicleProperties.class);
                    builder.put(id, properties);
                    readCosmeticModelLocations(buffer, properties);
                }
                return builder.build();
            }
            return ImmutableMap.of();
        }

        private static void writeCosmeticModelLocations(FriendlyByteBuf buffer, VehicleProperties properties)
        {
            buffer.writeInt(properties.getCosmetics().size());
            properties.getCosmetics().forEach((cosmeticId, cosmeticProperties) ->
            {
                buffer.writeResourceLocation(cosmeticId);
                buffer.writeInt(cosmeticProperties.getModelLocations().size());
                cosmeticProperties.getModelLocations().forEach(location ->
                {
                    buffer.writeResourceLocation(location);
                    List<ResourceLocation> disabledCosmetics = cosmeticProperties.getDisabledCosmetics().get(location);
                    buffer.writeInt(disabledCosmetics.size());
                    disabledCosmetics.forEach(buffer::writeResourceLocation);
                });
            });
        }

        private static void readCosmeticModelLocations(FriendlyByteBuf buffer, VehicleProperties properties)
        {
            int cosmeticsLength = buffer.readInt();
            for(int i = 0; i < cosmeticsLength; i++)
            {
                List<Pair<ResourceLocation, List<ResourceLocation>>> models = new ArrayList<>();
                ResourceLocation cosmeticId = buffer.readResourceLocation();
                int modelsLength = buffer.readInt();
                for(int j = 0; j < modelsLength; j++)
                {
                    ResourceLocation modelLocation = buffer.readResourceLocation();
                    List<ResourceLocation> disabledCosmetics = new ArrayList<>();
                    int disabledCosmeticsLength = buffer.readInt();
                    for(int k = 0; k < disabledCosmeticsLength; k++)
                    {
                        disabledCosmetics.add(buffer.readResourceLocation());
                    }
                    models.add(Pair.of(modelLocation, disabledCosmetics));
                }
                Optional.ofNullable(properties.getCosmetics().get(cosmeticId)).ifPresent(cosmetic ->
                {
                    cosmetic.setModelLocations(models.stream().map(Pair::getLeft).collect(Collectors.toList()));
                    cosmetic.setDisabledCosmetics(models.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
                });
            }
        }
    }
}
