package de.wutzuket.overdrive.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ODConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_ALUMINUM_GENERATION = BUILDER
            .comment(
                    "Enable Aluminum Ore world generation.",
                    "Set to false to disable all aluminum ore generation (for modpack compatibility).")
            .define("enableAluminumGeneration", true);

    static {
        BUILDER.push("circuit_press");
    }

    public static final ModConfigSpec.IntValue CIRCUIT_PRESS_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage of the Circuit Press (FE).")
            .defineInRange("maxEnergy", 1_000_000, 1, 1_000_000_000);

    public static final ModConfigSpec.IntValue CIRCUIT_PRESS_ENERGY_PER_TICK = BUILDER
            .comment("Energy consumed by the Circuit Press while processing (FE/t).")
            .defineInRange("energyPerTick", 100, 0, 1_000_000);

    public static final ModConfigSpec.IntValue CIRCUIT_PRESS_PROCESS_TIME = BUILDER
            .comment("Default processing time used by Circuit Press recipes (ticks).")
            .defineInRange("processTime", 100, 1, 1_000_000);

    public static final ModConfigSpec.IntValue CIRCUIT_PRESS_ITEM_HANDLER_CAPACITY = BUILDER
            .comment("Maximum number of items stored in each Circuit Press slot.")
            .defineInRange("slotCapacity", 64, 1, 64);

    static {
        BUILDER.pop();
    }

    // --- Annihilation Reactor ---
    static {
        BUILDER.push("annihilation_reactor");
    }

    public static final ModConfigSpec.DoubleValue MIN_BURN_RATE = BUILDER
            .comment("Minimum burn rate of the Annihilation Reactor (no upper limit).")
            .defineInRange("minBurnRate", 0.1, 0.01, 100.0);

    public static final ModConfigSpec.DoubleValue HEATING_FACTOR = BUILDER
            .comment(
                    "Heating per tick in K/t = heatingFactor * sqrt(burnRate).",
                    "Cooling only depends on the temperature overshoot, so the equilibrium temperature",
                    "follows a square-root curve: T ~ boilingTemp + heatingFactor * 100 * sqrt(burnRate) / K,",
                    "with K = mbPer100K * coolingPerMb of the coolant (water: K=10 -> ~2000 C at burn rate 100).")
            .defineInRange("heatingFactor", 19.0, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue FUEL_DIVISOR = BUILDER
            .comment(
                    "Fuel need per tick and side = recipeAmount * burnRate / fuelDivisor.",
                    "Default: 10 mB * 100 / 20 = 50 mB/t Antiprotons + 50 mB/t Positrons at burn rate 100.")
            .defineInRange("fuelDivisor", 20.0, 0.1, 100000.0);

    public static final ModConfigSpec.EnumValue<ReactorFailureMode> REACTOR_FAILURE_MODE = BUILDER
            .comment(
                    "What happens when the reactor fails (vacuum loss, overheating, etc.):",
                    "MELTDOWN: Core turns to corium, creates a pit, structure mostly stays.",
                    "EXPLOSION: Large explosion destroys the whole structure.",
                    "NONE: Reactor just shuts down safely, no damage.")
            .defineEnum("failureMode", ReactorFailureMode.MELTDOWN);

    public static final ModConfigSpec.BooleanValue DESTROY_STRUCTURE_ON_MELTDOWN = BUILDER
            .comment("If true and failureMode is MELTDOWN, also destroys the torus structure (keeps bottom layer).")
            .define("destroyStructureOnMeltdown", false);

    static {
        BUILDER.pop();
    }

    // --- Particle Accelerator ---
    static {
        BUILDER.push("particle_accelerator");
    }

    public static final ModConfigSpec.BooleanValue ACCELERATOR_EXPLODE_ON_FAILURE = BUILDER
            .comment("If true, accelerator explodes and partially destroys its ring on vacuum loss or other failure.")
            .define("explodeOnFailure", true);

    public static final ModConfigSpec.DoubleValue ACCELERATOR_BOTTOM_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for bottom layer (y-1) blocks to be destroyed on failure.")
            .defineInRange("bottomLayerDestroyChance", 0.15, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ACCELERATOR_MIDDLE_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for middle layer (y) blocks to be destroyed on failure.")
            .defineInRange("middleLayerDestroyChance", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ACCELERATOR_TOP_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for top layer (y+1) blocks to be destroyed on failure.")
            .defineInRange("topLayerDestroyChance", 0.85, 0.0, 1.0);

    static {
        BUILDER.pop();
    }

    // --- AD Ring ---
    static {
        BUILDER.push("ad_ring");
    }

    public static final ModConfigSpec.BooleanValue AD_EXPLODE_ON_FAILURE = BUILDER
            .comment("If true, AD ring explodes and partially destroys its structure on vacuum loss or other failure.")
            .define("explodeOnFailure", true);

    public static final ModConfigSpec.DoubleValue AD_BOTTOM_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for bottom layer blocks to be destroyed on failure.")
            .defineInRange("bottomLayerDestroyChance", 0.15, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue AD_MIDDLE_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for middle layer blocks to be destroyed on failure.")
            .defineInRange("middleLayerDestroyChance", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue AD_TOP_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for top layer blocks to be destroyed on failure.")
            .defineInRange("topLayerDestroyChance", 0.85, 0.0, 1.0);

    static {
        BUILDER.pop();
    }

    // --- ELENA Ring ---
    static {
        BUILDER.push("elena_ring");
    }

    public static final ModConfigSpec.BooleanValue ELENA_EXPLODE_ON_FAILURE = BUILDER
            .comment("If true, ELENA ring explodes and partially destroys its structure on vacuum loss or other failure.")
            .define("explodeOnFailure", true);

    public static final ModConfigSpec.DoubleValue ELENA_BOTTOM_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for bottom layer blocks to be destroyed on failure.")
            .defineInRange("bottomLayerDestroyChance", 0.15, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ELENA_MIDDLE_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for middle layer blocks to be destroyed on failure.")
            .defineInRange("middleLayerDestroyChance", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue ELENA_TOP_DESTROY_CHANCE = BUILDER
            .comment("Chance (0-1) for top layer blocks to be destroyed on failure.")
            .defineInRange("topLayerDestroyChance", 0.85, 0.0, 1.0);

    static {
        BUILDER.pop();
    }

    // --- Coal Generator ---
    static {
        BUILDER.push("coal_generator");
    }

    public static final ModConfigSpec.IntValue COAL_GENERATOR_MAX_ENERGY = BUILDER
            .comment("Maximum energy the Coal Generator can store (FE).")
            .defineInRange("maxEnergy", 400_000, 1, 100_000_000);

    public static final ModConfigSpec.IntValue COAL_GENERATOR_FE_PER_TICK = BUILDER
            .comment("Energy output per tick while burning fuel (FE/t).")
            .defineInRange("fePerTick", 20, 1, 100_000);

    public static final ModConfigSpec.BooleanValue COAL_GENERATOR_AUTO_PUSH = BUILDER
            .comment(
                    "If true, the Coal Generator automatically pushes energy to adjacent blocks",
                    "(pipes, energy ports, ...) that can receive energy.",
                    "The pushed amount per tick is the same as the generation rate (fePerTick).")
            .define("autoPush", true);

    public static final ModConfigSpec.ConfigValue<java.util.List<? extends java.lang.String>> COAL_GENERATOR_EXTRA_FUELS = BUILDER
            .comment(
                    "Extra fuels for the Coal Generator, format: <itemid>=<burntime_in_ticks>.",
                    "Example: minecraft:my_coal=1600",
                    "Vanilla fuels, datapack values and mod fuels are detected automatically",
                    "(same burn time as a vanilla furnace), this list is only for extra/custom fuels.")
            .defineListAllowEmpty("extraFuels", java.util.List.<String>of(), o -> o instanceof String s && s.contains("="));

    static {
        BUILDER.pop();
    }

    // --- Antimatter ---
    static {
        BUILDER.push("antimatter");
    }

    public static final ModConfigSpec.IntValue ANTIMATTER_TANK_CAPACITY = BUILDER
            .comment("Capacity of a single Antimatter Tank in antimatter units. " +
                    "The value should be at least as big as the transfer rate of the fastest pipe, " +
                    "because a pipe can only take out what the tank holds.")
            .defineInRange("tankCapacity", 32_000, 1, 1_000_000_000);

    public static final ModConfigSpec.IntValue ANTIMATTER_CELL_CAPACITY = BUILDER
            .comment("Capacity of a single Antimatter Cell in antimatter units.")
            .defineInRange("cellCapacity", 4_000, 1, 1_000_000_000);

    public static final ModConfigSpec.IntValue ANTIMATTER_PIPE_BASIC_RATE = BUILDER
            .comment("How many mB a basic antimatter pipe moves per tick and side (4 buckets).")
            .defineInRange("pipeBasicRate", 4_000, 1, 1_000_000);

    public static final ModConfigSpec.IntValue ANTIMATTER_PIPE_ADVANCED_RATE = BUILDER
            .comment("How many mB an advanced antimatter pipe moves per tick and side (8 buckets).")
            .defineInRange("pipeAdvancedRate", 8_000, 1, 1_000_000);

    public static final ModConfigSpec.IntValue ANTIMATTER_PIPE_ELITE_RATE = BUILDER
            .comment("How many mB an elite antimatter pipe moves per tick and side (16 buckets).")
            .defineInRange("pipeEliteRate", 16_000, 1, 1_000_000);

    public static final ModConfigSpec.IntValue ANTIMATTER_PIPE_ULTIMATE_RATE = BUILDER
            .comment("How many mB an ultimate antimatter pipe moves per tick and side (32 buckets per tick).")
            .defineInRange("pipeUltimateRate", 32_000, 1, 1_000_000);

    static {
        BUILDER.pop();
    }

    // --- Vacuum Pump ---
    static {
        BUILDER.push("vacuum");
    }

    public static final ModConfigSpec.DoubleValue VACUUM_BASE_FE_PER_TICK = BUILDER
            .comment(
                    "Base energy consumption of the vacuum pump while it evacuates or leaks (FE/t).",
                    "The volume of the pump structure itself (27 blocks) is already included in the volume.",
                    "The real consumption is scaled with the connected volume, see 'scaling'.")
            .defineInRange("baseFePerTick", 100.0, 0.0, 1_000_000.0);

    public static final ModConfigSpec.DoubleValue VACUUM_FE_PER_BLOCK = BUILDER
            .comment(
                    "Additional consumption per block of the connected volume (FE/t) in LINEAR mode.",
                    "Example: 0.2 -> a volume of 1000 blocks costs 200 FE/t more than the base.",
                    "Only used in LINEAR mode.")
            .defineInRange("fePerBlock", 0.2, 0.0, 10_000.0);

    public static final ModConfigSpec.EnumValue<VacuumScaling> VACUUM_SCALING = BUILDER
            .comment(
                    "How the consumption grows with the volume:",
                    "LINEAR: baseFePerTick + fePerBlock * volume",
                    "EXPONENTIAL: baseFePerTick * exponentialFactor ^ volume")
            .defineEnum("scaling", VacuumScaling.LINEAR);

    public static final ModConfigSpec.DoubleValue VACUUM_EXPONENTIAL_FACTOR = BUILDER
            .comment(
                    "Factor per block of volume in EXPONENTIAL mode.",
                    "Example: 1.001 -> a volume of 1000 blocks costs 2.7x the base.",
                    "Only used in EXPONENTIAL mode.")
            .defineInRange("exponentialFactor", 1.001, 1.0, 1.1);

    public static final ModConfigSpec.DoubleValue VACUUM_STABLE_FACTOR = BUILDER
            .comment(
                    "Share of the consumption while the vacuum is stable (0.1 = 10% of the evacuating rate).",
                    "The stable rate is also scaled with the volume.")
            .defineInRange("stableFactor", 0.1, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue VACUUM_EVAC_TICKS_PER_BLOCK = BUILDER
            .comment(
                    "Ticks of evacuation per block of volume (default 200/27 = 7.4 ticks per block).",
                    "The pump needs this many ticks to evacuate the whole connected volume.")
            .defineInRange("evacTicksPerBlock", 200.0 / 27.0, 0.0, 1000.0);

    public static final ModConfigSpec.BooleanValue VACUUM_COUNT_MACHINE_BLOCKS = BUILDER
            .comment(
                    "If true, the blocks of the connected multiblocks (accelerator, AD, ELENA, reactor,",
                    "tungsten) are counted into the volume of the pump, like the reactor already does.",
                    "If false, only the pump structure and the pipes are counted.")
            .define("countMachineBlocks", true);

    static {
        BUILDER.pop();
    }

    // --- Cooling Tower ---
    static {
        BUILDER.push("cooling_tower");
    }

    public static final ModConfigSpec.BooleanValue COOLING_TOWER_EXPLODE_ON_FAILURE = BUILDER
            .comment("If true, cooling tower explodes on failure.")
            .define("explodeOnFailure", false);

    static {
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    public enum ReactorFailureMode {
        MELTDOWN,
        EXPLOSION,
        NONE
    }

    /** Wie der Verbrauch der Vakuum-Pumpe mit dem Volumen wächst. */
    public enum VacuumScaling {
        LINEAR,
        EXPONENTIAL
    }
}