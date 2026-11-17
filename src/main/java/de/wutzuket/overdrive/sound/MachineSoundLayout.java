package de.wutzuket.overdrive.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the positions at which the sound of a machine is played.
 *
 * A big multiblock spreads its sound over the whole structure, so that it does
 * not come from the controller only.
 */
public final class MachineSoundLayout {
    private MachineSoundLayout() {
    }

    /**
     * Positions on a circle around the center.
     *
     * @param center the middle of the structure
     * @param radius the distance of the positions from the center
     * @param count how many positions the sound is spread over
     */
    public static List<BlockPos> ring(BlockPos center, double radius, int count) {
        if (count <= 1 || radius <= 0.0D) {
            return List.of(center);
        }
        var positions = new ArrayList<BlockPos>(count);
        for (int i = 0; i < count; i++) {
            double angle = 2.0D * Math.PI * i / count;
            int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
            positions.add(new BlockPos(x, center.getY(), z));
        }
        return positions;
    }

    /**
     * Positions on a straight line, starting at the given position.
     *
     * @param start the first position of the line
     * @param direction the direction of the line
     * @param count how many positions the sound is spread over
     */
    public static List<BlockPos> line(BlockPos start, Direction direction, int count) {
        if (count <= 1) {
            return List.of(start);
        }
        var positions = new ArrayList<BlockPos>(count);
        for (int i = 0; i < count; i++) {
            positions.add(start.relative(direction, i));
        }
        return positions;
    }
}
