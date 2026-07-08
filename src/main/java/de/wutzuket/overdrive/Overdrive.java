package de.wutzuket.overdrive;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.client.event.InputEvent;

import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorRenderer;
import de.wutzuket.overdrive.block.scanner.scanner_renderer;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe_manager;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpRenderer;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.fluid.ODFluidBlocks;
import de.wutzuket.overdrive.fluid.ODFluidTypes;
import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.fluid.ODItemsFluid;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODCreativeTab;
import de.wutzuket.overdrive.index.ODDataComponents;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.index.ODMenuTypes;
import de.wutzuket.overdrive.menu.AcceleratorControllerScreen;
import de.wutzuket.overdrive.menu.AcceleratorOutputScreen;
import de.wutzuket.overdrive.menu.IridiumTargetScreen;
import de.wutzuket.overdrive.menu.VacuumPumpScreen;
import de.wutzuket.overdrive.recipe.ODRecipeSerializers;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import de.wutzuket.overdrive.worldgen.ODBiomeModifiers;
import de.wutzuket.overdrive.worldgen.ODWorldGen;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;

@Mod(Overdrive.MODID)
public class Overdrive {
    public static final String MODID = "overdrive";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Overdrive(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ODConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        ODBlocks.register(modEventBus);
        ODBlockEntities.register(modEventBus);
        ODItems.register(modEventBus);
        ODCreativeTab.register(modEventBus);
        ODMenuTypes.register(modEventBus);
        ODRecipeTypes.register(modEventBus);
        ODRecipeSerializers.register(modEventBus);
        ODFluidTypes.register(modEventBus);
        ODFluids.register(modEventBus);
        ODFluidBlocks.register(modEventBus);
        ODItemsFluid.register(modEventBus);
        ODWorldGen.register(modEventBus);
        ODBiomeModifiers.register(modEventBus);
        ODDataComponents.register(modEventBus);

        modEventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.CREATIVE_ENERGY_CUBE_BLOCK_ENTITY.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.SCANNER_BLOCK_ENTITY.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.VACUUM_PUMP_BLOCK_ENTITY.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.ENERGY_INPUT_PORT_BLOCK_ENTITY.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.PARTICLE_ACCELERATOR_CONTROLLER.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.ACCELERATOR_ENERGY_INPUT_PORT.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK,
                    ODBlockEntities.IRIDIUM_TARGET.get(),
                    (blockEntity, side) -> blockEntity.getItemHandler()
            );
            event.registerBlockEntity(
                    Capabilities.FluidHandler.BLOCK,
                    ODBlockEntities.PARTICLE_ACCELERATOR_CONTROLLER.get(),
                    (blockEntity, side) -> ((ParticleAcceleratorControllerEntity) blockEntity).getFluidTank()
            );
            event.registerBlockEntity(
                    Capabilities.FluidHandler.BLOCK,
                    ODBlockEntities.ACCELERATOR_OUTPUT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.accelerator.AcceleratorOutputEntity) blockEntity).getFluidTank()
            );
            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK,
                    ODBlockEntities.ACCELERATOR_OUTPUT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.accelerator.AcceleratorOutputEntity) blockEntity).getItemHandler()
            );
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(scanner_recipe_manager.INSTANCE);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        if (ModList.get().isLoaded("theoneprobe")) {
            de.wutzuket.overdrive.integration.top.TOPCompatibility.register();
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    private static final net.minecraft.client.KeyMapping TOGGLE_ACCELERATOR_HOLOGRAM_KEY = new net.minecraft.client.KeyMapping(
            "key.overdrive.toggle_accelerator_hologram",
            org.lwjgl.glfw.GLFW.GLFW_KEY_H,
            "key.categories.gameplay"
    );

    private static final java.util.Map<net.minecraft.core.BlockPos, Boolean> CLIENT_HOLOGRAMS = new java.util.HashMap<>();

    public static boolean isClientHologramEnabled(net.minecraft.core.BlockPos pos) {
        return CLIENT_HOLOGRAMS.getOrDefault(pos, false);
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            NeoForge.EVENT_BUS.addListener(ClientModEvents::onClientTick);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_ACCELERATOR_HOLOGRAM_KEY);
        }

        public static void onClientTick(ClientTickEvent.Pre event) {
            if (TOGGLE_ACCELERATOR_HOLOGRAM_KEY.consumeClick()) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.player != null && mc.level != null) {
                    net.minecraft.core.BlockPos targetPos;
                    if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                        targetPos = blockHit.getBlockPos();
                        net.minecraft.world.level.block.entity.BlockEntity be = mc.level.getBlockEntity(targetPos);
                        if (be instanceof ParticleAcceleratorControllerEntity) {
                            boolean current = CLIENT_HOLOGRAMS.getOrDefault(targetPos, false);
                            CLIENT_HOLOGRAMS.put(targetPos, !current);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ODBlockEntities.SCANNER_BLOCK_ENTITY.get(), scanner_renderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.VACUUM_PUMP_BLOCK_ENTITY.get(), VacuumPumpRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.PARTICLE_ACCELERATOR_CONTROLLER.get(), ParticleAcceleratorRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ODMenuTypes.VACUUM_PUMP_MENU.get(), VacuumPumpScreen::new);
            event.register(ODMenuTypes.IRIDIUM_TARGET_MENU.get(), IridiumTargetScreen::new);
            event.register(ODMenuTypes.ACCELERATOR_CONTROLLER_MENU.get(), AcceleratorControllerScreen::new);
            event.register(ODMenuTypes.ACCELERATOR_OUTPUT_MENU.get(), AcceleratorOutputScreen::new);
        }

        @SubscribeEvent
        public static void registerFluidTypeExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return ResourceLocation.withDefaultNamespace("block/water_still");
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return ResourceLocation.withDefaultNamespace("block/water_flow");
                }

                @Override
                public int getTintColor() {
                    return 0xFFAA00FF;
                }
            }, ODFluidTypes.ANTIPROTONS_TYPE.get());
        }
    }
}
