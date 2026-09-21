package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.fluid.ODItemsFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Overdrive.MODID);

    public static final Supplier<CreativeModeTab> OVERDRIVE_TAB = CREATIVE_TABS.register("overdrive", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.overdrive"))
            .icon(() -> new ItemStack(ODItems.SCANNER_ITEM.get()))
            .displayItems((parameters, output) -> {
                output.accept(ODItems.SCANNER_ITEM.get());
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
                output.accept(ODItems.WOLFRAM_ORE.get());
                output.accept(ODItems.DEEPSLATE_WOLFRAM_ORE.get());
                output.accept(ODItems.RAW_WOLFRAM.get());
                output.accept(ODItems.WOLFRAM_INGOT.get());
                output.accept(ODItems.WOLFRAM_BLOCK.get());
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
                output.accept(ODItemsFluid.ANTIPROTONS_BUCKET.get());
                output.accept(ODItemsFluid.DECELERATED_ANTIPROTONS_BUCKET.get());
                output.accept(ODItemsFluid.COOLED_ANTIPROTONS_BUCKET.get());
                output.accept(ODItemsFluid.POSITRONS_BUCKET.get());
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
                output.accept(ODItems.WOLFRAM_CASING.get());
                output.accept(ODItems.ELECTRON_GUN.get());
                output.accept(ODItems.WOLFRAM_TARGET.get());
                output.accept(ODItems.POSITRON_VACUUM_OUTPUT_PORT.get());
                output.accept(ODItems.WOLFRAM_MESH.get());
            })
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
