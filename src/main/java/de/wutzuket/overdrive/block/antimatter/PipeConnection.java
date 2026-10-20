package de.wutzuket.overdrive.block.antimatter;

import net.minecraft.util.StringRepresentable;

/**
 * The connection state of a single pipe side as it is stored in the block state.
 */
public enum PipeConnection implements StringRepresentable {

    /** Nothing is connected to this side. */
    NONE("none"),
    /** A tube is connected to this side and antimatter can flow through it. */
    OPEN("open"),
    /** A tube is connected to this side and the pipe pulls the antimatter out of the neighbour. */
    PULL("pull"),
    /** A tube is connected to this side and the pipe pushes the antimatter into the neighbour. */
    PUSH("push"),
    /** A tube is connected to this side but the side is blocked and rendered with a cap. */
    DISABLED("disabled");

    private final String name;

    PipeConnection(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
