package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.energy.CreativeEnergyCubeBlock;
import de.wutzuket.overdrive.block.scanner.scanner_block;
import de.wutzuket.overdrive.block.vacuum.EnergyInputPortBlock;
import de.wutzuket.overdrive.block.vacuum.PumpCasingBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumOutputPortBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumPipeBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Overdrive.MODID);

    public static final Supplier<Block> SCANNER_BLOCK = BLOCKS.register("scanner",
            () -> new scanner_block(BlockBehaviour.Properties.of().strength(2.0F)));

    public static final Supplier<Block> ALUMINUM_ORE = BLOCKS.register("aluminum_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Supplier<Block> DEEPSLATE_ALUMINUM_ORE = BLOCKS.register("deepslate_aluminum_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final Supplier<Block> ALUMINUM_BLOCK = BLOCKS.register("aluminum_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> CREATIVE_ENERGY_CUBE = BLOCKS.register("creative_energy_cube",
            () -> new CreativeEnergyCubeBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(state -> 15)));

    public static final Supplier<Block> IRIDIUM_ORE = BLOCKS.register("iridium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(4.0F, 5.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Supplier<Block> DEEPSLATE_IRIDIUM_ORE = BLOCKS.register("deepslate_iridium_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(5.5F, 5.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final Supplier<Block> IRIDIUM_BLOCK = BLOCKS.register("iridium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BLUE)
                    .strength(6.0F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> VACUUM_PUMP = BLOCKS.register("vacuum_pump",
            () -> new VacuumPumpBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 4.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> PUMP_CASING = BLOCKS.register("pump_casing",
            () -> new PumpCasingBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 4.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> ENERGY_INPUT_PORT = BLOCKS.register("energy_input_port",
            () -> new EnergyInputPortBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 4.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> VACUUM_OUTPUT_PORT = BLOCKS.register("vacuum_output_port",
            () -> new VacuumOutputPortBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 4.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> VACUUM_PIPE = BLOCKS.register("vacuum_pipe",
            () -> new VacuumPipeBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
