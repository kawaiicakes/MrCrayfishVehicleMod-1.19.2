package com.mrcrayfish.vehicle;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.vehicle.client.ClientHandler;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.common.CommonEvents;
import com.mrcrayfish.vehicle.common.FluidNetworkHandler;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.crafting.ModRecipeTypes;
import com.mrcrayfish.vehicle.datagen.LootTableGen;
import com.mrcrayfish.vehicle.datagen.RecipeGen;
import com.mrcrayfish.vehicle.datagen.VehiclePropertiesGen;
import com.mrcrayfish.vehicle.entity.properties.*;
import com.mrcrayfish.vehicle.init.*;
import com.mrcrayfish.vehicle.network.PacketHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class VehicleMod {
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab(Reference.MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ModItems.IRON_SMALL_ENGINE.get());
        }
    };

    public VehicleMod() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.REGISTER.register(eventBus);
        ModItems.REGISTER.register(eventBus);
        ModEntities.REGISTER.register(eventBus);
        ModTileEntities.REGISTER.register(eventBus);
        ModContainers.REGISTER.register(eventBus);
        ModParticleTypes.REGISTER.register(eventBus);
        ModSounds.REGISTER.register(eventBus);
        ModRecipeSerializers.REGISTER.register(eventBus);
        ModRecipeTypes.REGISTER.register(eventBus);
        ModFluids.REGISTER.register(eventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onClientSetup);
        eventBus.addListener(this::onGatherData);
        eventBus.addListener(this::onMissingMapping);

        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        MinecraftForge.EVENT_BUS.register(new ModCommands());
        MinecraftForge.EVENT_BUS.register(FluidNetworkHandler.instance());

        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "powered"), PoweredProperties.class, PoweredProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "land"), LandProperties.class, LandProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "motorcycle"), MotorcycleProperties.class, MotorcycleProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "plane"), PlaneProperties.class, PlaneProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "helicopter"), HelicopterProperties.class, HelicopterProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "trailer"), TrailerProperties.class, TrailerProperties::new);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ComponentManager.registerLoader(VehicleModels.LOADER));
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        ModRecipeTypes.init();
        VehicleProperties.loadDefaultProperties();
        PacketHandler.registerPlayMessage();
        HeldVehicleDataHandler.register();
        ModDataKeys.register();
        ModLootFunctions.init();
        CraftingHelper.register(new ResourceLocation(Reference.MOD_ID, "workstation_ingredient"), CompoundIngredient.Serializer.INSTANCE);
        event.enqueueWork(() -> VehicleProperties.registerDynamicProvider(() -> new VehiclePropertiesGen(null)));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ClientHandler.setup();
    }

    private void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(true, new LootTableGen(generator));
        generator.addProvider(true, new RecipeGen(generator));
        generator.addProvider(true, new VehiclePropertiesGen(generator));
    }

    public void onMissingMapping(MissingMappingsEvent event) { //FIXME: scuffed implementation lmfaoooo
        ImmutableList<MissingMappingsEvent.Mapping<Item>> itemMappings = ImmutableList.copyOf(event.getAllMappings(ForgeRegistries.Keys.ITEMS).stream().filter(e -> e.getKey().getNamespace().equals(Reference.MOD_ID)).collect(Collectors.toList()));
        ImmutableList<MissingMappingsEvent.Mapping<SoundEvent>> soundMappings = ImmutableList.copyOf(event.getAllMappings(ForgeRegistries.Keys.SOUND_EVENTS).stream().filter(e -> e.getKey().getNamespace().equals(Reference.MOD_ID)).collect(Collectors.toList()));
        ImmutableList<MissingMappingsEvent.Mapping<EntityType<?>>> entityTypeMappings = ImmutableList.copyOf(event.getAllMappings(ForgeRegistries.Keys.ENTITY_TYPES).stream().filter(e -> e.getKey().getNamespace().equals(Reference.MOD_ID)).collect(Collectors.toList()));
        for (MissingMappingsEvent.Mapping<Item> missing : itemMappings) {
            if (missing.getKey().getNamespace().equals(Reference.MOD_ID) && CommonEvents.getIgnoreItems().contains(missing.getKey().getPath())) {
                missing.ignore();
            }
        }
        for (MissingMappingsEvent.Mapping<SoundEvent> missing : soundMappings) {
            if (missing.getKey().getNamespace().equals(Reference.MOD_ID) && CommonEvents.getIgnoreSounds().contains(missing.getKey().getPath())) {
                missing.ignore();
            }
        }
        for (MissingMappingsEvent.Mapping<EntityType<?>> missing : entityTypeMappings) {
            if (missing.getKey().getNamespace().equals(Reference.MOD_ID) && CommonEvents.getIgnoreEntities().contains(missing.getKey().getPath())) {
                missing.ignore();
            }
        }
    }
}