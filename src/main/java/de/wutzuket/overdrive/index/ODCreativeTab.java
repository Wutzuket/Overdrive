package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
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
                output.accept(ODItems.ALUMINUM_ORE.get());
                output.accept(ODItems.DEEPSLATE_ALUMINUM_ORE.get());
                output.accept(ODItems.RAW_ALUMINUM.get());
                output.accept(ODItems.ALUMINUM_INGOT.get());
                output.accept(ODItems.ALUMINUM_NUGGET.get());
                output.accept(ODItems.ALUMINUM_BLOCK.get());
                output.accept(ODItems.CREATIVE_ENERGY_CUBE.get());
            })
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
