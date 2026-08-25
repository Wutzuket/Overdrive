package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.accelerator.AcceleratorCasing;
import de.wutzuket.overdrive.block.accelerator.AcceleratorEnergyInputPortBlock;
import de.wutzuket.overdrive.block.accelerator.AcceleratorVacuumInputPortBlock;
import de.wutzuket.overdrive.block.accelerator.AcceleratorOutputBlock;
import de.wutzuket.overdrive.block.accelerator.IridiumTargetBlock;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerBlock;
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

    public static final Supplier<Block> WOLFRAM_ORE = BLOCKS.register("wolfram_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(4.5F, 5.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Supplier<Block> DEEPSLATE_WOLFRAM_ORE = BLOCKS.register("deepslate_wolfram_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(6.0F, 5.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final Supplier<Block> WOLFRAM_BLOCK = BLOCKS.register("wolfram_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(7.0F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> VACUUM_PUMP = BLOCKS.register("vacuum_pump",
            () -> new VacuumPumpBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 4.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> PUMP_CASING = BLOCKS.register("pump_casing",
            () -> new PumpCasingBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 4.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final Supplier<Block> VACUUM_ENERGY_PORT = BLOCKS.register("vacuum_energy_port",
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

    public static final Supplier<Block> ACCELERATOR_CASING = BLOCKS.register("accelerator_casing",
            () -> new AcceleratorCasing(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final Supplier<Block> ACCELERATOR_ENERGY_INPUT_PORT = BLOCKS.register("accelerator_energy_input_port",
            () -> new AcceleratorEnergyInputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> ACCELERATOR_VACUUM_INPUT_PORT = BLOCKS.register("accelerator_vacuum_input_port",
            () -> new AcceleratorVacuumInputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> PARTICLE_ACCELERATOR_CONTROLLER = BLOCKS.register("particle_accelerator_controller",
            () -> new ParticleAcceleratorControllerBlock(BlockBehaviour.Properties.of()
                    .strength(5.0F, 8.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> IRIDIUM_TARGET = BLOCKS.register("iridium_target",
            () -> new IridiumTargetBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F)
                    .sound(SoundType.METAL)));

    public static final Supplier<Block> ACCELERATOR_OUTPUT = BLOCKS.register("accelerator_output",
            () -> new AcceleratorOutputBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F)
                    .sound(SoundType.METAL)));

    // AD Ring
    public static final Supplier<Block> AD_CASING = BLOCKS.register("ad_casing",
            () -> new de.wutzuket.overdrive.block.ad.ADCasingBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));

    public static final Supplier<Block> AD_CONTROLLER = BLOCKS.register("ad_controller",
            () -> new de.wutzuket.overdrive.block.ad.ADControllerBlock(BlockBehaviour.Properties.of()
                    .strength(5.0F, 8.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> AD_ENERGY_INPUT_PORT = BLOCKS.register("ad_energy_input_port",
            () -> new de.wutzuket.overdrive.block.ad.ADEnergyInputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> AD_VACUUM_PORT = BLOCKS.register("ad_vacuum_port",
            () -> new de.wutzuket.overdrive.block.ad.ADVacuumPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> AD_INPUT_PORT = BLOCKS.register("ad_input_port",
            () -> new de.wutzuket.overdrive.block.ad.ADInputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> AD_OUTPUT_PORT = BLOCKS.register("ad_output_port",
            () -> new de.wutzuket.overdrive.block.ad.ADOutputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    // ELENA Ring
    public static final Supplier<Block> ELENA_CASING = BLOCKS.register("elena_casing",
            () -> new de.wutzuket.overdrive.block.elena.ELENACasingBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));

    public static final Supplier<Block> ELENA_CONTROLLER = BLOCKS.register("elena_controller",
            () -> new de.wutzuket.overdrive.block.elena.ELENAControllerBlock(BlockBehaviour.Properties.of()
                    .strength(5.0F, 8.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> ELENA_ENERGY_INPUT_PORT = BLOCKS.register("elena_energy_input_port",
            () -> new de.wutzuket.overdrive.block.elena.ELENAEnergyInputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> ELENA_VACUUM_PORT = BLOCKS.register("elena_vacuum_port",
            () -> new de.wutzuket.overdrive.block.elena.ELENAVacuumPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> ELENA_INPUT_PORT = BLOCKS.register("elena_input_port",
            () -> new de.wutzuket.overdrive.block.elena.ELENAInputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> ELENA_OUTPUT_PORT = BLOCKS.register("elena_output_port",
            () -> new de.wutzuket.overdrive.block.elena.ELENAOutputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    // Wolfram Target
    public static final Supplier<Block> WOLFRAM_CASING = BLOCKS.register("wolfram_casing",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));

    public static final Supplier<Block> ELECTRON_GUN = BLOCKS.register("electron_gun",
            () -> new de.wutzuket.overdrive.block.wolfram.ElectronGunBlock(BlockBehaviour.Properties.of()
                    .strength(5.0F, 8.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> WOLFRAM_TARGET = BLOCKS.register("wolfram_target",
            () -> new de.wutzuket.overdrive.block.wolfram.WolframTargetBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> POSITRON_VACUUM_OUTPUT_PORT = BLOCKS.register("positron_vacuum_output_port",
            () -> new de.wutzuket.overdrive.block.wolfram.PositronVacuumOutputPortBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F).sound(SoundType.METAL)));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
