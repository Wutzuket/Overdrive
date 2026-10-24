package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.fluid.ODItemsFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Overdrive.MODID);

public static final Supplier<CreativeModeTab> OVERDRIVE_TAB = CREATIVE_TABS.register("overdrive", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.overdrive"))
            .icon(() -> new ItemStack(ODItems.WRENCH.get()))
            .displayItems((parameters, output) -> {
                output.accept(ODItems.WRENCH.get());
                output.accept(ODItems.ALUMINUM_ORE.get());
                output.accept(ODItems.DEEPSLATE_ALUMINUM_ORE.get());
                output.accept(ODItems.RAW_ALUMINUM.get());
                output.accept(ODItems.ALUMINUM_INGOT.get());
                output.accept(ODItems.ALUMINUM_NUGGET.get());
                output.accept(ODItems.ALUMINUM_BLOCK.get());
                output.accept(ODItems.CREATIVE_ENERGY_CUBE.get());
                output.accept(ODItems.IRIDIUM_ORE.get());
                output.accept(ODItems.DEEPSLATE_IRIDIUM_ORE.get());
                output.accept(ODItems.RAW_IRIDIUM.get());
                output.accept(ODItems.IRIDIUM_INGOT.get());
                output.accept(ODItems.IRIDIUM_BLOCK.get());
                output.accept(ODItems.TUNGSTEN_ORE.get());
                output.accept(ODItems.DEEPSLATE_TUNGSTEN_ORE.get());
                output.accept(ODItems.RAW_TUNGSTEN.get());
                output.accept(ODItems.TUNGSTEN_INGOT.get());
                output.accept(ODItems.TUNGSTEN_BLOCK.get());
                output.accept(ODItems.VACUUM_PUMP.get());
                output.accept(ODItems.PUMP_CASING.get());
                output.accept(ODItems.VACUUM_ENERGY_PORT.get());
                output.accept(ODItems.VACUUM_OUTPUT_PORT.get());
                output.accept(ODItems.VACUUM_PIPE.get());
                output.accept(ODItems.ACCELERATOR_CASING.get());
                output.accept(ODItems.ACCELERATOR_ENERGY_INPUT_PORT.get());
                output.accept(ODItems.ACCELERATOR_VACUUM_INPUT_PORT.get());
                output.accept(ODItems.PARTICLE_ACCELERATOR_CONTROLLER.get());
                output.accept(ODItems.IRIDIUM_TARGET.get());
                output.accept(ODItems.ACCELERATOR_OUTPUT.get());
                output.accept(ODItems.HOLOGRAM_BLOCK.get());
                output.accept(ODItems.ADVANCED_HOLOGRAM_BLOCK.get());
                output.accept(ODItemsFluid.CRYOFLUID_BUCKET.get());
                output.accept(ODItemsFluid.STEAM_BUCKET.get());
                output.accept(ODItemsFluid.SUPERHEATED_CRYOFLUID_BUCKET.get());
                output.accept(ODItemsFluid.LIQUID_IRIDIUM_BUCKET.get());
                output.accept(ODItems.AD_CASING.get());
                output.accept(ODItems.AD_CONTROLLER.get());
                output.accept(ODItems.AD_ENERGY_INPUT_PORT.get());
                output.accept(ODItems.AD_VACUUM_PORT.get());
                output.accept(ODItems.AD_INPUT_PORT.get());
                output.accept(ODItems.AD_OUTPUT_PORT.get());
                output.accept(ODItems.ELENA_CASING.get());
                output.accept(ODItems.ELENA_CONTROLLER.get());
                output.accept(ODItems.ELENA_ENERGY_INPUT_PORT.get());
                output.accept(ODItems.ELENA_VACUUM_PORT.get());
                output.accept(ODItems.ELENA_INPUT_PORT.get());
                output.accept(ODItems.ELENA_OUTPUT_PORT.get());
                output.accept(ODItems.TUNGSTEN_CASING.get());
                output.accept(ODItems.ELECTRON_GUN.get());
                output.accept(ODItems.TUNGSTEN_TARGET.get());
                output.accept(ODItems.POSITRON_VACUUM_OUTPUT_PORT.get());
                output.accept(ODItems.TUNGSTEN_MESH.get());
                output.accept(ODItems.ANNIHILATION_HEAVY_CASING.get());
                output.accept(ODItems.REACTOR_CORE_BLOCK.get());
                output.accept(ODItems.CORE_CONTAINMENT_GLASS.get());
                output.accept(ODItems.CORIUM_BLOCK.get());
                output.accept(ODItems.ANNIHILATION_CONTROLLER.get());
                output.accept(ODItems.ANNIHILATION_VACUUM_PORT.get());
                output.accept(ODItems.CELL_DOCKING_PORT.get());
                output.accept(ODItems.COOLANT_INPUT_PORT.get());
                output.accept(ODItems.STEAM_OUTPUT_PORT.get());
                output.accept(ODItems.ANNIHILATION_ENERGY_OUTPUT.get());
                output.accept(ODItems.COOLING_TOWER_CASING.get());
                output.accept(ODItems.COOLING_TOWER_CONTROLLER.get());
                output.accept(ODItems.COOLING_TOWER_INPUT_PORT.get());
                output.accept(ODItems.COOLING_TOWER_OUTPUT_PORT.get());
                output.accept(ODItems.SCANNER.get());
                output.accept(ODItems.CRYO_DISTILLER.get());
                output.accept(ODItems.CIRCUIT_PRESS.get());
                output.accept(ODItems.CIRCUIT_BOARD.get());
                output.accept(ODItems.COAL_GENERATOR.get());
                output.accept(ODItems.SIMPLE_FRAME.get());
                output.accept(ODItems.ANTIMATTER_PIPE_BASIC.get());
                output.accept(ODItems.ANTIMATTER_PIPE_ADVANCED.get());
                output.accept(ODItems.ANTIMATTER_PIPE_ELITE.get());
                output.accept(ODItems.ANTIMATTER_PIPE_ULTIMATE.get());
                output.accept(ODItems.ANTIMATTER_TANK.get());
                output.accept(ODItems.EMPTY_ANTIMATTER_CELL.get());
                // Die Cells werden automatisch fuer jeden Antimatter-Typ erzeugt
                for (Item cell : ODItems.getAntimatterCells()) {
                    output.accept(cell);
                }
                // Fuer jeden Antimatter-Typ gibt es auch einen kreativen Tank
                for (Item creativeTank : ODItems.getCreativeAntimatterTanks()) {
                    output.accept(creativeTank);
                }
            })
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
