package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.energy.CreativeEnergyCubeBlockEntity;
import de.wutzuket.overdrive.block.scanner.scanner_blockentity;
import de.wutzuket.overdrive.block.vacuum.EnergyInputPortBlockEntity;
import de.wutzuket.overdrive.block.vacuum.VacuumOutputPortBlockEntity;
import de.wutzuket.overdrive.block.vacuum.VacuumPipeBlockEntity;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ODBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Overdrive.MODID);

    public static final java.util.function.Supplier<BlockEntityType<scanner_blockentity>> SCANNER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "scanner",
                    () -> BlockEntityType.Builder.of(scanner_blockentity::new, ODBlocks.SCANNER_BLOCK.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<CreativeEnergyCubeBlockEntity>> CREATIVE_ENERGY_CUBE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "creative_energy_cube",
                    () -> BlockEntityType.Builder.of(CreativeEnergyCubeBlockEntity::new, ODBlocks.CREATIVE_ENERGY_CUBE.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<VacuumPumpBlockEntity>> VACUUM_PUMP_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "vacuum_pump",
                    () -> BlockEntityType.Builder.of(VacuumPumpBlockEntity::new, ODBlocks.VACUUM_PUMP.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<EnergyInputPortBlockEntity>> ENERGY_INPUT_PORT_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "energy_input_port",
                    () -> BlockEntityType.Builder.of(EnergyInputPortBlockEntity::new, ODBlocks.ENERGY_INPUT_PORT.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<VacuumOutputPortBlockEntity>> VACUUM_OUTPUT_PORT_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "vacuum_output_port",
                    () -> BlockEntityType.Builder.of(VacuumOutputPortBlockEntity::new, ODBlocks.VACUUM_OUTPUT_PORT.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<VacuumPipeBlockEntity>> VACUUM_PIPE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "vacuum_pipe",
                    () -> BlockEntityType.Builder.of(VacuumPipeBlockEntity::new, ODBlocks.VACUUM_PIPE.get()).build(null)
            );

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
