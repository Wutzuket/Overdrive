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
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import de.wutzuket.overdrive.antimatter.ODAntimatterCapability;
import de.wutzuket.overdrive.antimatter.ODAntimatters;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorRenderer;
import de.wutzuket.overdrive.block.advanced_hologram.AdvancedHologramBlockRenderer;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeRenderer;
import de.wutzuket.overdrive.block.hologram.HologramBlockRenderer;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpRenderer;
import de.wutzuket.overdrive.block.scanner.scanner_renderer;
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
import de.wutzuket.overdrive.menu.ADControllerScreen;
import de.wutzuket.overdrive.menu.AdvancedHologramScreen;
import de.wutzuket.overdrive.menu.ELENAControllerScreen;
import de.wutzuket.overdrive.menu.HologramScreen;
import de.wutzuket.overdrive.menu.VacuumPumpScreen;
import de.wutzuket.overdrive.menu.TungstenTargetScreen;
import de.wutzuket.overdrive.recipe.ODRecipeSerializers;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import de.wutzuket.overdrive.worldgen.ODBiomeModifiers;
import de.wutzuket.overdrive.worldgen.ODWorldGen;

import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
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
        ODAntimatters.register(modEventBus);

        // Die Antimatter-Typen und ihre Cells werden vollstaendig automatisch registriert:
        // fuer jeden Typ in ODAntimatters gibt es ein Cell-Item, eine prozedurale Textur und
        // einen Eintrag im Creative-Tab.
        modEventBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(ODAntimatters.REGISTRY_KEY)) {
                for (de.wutzuket.overdrive.antimatter.Antimatter type : ODAntimatters.getTypes()) {
                    event.register(ODAntimatters.REGISTRY_KEY, type.id(), () -> type);
                }
                return;
            }
            if (event.getRegistryKey().equals(Registries.ITEM)) {
                for (de.wutzuket.overdrive.antimatter.Antimatter type : ODAntimatters.getTypes()) {
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID,
                            type.id().getPath() + "_cell");
                    event.register(Registries.ITEM, id, () -> new de.wutzuket.overdrive.item.AntimatterCellItem(
                            new net.minecraft.world.item.Item.Properties().stacksTo(1), type, true));
                    ODItems.registerAntimatterCell(type, id);

                    // fuer jeden Typ gibt es auch einen kreativen Tank
                    ResourceLocation creativeId = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID,
                            "creative_" + type.id().getPath() + "_tank");
                    event.register(Registries.ITEM, creativeId,
                            () -> new de.wutzuket.overdrive.item.CreativeAntimatterTankItem(
                                    new net.minecraft.world.item.Item.Properties().stacksTo(1), type));
                    ODItems.registerCreativeAntimatterTank(type, creativeId);
                }
            }
        });

        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            PayloadRegistrar registrar = event.registrar(MODID);
            registrar.playToServer(
                    de.wutzuket.overdrive.network.ToggleHologramPayload.TYPE,
                    de.wutzuket.overdrive.network.ToggleHologramPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleToggleHologram
            );
registrar.playToServer(
                    de.wutzuket.overdrive.network.HologramMenuUpdatePayload.TYPE,
                    de.wutzuket.overdrive.network.HologramMenuUpdatePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleHologramMenuUpdate
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.HologramCreateFilePayload.TYPE,
                    de.wutzuket.overdrive.network.HologramCreateFilePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleHologramCreateFile
            );
