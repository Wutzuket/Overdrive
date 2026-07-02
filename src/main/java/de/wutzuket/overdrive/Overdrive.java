package de.wutzuket.overdrive;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import de.wutzuket.overdrive.block.scanner.scanner_renderer;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe_manager;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODCreativeTab;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.worldgen.ODBiomeModifiers;
import de.wutzuket.overdrive.worldgen.ODWorldGen;

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
        ODWorldGen.register(modEventBus);
        ODBiomeModifiers.register(modEventBus);

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
        });
    }

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(scanner_recipe_manager.INSTANCE);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ODBlockEntities.SCANNER_BLOCK_ENTITY.get(), scanner_renderer::new);
        }
    }
}
