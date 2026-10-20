package de.wutzuket.overdrive.antimatter;

import net.minecraft.network.chat.Component;

/**
 * Configuration of a single pipe side.
 */
public enum PipeSideMode {

    /** Transfers in both directions, driven by the fill level of the network. */
    NORMAL("normal"),
    /** Always pushes antimatter out of the pipe. */
    PUSH("push"),
    /** Always pulls antimatter into the pipe. */
    PULL("pull"),
    /** The side is blocked, no antimatter can pass and a cap is rendered. */
    DISABLED("disabled");

    private final String name;

    PipeSideMode(String name) {
        this.name = name;
    }

    public String getSerializedName() {
        return name;
    }

    /** @return the next mode in the cycle normal -&gt; push -&gt; pull -&gt; disabled. */
    public PipeSideMode next() {
        return switch (this) {
            case NORMAL -> PUSH;
            case PUSH -> PULL;
            case PULL -> DISABLED;
            case DISABLED -> NORMAL;
        };
    }

    /** @return the previous mode in the cycle. */
    public PipeSideMode previous() {
        return switch (this) {
            case NORMAL -> DISABLED;
            case DISABLED -> PULL;
            case PULL -> PUSH;
            case PUSH -> NORMAL;
        };
    }

    public Component displayName() {
        return Component.translatable("pipe_mode.overdrive." + name);
    }
}
