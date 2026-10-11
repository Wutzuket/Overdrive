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

    static {
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
