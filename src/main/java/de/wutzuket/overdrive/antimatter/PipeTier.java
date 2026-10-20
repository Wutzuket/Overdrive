package de.wutzuket.overdrive.antimatter;

import de.wutzuket.overdrive.config.ODConfig;

/**
 * The tiers of the antimatter pipe. Every tier has its own color and its own transfer rate, the
 * rate can be changed in the configuration.
 */
public enum PipeTier {

    /** 250 mB per tick. */
    BASIC("basic", 0xFF4CE04C),
    /** 1000 mB per tick. */
    ADVANCED("advanced", 0xFFFF4030),
    /** 8000 mB per tick. */
    ELITE("elite", 0xFF2E8BFF),
    /** 32000 mB per tick. */
    ULTIMATE("ultimate", 0xFFC13BFF);

    private final String id;
    private final int color;

    PipeTier(String id, int color) {
        this.id = id;
        this.color = color;
    }

    /** @return the name of the tier, used for the registry names of the blocks and items. */
    public String getId() {
        return id;
    }

    /** @return the color of the tier, it is used for the tint of the pipe and the top display. */
    public int getColor() {
        return color;
    }

    /** @return how many mB the pipe moves per tick and side. */
    public int getTransferRate() {
        return switch (this) {
            case BASIC -> ODConfig.ANTIMATTER_PIPE_BASIC_RATE.get();
            case ADVANCED -> ODConfig.ANTIMATTER_PIPE_ADVANCED_RATE.get();
            case ELITE -> ODConfig.ANTIMATTER_PIPE_ELITE_RATE.get();
            case ULTIMATE -> ODConfig.ANTIMATTER_PIPE_ULTIMATE_RATE.get();
        };
    }

    /** @return how much antimatter the pipe can hold. */
    public int getBufferCapacity() {
        return Math.max(1, getTransferRate() * 4);
    }

    /** @return the tier with the given name or {@link #BASIC} when it is unknown. */
    public static PipeTier byId(String id) {
        for (PipeTier tier : values()) {
            if (tier.id.equals(id)) {
                return tier;
            }
        }
        return BASIC;
    }
}
