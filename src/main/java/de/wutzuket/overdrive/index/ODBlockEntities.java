package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.accelerator.AcceleratorEnergyInputPortEntity;
import de.wutzuket.overdrive.block.accelerator.AcceleratorOutputEntity;
import de.wutzuket.overdrive.block.accelerator.AcceleratorVacuumInputPortEntity;
import de.wutzuket.overdrive.block.accelerator.IridiumTargetEntity;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import de.wutzuket.overdrive.block.energy.CreativeEnergyCubeBlockEntity;
import de.wutzuket.overdrive.block.advanced_hologram.AdvancedHologramBlockEntity;
import de.wutzuket.overdrive.block.hologram.HologramBlockEntity;
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

public static final java.util.function.Supplier<BlockEntityType<HologramBlockEntity>> HOLOGRAM_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "hologram_block",
                    () -> BlockEntityType.Builder.of(HologramBlockEntity::new, ODBlocks.HOLOGRAM_BLOCK.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<AdvancedHologramBlockEntity>> ADVANCED_HOLOGRAM_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "advanced_hologram_block",
                    () -> BlockEntityType.Builder.of(AdvancedHologramBlockEntity::new, ODBlocks.ADVANCED_HOLOGRAM_BLOCK.get()).build(null)
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
                    "vacuum_energy_port",
                    () -> BlockEntityType.Builder.of(EnergyInputPortBlockEntity::new, ODBlocks.VACUUM_ENERGY_PORT.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<VacuumOutputPortBlockEntity>> VACUUM_OUTPUT_PORT_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "vacuum_output_port.json",
                    () -> BlockEntityType.Builder.of(VacuumOutputPortBlockEntity::new, ODBlocks.VACUUM_OUTPUT_PORT.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<VacuumPipeBlockEntity>> VACUUM_PIPE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "vacuum_pipe",
                    () -> BlockEntityType.Builder.of(VacuumPipeBlockEntity::new, ODBlocks.VACUUM_PIPE.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<ParticleAcceleratorControllerEntity>> PARTICLE_ACCELERATOR_CONTROLLER =
            BLOCK_ENTITY_TYPES.register(
                    "particle_accelerator_controller",
                    () -> BlockEntityType.Builder.of(ParticleAcceleratorControllerEntity::new, ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<AcceleratorEnergyInputPortEntity>> ACCELERATOR_ENERGY_INPUT_PORT =
            BLOCK_ENTITY_TYPES.register(
                    "accelerator_energy_input_port",
                    () -> BlockEntityType.Builder.of(AcceleratorEnergyInputPortEntity::new, ODBlocks.ACCELERATOR_ENERGY_INPUT_PORT.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<AcceleratorVacuumInputPortEntity>> ACCELERATOR_VACUUM_INPUT_PORT =
            BLOCK_ENTITY_TYPES.register(
                    "accelerator_vacuum_input_port",
                    () -> BlockEntityType.Builder.of(AcceleratorVacuumInputPortEntity::new, ODBlocks.ACCELERATOR_VACUUM_INPUT_PORT.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<IridiumTargetEntity>> IRIDIUM_TARGET =
            BLOCK_ENTITY_TYPES.register(
                    "iridium_target",
                    () -> BlockEntityType.Builder.of(IridiumTargetEntity::new, ODBlocks.IRIDIUM_TARGET.get()).build(null)
            );

    public static final java.util.function.Supplier<BlockEntityType<AcceleratorOutputEntity>> ACCELERATOR_OUTPUT =
            BLOCK_ENTITY_TYPES.register(
                    "accelerator_output",
                    () -> BlockEntityType.Builder.of(AcceleratorOutputEntity::new, ODBlocks.ACCELERATOR_OUTPUT.get()).build(null)
            );

    // AD Ring
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.ad.ADControllerEntity>> AD_CONTROLLER =
            BLOCK_ENTITY_TYPES.register("ad_controller",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.ad.ADControllerEntity::new, ODBlocks.AD_CONTROLLER.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.ad.ADEnergyInputPortEntity>> AD_ENERGY_INPUT_PORT =
            BLOCK_ENTITY_TYPES.register("ad_energy_input_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.ad.ADEnergyInputPortEntity::new, ODBlocks.AD_ENERGY_INPUT_PORT.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.ad.ADVacuumPortEntity>> AD_VACUUM_PORT =
            BLOCK_ENTITY_TYPES.register("ad_vacuum_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.ad.ADVacuumPortEntity::new, ODBlocks.AD_VACUUM_PORT.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.ad.ADInputPortEntity>> AD_INPUT_PORT =
            BLOCK_ENTITY_TYPES.register("ad_input_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.ad.ADInputPortEntity::new, ODBlocks.AD_INPUT_PORT.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.ad.ADOutputPortEntity>> AD_OUTPUT_PORT =
            BLOCK_ENTITY_TYPES.register("ad_output_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.ad.ADOutputPortEntity::new, ODBlocks.AD_OUTPUT_PORT.get()).build(null));

    // ELENA Ring
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.elena.ELENAControllerEntity>> ELENA_CONTROLLER =
            BLOCK_ENTITY_TYPES.register("elena_controller",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.elena.ELENAControllerEntity::new, ODBlocks.ELENA_CONTROLLER.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.elena.ELENAEnergyInputPortEntity>> ELENA_ENERGY_INPUT_PORT =
            BLOCK_ENTITY_TYPES.register("elena_energy_input_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.elena.ELENAEnergyInputPortEntity::new, ODBlocks.ELENA_ENERGY_INPUT_PORT.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.elena.ELENAVacuumPortEntity>> ELENA_VACUUM_PORT =
            BLOCK_ENTITY_TYPES.register("elena_vacuum_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.elena.ELENAVacuumPortEntity::new, ODBlocks.ELENA_VACUUM_PORT.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.elena.ELENAInputPortEntity>> ELENA_INPUT_PORT =
            BLOCK_ENTITY_TYPES.register("elena_input_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.elena.ELENAInputPortEntity::new, ODBlocks.ELENA_INPUT_PORT.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.elena.ELENAOutputPortEntity>> ELENA_OUTPUT_PORT =
            BLOCK_ENTITY_TYPES.register("elena_output_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.elena.ELENAOutputPortEntity::new, ODBlocks.ELENA_OUTPUT_PORT.get()).build(null));

    // Tungsten Target
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.tungsten.ElectronGunEntity>> ELECTRON_GUN =
            BLOCK_ENTITY_TYPES.register("electron_gun",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.tungsten.ElectronGunEntity::new, ODBlocks.ELECTRON_GUN.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.tungsten.TungstenTargetEntity>> TUNGSTEN_TARGET =
            BLOCK_ENTITY_TYPES.register("tungsten_target",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.tungsten.TungstenTargetEntity::new, ODBlocks.TUNGSTEN_TARGET.get()).build(null));

    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.tungsten.PositronVacuumOutputPortEntity>> POSITRON_VACUUM_OUTPUT_PORT =
            BLOCK_ENTITY_TYPES.register("positron_vacuum_output_port",
                    () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.tungsten.PositronVacuumOutputPortEntity::new, ODBlocks.POSITRON_VACUUM_OUTPUT_PORT.get()).build(null));

    // Step 4 - Annihilation
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity>> ANNIHILATION_CONTROLLER =
            BLOCK_ENTITY_TYPES.register("annihilation_controller", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity::new, ODBlocks.ANNIHILATION_CONTROLLER.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.annihilation.AnnihilationVacuumPortEntity>> ANNIHILATION_VACUUM_PORT =
            BLOCK_ENTITY_TYPES.register("annihilation_vacuum_port", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.annihilation.AnnihilationVacuumPortEntity::new, ODBlocks.ANNIHILATION_VACUUM_PORT.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity>> ANNIHILATION_CELL_PORT =
            BLOCK_ENTITY_TYPES.register("cell_docking_port", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity::new, ODBlocks.CELL_DOCKING_PORT.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.annihilation.CoolantInputPortEntity>> ANNIHILATION_COOLANT_INPUT =
            BLOCK_ENTITY_TYPES.register("coolant_input_port", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.annihilation.CoolantInputPortEntity::new, ODBlocks.COOLANT_INPUT_PORT.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.annihilation.SteamOutputPortEntity>> ANNIHILATION_STEAM_OUTPUT =
            BLOCK_ENTITY_TYPES.register("steam_output_port", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.annihilation.SteamOutputPortEntity::new, ODBlocks.STEAM_OUTPUT_PORT.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.annihilation.EnergyOutputPortEntity>> ANNIHILATION_ENERGY_OUTPUT =
            BLOCK_ENTITY_TYPES.register("annihilation_energy_output", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.annihilation.EnergyOutputPortEntity::new, ODBlocks.ANNIHILATION_ENERGY_OUTPUT.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity>> COOLING_TOWER_CONTROLLER =
            BLOCK_ENTITY_TYPES.register("cooling_tower_controller", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity::new, ODBlocks.COOLING_TOWER_CONTROLLER.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.coolingtower.CoolingTowerInputPortEntity>> COOLING_TOWER_INPUT_PORT =
            BLOCK_ENTITY_TYPES.register("cooling_tower_input_port", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.coolingtower.CoolingTowerInputPortEntity::new, ODBlocks.COOLING_TOWER_INPUT_PORT.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.coolingtower.CoolingTowerOutputPortEntity>> COOLING_TOWER_OUTPUT_PORT =
            BLOCK_ENTITY_TYPES.register("cooling_tower_output_port", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.coolingtower.CoolingTowerOutputPortEntity::new, ODBlocks.COOLING_TOWER_OUTPUT_PORT.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.scanner.scanner_blockentity>> SCANNER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("scanner", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.scanner.scanner_blockentity::new, ODBlocks.SCANNER.get()).build(null));
    public static final java.util.function.Supplier<BlockEntityType<de.wutzuket.overdrive.block.distiller.CryoDistillerEntity>> CRYO_DISTILLER =
            BLOCK_ENTITY_TYPES.register("cryo_distiller", () -> BlockEntityType.Builder.of(de.wutzuket.overdrive.block.distiller.CryoDistillerEntity::new, ODBlocks.CRYO_DISTILLER.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
