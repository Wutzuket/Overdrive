package de.wutzuket.overdrive.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ODConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_ALUMINUM_GENERATION = BUILDER
            .comment(
                    "Enable Aluminum Ore world generation.",
                    "Set to false to disable all aluminum ore generation (for modpack compatibility).")
            .define("enableAluminumGeneration", true);

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
}