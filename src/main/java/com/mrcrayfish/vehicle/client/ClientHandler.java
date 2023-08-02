package com.mrcrayfish.vehicle.client;

import com.mojang.blaze3d.platform.ScreenManager;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.handler.*;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.particle.DustParticle;
import com.mrcrayfish.vehicle.client.particle.TyreSmokeParticle;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.render.blockentity.*;
import com.mrcrayfish.vehicle.client.render.vehicle.*;
import com.mrcrayfish.vehicle.client.screen.*;
import com.mrcrayfish.vehicle.entity.trailer.*;
import com.mrcrayfish.vehicle.entity.vehicle.MopedEntity;
import com.mrcrayfish.vehicle.entity.vehicle.SportsCarEntity;
import com.mrcrayfish.vehicle.init.*;
import com.mrcrayfish.vehicle.item.PartItem;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.VehicleUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.Tag;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler
{
    private static boolean controllableLoaded = false;

    public static boolean isControllableLoaded()
    {
        return controllableLoaded;
    }

    public static void setup()
    {
        if(ModList.get().isLoaded("controllable"))
        {
            ClientHandler.controllableLoaded = true;
            MinecraftForge.EVENT_BUS.register(new ControllerHandler());
            ControllerHandler.init();
        }

        MinecraftForge.EVENT_BUS.register(EntityRayTracer.instance());
        MinecraftForge.EVENT_BUS.register(CosmeticCache.instance());
        MinecraftForge.EVENT_BUS.register(CameraHandler.instance());
        MinecraftForge.EVENT_BUS.register(new FuelingHandler());
        MinecraftForge.EVENT_BUS.register(new HeldVehicleHandler());
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        MinecraftForge.EVENT_BUS.register(new OverlayHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());
        MinecraftForge.EVENT_BUS.register(new SprayCanHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        setupCustomBlockModels();
        setupRenderLayers();
        setupVehicleRenders();
        setupScreenFactories();
        setupInteractableVehicles();

        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if(manager instanceof ReloadableResourceManager)
        {
            ((ReloadableResourceManager) manager).registerReloadListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> stage.wait(Unit.INSTANCE).thenRun(() -> {
                FluidUtils.clearCacheFluidColor();
                EntityRayTracer.instance().clearDataForReregistration();
                ComponentManager.clearCache();
            }));
        }
    }

    private static void setupCustomBlockModels()
    {
        //TODO add custom loader
        //RegisterGeometryLoaders.registerLoader(new CustomLoader());
        //RegisterGeometryLoaders.registerLoader(new ResourceLocation(Reference.MOD_ID, "ramp"), new CustomLoader());
    }

    //TODO remove reliance on deprecated #setRenderLayer method
    @SuppressWarnings("removal")
    private static void setupRenderLayers()
    {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORKSTATION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_EXTRACTOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GAS_PUMP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.BLAZE_JUICE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_BLAZE_JUICE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_CONE.get(), RenderType.cutout());
    }

    private static void setupVehicleRenders()
    {
        /* Register Vehicles */
        VehicleUtil.registerVehicleRenderer(ModEntities.QUAD_BIKE.get(), QuadBikeRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.SPORTS_CAR.get(), SportsCarRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.GO_KART.get(), GoKartRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.JET_SKI.get(), JetSkiRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.LAWN_MOWER.get(), LawnMowerRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.MOPED.get(), MopedRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.SPORTS_PLANE.get(), SportsPlaneRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.GOLF_CART.get(), GolfCartRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.OFF_ROADER.get(), OffRoaderRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.TRACTOR.get(), TractorRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.MINI_BUS.get(), MiniBusRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.DIRT_BIKE.get(), DirtBikeRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.COMPACT_HELICOPTER.get(), CompactHelicopterRenderer::new);

        /* Register Trailers */
        VehicleUtil.registerVehicleRenderer(ModEntities.VEHICLE_TRAILER.get(), VehicleTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.STORAGE_TRAILER.get(), StorageTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.FLUID_TRAILER.get(), FluidTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.SEEDER.get(), SeederTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(ModEntities.FERTILIZER.get(), FertilizerTrailerRenderer::new);

        /* Register Mod Exclusive Vehicles */
        if(ModList.get().isLoaded("cfm"))
        {
            assert ModEntities.SOFACOPTER != null;
            VehicleUtil.registerVehicleRenderer(ModEntities.SOFACOPTER.get(), SofaHelicopterRenderer::new);
        }

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.JACK.get(), com.mrcrayfish.vehicle.client.render.JackRenderer::new);
    }

    @SubscribeEvent
    private static void setupEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(ModTileEntities.FLUID_EXTRACTOR.get(), FluidExtractorRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.INDUSTRIAL_FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.VEHICLE_CRATE.get(), VehicleCrateRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.JACK.get(), com.mrcrayfish.vehicle.client.render.blockentity.JackRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.GAS_PUMP.get(), GasPumpRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.GAS_PUMP_TANK.get(), GasPumpTankRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.FLUID_PUMP.get(), FluidPumpRenderer::new);
    }

    private static void setupScreenFactories()
    {
        ScreenManager.register(ModContainers.FLUID_EXTRACTOR.get(), FluidExtractorScreen::new);
        ScreenManager.register(ModContainers.FLUID_MIXER.get(), FluidMixerScreen::new);
        ScreenManager.register(ModContainers.EDIT_VEHICLE.get(), EditVehicleScreen::new);
        ScreenManager.register(ModContainers.WORKSTATION.get(), WorkstationScreen::new);
        ScreenManager.register(ModContainers.STORAGE.get(), StorageScreen::new);
    }

    @SubscribeEvent
    private static void setupItemColors(RegisterColorHandlersEvent.Item event)
    {
        ItemColor color = (stack, index) ->
        {
            if(index == 0 && stack.hasTag()) {
                assert stack.getTag() != null;
                if (stack.getTag().contains("Color", Tag.TAG_INT)) {
                    return stack.getTag().getInt("Color");
                }
            }
            return 0xFFFFFF;
        };

        ForgeRegistries.ITEMS.forEach(item ->
        {
            if(item instanceof SprayCanItem || (item instanceof PartItem && ((PartItem) item).isColored()))
            {
                event.register(color, item);
            }
        });
    }

    private static void setupInteractableVehicles()
    {
        MopedEntity.registerInteractionBoxes();
        FertilizerTrailerEntity.registerInteractionBoxes();
        FluidTrailerEntity.registerInteractionBoxes();
        SeederTrailerEntity.registerInteractionBoxes();
        StorageTrailerEntity.registerInteractionBoxes();
        VehicleTrailerEntity.registerInteractionBoxes();
        SportsCarEntity.registerInteractionBoxes();
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event)
    {
        event.register(ModParticleTypes.TYRE_SMOKE.get(), TyreSmokeParticle.Factory::new);
        event.register(ModParticleTypes.DUST.get(), DustParticle.Factory::new);
    }
}
