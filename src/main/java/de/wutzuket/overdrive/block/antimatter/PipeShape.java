package de.wutzuket.overdrive.block.antimatter;

import net.minecraft.util.StringRepresentable;

/**
 * The overall shape of a pipe. Straight pipes are rendered as one continuous tube without the
 * bulky center cube, every other combination uses the closed center cube.
 */
public enum PipeShape implements StringRepresentable {

    /** The closed center cube is rendered. */
    CENTER("center"),
    /** A continuous tube running from west to east. */
    STRAIGHT_X("straight_x"),
    /** A continuous tube running from north to south. */
    STRAIGHT_Z("straight_z"),
    /** A continuous tube running from bottom to top. */
    STRAIGHT_Y("straight_y");

    private final String name;

    PipeShape(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
