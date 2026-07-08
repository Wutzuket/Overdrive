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

    public static final Supplier<Item> IRIDIUM_ORE = ITEMS.register("iridium_ore",
            () -> new BlockItem(ODBlocks.IRIDIUM_ORE.get(), new Item.Properties()));

    public static final Supplier<Item> DEEPSLATE_IRIDIUM_ORE = ITEMS.register("deepslate_iridium_ore",
            () -> new BlockItem(ODBlocks.DEEPSLATE_IRIDIUM_ORE.get(), new Item.Properties()));

    public static final Supplier<Item> IRIDIUM_BLOCK = ITEMS.register("iridium_block",
            () -> new BlockItem(ODBlocks.IRIDIUM_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> RAW_IRIDIUM = ITEMS.register("raw_iridium",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> IRIDIUM_INGOT = ITEMS.register("iridium_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> VACUUM_PUMP = ITEMS.register("vacuum_pump",
            () -> new BlockItem(ODBlocks.VACUUM_PUMP.get(), new Item.Properties()));

    public static final Supplier<Item> PUMP_CASING = ITEMS.register("pump_casing",
            () -> new BlockItem(ODBlocks.PUMP_CASING.get(), new Item.Properties()));

    public static final Supplier<Item> ENERGY_INPUT_PORT = ITEMS.register("energy_input_port",
            () -> new BlockItem(ODBlocks.ENERGY_INPUT_PORT.get(), new Item.Properties()));

    public static final Supplier<Item> VACUUM_OUTPUT_PORT = ITEMS.register("vacuum_output_port",
            () -> new BlockItem(ODBlocks.VACUUM_OUTPUT_PORT.get(), new Item.Properties()));

    public static final Supplier<Item> VACUUM_PIPE = ITEMS.register("vacuum_pipe",
            () -> new BlockItem(ODBlocks.VACUUM_PIPE.get(), new Item.Properties()));

    public static final Supplier<Item> ACCELERATOR_CASING = ITEMS.register("accelerator_casing",
            () -> new BlockItem(ODBlocks.ACCELERATOR_CASING.get(), new Item.Properties()));

    public static final Supplier<Item> ACCELERATOR_ENERGY_INPUT_PORT = ITEMS.register("accelerator_energy_input_port",
            () -> new BlockItem(ODBlocks.ACCELERATOR_ENERGY_INPUT_PORT.get(), new Item.Properties()));

    public static final Supplier<Item> ACCELERATOR_VACUUM_INPUT_PORT = ITEMS.register("accelerator_vacuum_input_port",
            () -> new BlockItem(ODBlocks.ACCELERATOR_VACUUM_INPUT_PORT.get(), new Item.Properties()));

    public static final Supplier<Item> PARTICLE_ACCELERATOR_CONTROLLER = ITEMS.register("particle_accelerator_controller",
            () -> new BlockItem(ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get(), new Item.Properties()));

    public static final Supplier<Item> IRIDIUM_TARGET = ITEMS.register("iridium_target",
            () -> new BlockItem(ODBlocks.IRIDIUM_TARGET.get(), new Item.Properties()));

    public static final Supplier<Item> ACCELERATOR_OUTPUT = ITEMS.register("accelerator_output",
            () -> new BlockItem(ODBlocks.ACCELERATOR_OUTPUT.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
