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

    // Wolfram
    public static final Supplier<Item> WOLFRAM_ORE = ITEMS.register("wolfram_ore",
            () -> new BlockItem(ODBlocks.WOLFRAM_ORE.get(), new Item.Properties()));

    public static final Supplier<Item> DEEPSLATE_WOLFRAM_ORE = ITEMS.register("deepslate_wolfram_ore",
            () -> new BlockItem(ODBlocks.DEEPSLATE_WOLFRAM_ORE.get(), new Item.Properties()));

    public static final Supplier<Item> WOLFRAM_BLOCK = ITEMS.register("wolfram_block",
            () -> new BlockItem(ODBlocks.WOLFRAM_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> RAW_WOLFRAM = ITEMS.register("raw_wolfram",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> WOLFRAM_INGOT = ITEMS.register("wolfram_ingot",
            () -> new Item(new Item.Properties()));

    public static final Supplier<Item> VACUUM_PUMP = ITEMS.register("vacuum_pump",
            () -> new BlockItem(ODBlocks.VACUUM_PUMP.get(), new Item.Properties()));

    public static final Supplier<Item> PUMP_CASING = ITEMS.register("pump_casing",
            () -> new BlockItem(ODBlocks.PUMP_CASING.get(), new Item.Properties()));

    public static final Supplier<Item> VACUUM_ENERGY_PORT = ITEMS.register("vacuum_energy_port",
            () -> new BlockItem(ODBlocks.VACUUM_ENERGY_PORT.get(), new Item.Properties()));

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

    // AD Ring
    public static final Supplier<Item> AD_CASING = ITEMS.register("ad_casing",
            () -> new BlockItem(ODBlocks.AD_CASING.get(), new Item.Properties()));
    public static final Supplier<Item> AD_CONTROLLER = ITEMS.register("ad_controller",
            () -> new BlockItem(ODBlocks.AD_CONTROLLER.get(), new Item.Properties()));
    public static final Supplier<Item> AD_ENERGY_INPUT_PORT = ITEMS.register("ad_energy_input_port",
            () -> new BlockItem(ODBlocks.AD_ENERGY_INPUT_PORT.get(), new Item.Properties()));
    public static final Supplier<Item> AD_VACUUM_PORT = ITEMS.register("ad_vacuum_port",
            () -> new BlockItem(ODBlocks.AD_VACUUM_PORT.get(), new Item.Properties()));
    public static final Supplier<Item> AD_INPUT_PORT = ITEMS.register("ad_input_port",
            () -> new BlockItem(ODBlocks.AD_INPUT_PORT.get(), new Item.Properties()));
    public static final Supplier<Item> AD_OUTPUT_PORT = ITEMS.register("ad_output_port",
            () -> new BlockItem(ODBlocks.AD_OUTPUT_PORT.get(), new Item.Properties()));

    // ELENA Ring
    public static final Supplier<Item> ELENA_CASING = ITEMS.register("elena_casing",
            () -> new BlockItem(ODBlocks.ELENA_CASING.get(), new Item.Properties()));
    public static final Supplier<Item> ELENA_CONTROLLER = ITEMS.register("elena_controller",
            () -> new BlockItem(ODBlocks.ELENA_CONTROLLER.get(), new Item.Properties()));
    public static final Supplier<Item> ELENA_ENERGY_INPUT_PORT = ITEMS.register("elena_energy_input_port",
            () -> new BlockItem(ODBlocks.ELENA_ENERGY_INPUT_PORT.get(), new Item.Properties()));
    public static final Supplier<Item> ELENA_VACUUM_PORT = ITEMS.register("elena_vacuum_port",
            () -> new BlockItem(ODBlocks.ELENA_VACUUM_PORT.get(), new Item.Properties()));
    public static final Supplier<Item> ELENA_INPUT_PORT = ITEMS.register("elena_input_port",
            () -> new BlockItem(ODBlocks.ELENA_INPUT_PORT.get(), new Item.Properties()));
    public static final Supplier<Item> ELENA_OUTPUT_PORT = ITEMS.register("elena_output_port",
            () -> new BlockItem(ODBlocks.ELENA_OUTPUT_PORT.get(), new Item.Properties()));

    // Wolfram Target
    public static final Supplier<Item> WOLFRAM_CASING = ITEMS.register("wolfram_casing",
            () -> new BlockItem(ODBlocks.WOLFRAM_CASING.get(), new Item.Properties()));
    public static final Supplier<Item> ELECTRON_GUN = ITEMS.register("electron_gun",
            () -> new BlockItem(ODBlocks.ELECTRON_GUN.get(), new Item.Properties()));
    public static final Supplier<Item> WOLFRAM_TARGET = ITEMS.register("wolfram_target",
            () -> new BlockItem(ODBlocks.WOLFRAM_TARGET.get(), new Item.Properties()));
    public static final Supplier<Item> POSITRON_VACUUM_OUTPUT_PORT = ITEMS.register("positron_vacuum_output_port",
            () -> new BlockItem(ODBlocks.POSITRON_VACUUM_OUTPUT_PORT.get(), new Item.Properties()));

    // Items
    public static final Supplier<Item> WOLFRAM_MESH = ITEMS.register("wolfram_mesh",
            () -> new Item(new Item.Properties().stacksTo(1).durability(100)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