registrar.playToClient(
                    de.wutzuket.overdrive.network.HologramMenuStatePayload.TYPE,
                    de.wutzuket.overdrive.network.HologramMenuStatePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ClientPayloadHandler::handleHologramMenuState
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AdvancedHologramScriptPayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramScriptPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAdvancedHologramScript
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AdvancedHologramRunPayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramRunPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAdvancedHologramRun
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AdvancedHologramConsoleInputPayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramConsoleInputPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAdvancedHologramConsoleInput
            );
            registrar.playToClient(
                    de.wutzuket.overdrive.network.AdvancedHologramMenuStatePayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramMenuStatePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ClientPayloadHandler::handleAdvancedHologramMenuState
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AdvancedHologramSelectScriptPayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramSelectScriptPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAdvancedHologramSelectScript
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AdvancedHologramSaveScriptPayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramSaveScriptPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAdvancedHologramSaveScript
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AdvancedHologramCreateScriptPayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramCreateScriptPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAdvancedHologramCreateScript
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AdvancedHologramDeleteScriptPayload.TYPE,
                    de.wutzuket.overdrive.network.AdvancedHologramDeleteScriptPayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAdvancedHologramDeleteScript
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AnnihilationTogglePayload.TYPE,
                    de.wutzuket.overdrive.network.AnnihilationTogglePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAnnihilationToggle
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AnnihilationBurnRatePayload.TYPE,
                    de.wutzuket.overdrive.network.AnnihilationBurnRatePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAnnihilationBurnRate
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.AcceleratorEnergyRatePayload.TYPE,
                    de.wutzuket.overdrive.network.AcceleratorEnergyRatePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleAcceleratorEnergyRate
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.ADEnergyRatePayload.TYPE,
                    de.wutzuket.overdrive.network.ADEnergyRatePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleADEnergyRate
            );
            registrar.playToServer(
                    de.wutzuket.overdrive.network.ELENAEnergyRatePayload.TYPE,
                    de.wutzuket.overdrive.network.ELENAEnergyRatePayload.STREAM_CODEC,
                    de.wutzuket.overdrive.network.ServerPayloadHandler::handleELENAEnergyRate
            );
        });

        modEventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.CREATIVE_ENERGY_CUBE_BLOCK_ENTITY.get(),
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
                    ODBlockEntities.SCANNER_BLOCK_ENTITY.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ODBlockEntities.ACCELERATOR_ENERGY_INPUT_PORT.get(),
                    (blockEntity, side) -> blockEntity
            );
            event.registerBlockEntity(
                    Capabilities.FluidHandler.BLOCK,
                    ODBlockEntities.IRIDIUM_TARGET.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.accelerator.IridiumTargetEntity) blockEntity).getTank()
            );
            // Der Antiprotonen-Output laeuft ueber den Accelerator-Output-Block und wird nur noch
            // ueber das Antimatter-System bewegt (siehe Antimatter-Capability weiter unten).
            // Fluessiges Iridium wird direkt am Iridium-Target befuellt (Eimer/Pipes -> Target-Tank).
            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK,
                    ODBlockEntities.ACCELERATOR_OUTPUT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.accelerator.AcceleratorOutputEntity) blockEntity).getItemHandler()
            );

            // AD Ring capabilities (Tanks liegen im Controller: fuellen -> Input, entnehmen -> Output)
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.AD_CONTROLLER.get(), (be, side) -> be);
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER, ODBlockEntities.AD_CONTROLLER.get(), (be, side) -> ((de.wutzuket.overdrive.block.ad.ADControllerEntity) be).getAntimatterHandler());
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.AD_ENERGY_INPUT_PORT.get(), (be, side) -> be);

            // Ports ohne eigenen Speicher leiten an die Controller-Tanks weiter



            // ELENA Ring capabilities (Tanks liegen im Controller: fuellen -> Input, entnehmen -> Output)
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.ELENA_CONTROLLER.get(), (be, side) -> be);
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER, ODBlockEntities.ELENA_CONTROLLER.get(), (be, side) -> ((de.wutzuket.overdrive.block.elena.ELENAControllerEntity) be).getAntimatterHandler());
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.ELENA_ENERGY_INPUT_PORT.get(), (be, side) -> be);




            // Tungsten Target capabilities
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.ELECTRON_GUN.get(), (be, side) -> be);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ODBlockEntities.TUNGSTEN_TARGET.get(), (be, side) -> ((de.wutzuket.overdrive.block.tungsten.TungstenTargetEntity) be).getItemHandler());

            // Step 4 - Annihilation
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ODBlockEntities.ANNIHILATION_VACUUM_PORT.get(), (be, side) -> null); // vacuum via IVacuumNode not fluid

            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ODBlockEntities.ANNIHILATION_COOLANT_INPUT.get(), (be, side) -> ((de.wutzuket.overdrive.block.annihilation.CoolantInputPortEntity)be).getTank());
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ODBlockEntities.ANNIHILATION_STEAM_OUTPUT.get(), (be, side) -> ((de.wutzuket.overdrive.block.annihilation.SteamOutputPortEntity)be).getTank());
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.ANNIHILATION_ENERGY_OUTPUT.get(), (be, side) -> (de.wutzuket.overdrive.block.annihilation.EnergyOutputPortEntity)be);
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ODBlockEntities.COOLING_TOWER_CONTROLLER.get(), (be, side) -> ((de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity)be).getInputTank());
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ODBlockEntities.COOLING_TOWER_INPUT_PORT.get(), (be, side) -> ((de.wutzuket.overdrive.block.coolingtower.CoolingTowerInputPortEntity)be).getTank());
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ODBlockEntities.COOLING_TOWER_OUTPUT_PORT.get(), (be, side) -> ((de.wutzuket.overdrive.block.coolingtower.CoolingTowerOutputPortEntity)be).getTank());

            // Cryo Distiller: Strom nur Eingang, Fluid getrennt Input/Output
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.CRYO_DISTILLER.get(), (be, side) -> be);
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ODBlockEntities.CRYO_DISTILLER.get(), (be, side) -> ((de.wutzuket.overdrive.block.distiller.CryoDistillerEntity) be).getFluidHandler());

            // Coal Generator: Energie-Quelle (Ausgang), kann nichts empfangen
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ODBlockEntities.COAL_GENERATOR_BLOCK_ENTITY.get(),
                    (net.minecraft.world.level.block.entity.BlockEntity be, net.minecraft.core.Direction side) -> (net.neoforged.neoforge.energy.IEnergyStorage) be);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ODBlockEntities.COAL_GENERATOR_BLOCK_ENTITY.get(),
                    (net.minecraft.world.level.block.entity.BlockEntity be, net.minecraft.core.Direction side) -> (net.neoforged.neoforge.items.IItemHandler) ((de.wutzuket.overdrive.block.coalgenerator.CoalGeneratorEntity) be).getFuelHandler());

            // Antimatter: eigene Capability, komplett unabhaengig vom Fluidsystem
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.ANTIMATTER_TANK.get(),
                    (blockEntity, side) -> blockEntity);

            // Antimatter an den Ports der Maschinen: die Ports geben den Antimatter direkt aus,
            // 1 Antimatter-Unit = 1 mB. Die Ports haben bewusst keine Fluid-Capability mehr, die
            // Antiprotonen und Positronen werden ausschliesslich ueber das Antimatter-System bewegt.
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.ACCELERATOR_OUTPUT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.accelerator.AcceleratorOutputEntity) blockEntity).getAntimatterTank());
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.AD_INPUT_PORT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.ad.ADInputPortEntity) blockEntity).getTank());
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.AD_OUTPUT_PORT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.ad.ADOutputPortEntity) blockEntity).getFluidTank());
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.ELENA_INPUT_PORT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.elena.ELENAInputPortEntity) blockEntity).getTank());
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.ELENA_OUTPUT_PORT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.elena.ELENAOutputPortEntity) blockEntity).getFluidTank());
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.ANNIHILATION_CELL_PORT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity) blockEntity).getTank());
            // Der Cell Docking Port nimmt Zellen auf, damit man den Reaktor per Hand befuellen kann.
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                    ODBlockEntities.ANNIHILATION_CELL_PORT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity) blockEntity).getItemHandler());
            // Der Elektronenstrahler und sein Positronen-Output erzeugen Positronen.
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.ELECTRON_GUN.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.tungsten.ElectronGunEntity) blockEntity).getAntimatterTank());
            event.registerBlockEntity(ODAntimatterCapability.ANTIMATTER_HANDLER,
                    ODBlockEntities.POSITRON_VACUUM_OUTPUT_PORT.get(),
                    (blockEntity, side) -> ((de.wutzuket.overdrive.block.tungsten.PositronVacuumOutputPortEntity) blockEntity).getTank());
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
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

    private static final java.util.Map<net.minecraft.core.BlockPos, Boolean> CLIENT_HOLOGRAMS = new java.util.HashMap<>();

    public static boolean isClientHologramEnabled(net.minecraft.core.BlockPos pos) {
        return CLIENT_HOLOGRAMS.getOrDefault(pos, false);
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        // The key mapping uses client only classes, so it has to live inside the client only
        // event subscriber. Otherwise the mod could not be loaded on a dedicated server.
        private static final net.minecraft.client.KeyMapping TOGGLE_ACCELERATOR_HOLOGRAM_KEY = new net.minecraft.client.KeyMapping(
                "key.overdrive.toggle_accelerator_hologram",
                org.lwjgl.glfw.GLFW.GLFW_KEY_H,
                "key.categories.gameplay"
        );

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        ODBlocks.CORE_CONTAINMENT_GLASS.get(),
                        net.minecraft.client.renderer.RenderType.translucent());
                // Das Antimatter-Rohr ist aus durchsichtigem Glas, deshalb muss es mit dem
                // translucent Render Type gezeichnet werden.
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        ODBlocks.ANTIMATTER_PIPE_BASIC.get(),
                        net.minecraft.client.renderer.RenderType.translucent());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        ODBlocks.ANTIMATTER_PIPE_ADVANCED.get(),
                        net.minecraft.client.renderer.RenderType.translucent());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        ODBlocks.ANTIMATTER_PIPE_ELITE.get(),
                        net.minecraft.client.renderer.RenderType.translucent());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                        ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get(),
                        net.minecraft.client.renderer.RenderType.translucent());
            });
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
                        net.minecraft.core.BlockPos togglePos = null;
                        if (be instanceof ParticleAcceleratorControllerEntity ||
                            be instanceof de.wutzuket.overdrive.block.ad.ADControllerEntity ||
                            be instanceof de.wutzuket.overdrive.block.elena.ELENAControllerEntity ||
                            be instanceof de.wutzuket.overdrive.block.tungsten.ElectronGunEntity ||
                            be instanceof de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity ||
                            be instanceof de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity ||
                            be instanceof de.wutzuket.overdrive.block.distiller.CryoDistillerEntity) {
                            togglePos = targetPos;
                        } else if (be instanceof de.wutzuket.overdrive.block.tungsten.TungstenTargetEntity) {
                            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                                if (!dir.getAxis().isHorizontal()) continue;
                                net.minecraft.world.level.block.entity.BlockEntity nearby = mc.level.getBlockEntity(targetPos.relative(dir, 1));
                                if (nearby instanceof de.wutzuket.overdrive.block.tungsten.ElectronGunEntity) {
                                    togglePos = targetPos.relative(dir, 1);
                                    break;
                                }
                            }
                        }
                        if (togglePos != null) {
                            boolean current = CLIENT_HOLOGRAMS.getOrDefault(togglePos, false);
                            CLIENT_HOLOGRAMS.put(togglePos, !current);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ODBlockEntities.VACUUM_PUMP_BLOCK_ENTITY.get(), VacuumPumpRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.PARTICLE_ACCELERATOR_CONTROLLER.get(), ParticleAcceleratorRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.AD_CONTROLLER.get(), de.wutzuket.overdrive.block.ad.ADRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.ELENA_CONTROLLER.get(), de.wutzuket.overdrive.block.elena.ELENARenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.ELECTRON_GUN.get(), de.wutzuket.overdrive.block.tungsten.TungstenTargetRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.HOLOGRAM_BLOCK_ENTITY.get(), HologramBlockRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.ADVANCED_HOLOGRAM_BLOCK_ENTITY.get(), AdvancedHologramBlockRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.ANNIHILATION_CONTROLLER.get(), de.wutzuket.overdrive.block.annihilation.AnnihilationRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.COOLING_TOWER_CONTROLLER.get(), de.wutzuket.overdrive.block.coolingtower.CoolingTowerRenderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.SCANNER_BLOCK_ENTITY.get(), scanner_renderer::new);
            event.registerBlockEntityRenderer(ODBlockEntities.ANTIMATTER_PIPE.get(), AntimatterPipeRenderer::new);
        }

        @SubscribeEvent
        public static void registerCellModels(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event) {
            // Die Cells werden komplett prozedural gezeichnet, deshalb ersetzen wir das Model
            // der automatisch registrierten Cell-Items. So braucht ein neuer Antimatter-Typ keine
            // Model- oder Textur-Datei.
            for (de.wutzuket.overdrive.antimatter.Antimatter type : ODAntimatters.getTypes()) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Overdrive.MODID,
                        type.id().getPath() + "_cell");
                event.getModels().replace(new net.minecraft.client.resources.model.ModelResourceLocation(
                        id, "inventory"), de.wutzuket.overdrive.item.AntimatterCellModel.INSTANCE);
            }
            // Auch die universelle leere Cell wird komplett gezeichnet
            event.getModels().replace(new net.minecraft.client.resources.model.ModelResourceLocation(
                    ResourceLocation.fromNamespaceAndPath(Overdrive.MODID, "empty_antimatter_cell"), "inventory"),
                    de.wutzuket.overdrive.item.AntimatterCellModel.INSTANCE);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ODMenuTypes.VACUUM_PUMP_MENU.get(), VacuumPumpScreen::new);
            event.register(ODMenuTypes.ACCELERATOR_CONTROLLER_MENU.get(), AcceleratorControllerScreen::new);
            event.register(ODMenuTypes.AD_CONTROLLER_MENU.get(), ADControllerScreen::new);
            event.register(ODMenuTypes.ELENA_CONTROLLER_MENU.get(), ELENAControllerScreen::new);
            event.register(ODMenuTypes.TUNGSTEN_TARGET_MENU.get(), TungstenTargetScreen::new);
            event.register(ODMenuTypes.ELECTRON_GUN_MENU.get(), de.wutzuket.overdrive.menu.ElectronGunScreen::new);
            event.register(ODMenuTypes.POSITRON_VACUUM_OUTPUT_PORT_MENU.get(), de.wutzuket.overdrive.menu.PositronVacuumOutputPortScreen::new);
            event.register(ODMenuTypes.HOLOGRAM_MENU.get(), HologramScreen::new);
            event.register(ODMenuTypes.ADVANCED_HOLOGRAM_MENU.get(), AdvancedHologramScreen::new);
            event.register(ODMenuTypes.ANNIHILATION_CONTROLLER_MENU.get(), de.wutzuket.overdrive.menu.AnnihilationControllerScreen::new);
            event.register(ODMenuTypes.COOLING_TOWER_MENU.get(), de.wutzuket.overdrive.menu.CoolingTowerScreen::new);
            event.register(ODMenuTypes.CRYO_DISTILLER_MENU.get(), de.wutzuket.overdrive.menu.CryoDistillerScreen::new);
            event.register(ODMenuTypes.COAL_GENERATOR_MENU.get(), de.wutzuket.overdrive.menu.CoalGeneratorScreen::new);
            event.register(ODMenuTypes.ANTIMATTER_TANK_MENU.get(), de.wutzuket.overdrive.menu.AntimatterTankScreen::new);
        }
        @SubscribeEvent
        public static void registerFluidTypeExtensions(RegisterClientExtensionsEvent event) {

            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() { return ResourceLocation.withDefaultNamespace("block/water_still"); }
                @Override
                public ResourceLocation getFlowingTexture() { return ResourceLocation.withDefaultNamespace("block/water_flow"); }
                @Override
                public int getTintColor() { return 0xFF88DDFF; }
            }, ODFluidTypes.CRYOFLUID_TYPE.get());
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() { return ResourceLocation.withDefaultNamespace("block/water_still"); }
                @Override
                public ResourceLocation getFlowingTexture() { return ResourceLocation.withDefaultNamespace("block/water_flow"); }
                @Override
                public int getTintColor() { return 0xFFCCCCCC; }
            }, ODFluidTypes.STEAM_TYPE.get());
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() { return ResourceLocation.withDefaultNamespace("block/water_still"); }
                @Override
                public ResourceLocation getFlowingTexture() { return ResourceLocation.withDefaultNamespace("block/water_flow"); }
                @Override
                public int getTintColor() { return 0xFF66FFFF; }
            }, ODFluidTypes.SUPERHEATED_CRYOFLUID_TYPE.get());
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() { return ResourceLocation.withDefaultNamespace("block/water_still"); }
                @Override
                public ResourceLocation getFlowingTexture() { return ResourceLocation.withDefaultNamespace("block/water_flow"); }
                @Override
                public int getTintColor() { return 0xFFE8ECF5; }
            }, ODFluidTypes.LIQUID_IRIDIUM_TYPE.get());
        }
    }
}
