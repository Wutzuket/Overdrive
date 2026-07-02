package de.wutzuket.overdrive.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ODConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_ALUMINUM_GENERATION = BUILDER
            .comment(
                    "Enable Aluminum Ore world generation.",
                    "Set to false to disable all aluminum ore generation (for modpack compatibility).")
            .define("enableAluminumGeneration", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
