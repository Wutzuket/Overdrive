package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Overdrive.MODID);

    public static final Supplier<Item> SCANNER_ITEM = ITEMS.register("scanner",
            () -> new BlockItem(ODBlocks.SCANNER_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> ALUMINUM_ORE = ITEMS.register("aluminum_ore",
            () -> new BlockItem(ODBlocks.ALUMINUM_ORE.get(), new Item.Properties()));

    public static final Supplier<Item> DEEPSLATE_ALUMINUM_ORE = ITEMS.register("deepslate_aluminum_ore",
            () -> new BlockItem(ODBlocks.DEEPSLATE_ALUMINUM_ORE.get(), new Item.Properties()));

    public static final Supplier<Item> ALUMINUM_BLOCK = ITEMS.register("aluminum_block",
            () -> new BlockItem(ODBlocks.ALUMINUM_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> RAW_ALUMINUM = ITEMS.register("raw_aluminum",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> ALUMINUM_INGOT = ITEMS.register("aluminum_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> ALUMINUM_NUGGET = ITEMS.register("aluminum_nugget",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> CREATIVE_ENERGY_CUBE = ITEMS.register("creative_energy_cube",
            () -> new BlockItem(ODBlocks.CREATIVE_ENERGY_CUBE.get(), new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
