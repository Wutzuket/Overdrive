package de.wutzuket.overdrive.block.vacuum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IVacuumNode {
    float getVacuumLevel();
    void setVacuumLevel(float level);
    default boolean canConnect(Direction side) {
        return true;
    }

    static void propagate(BlockEntity self) {
        if (!(self instanceof IVacuumNode node)) return;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) return;
        BlockPos pos = self.getBlockPos();

        float sum = node.getVacuumLevel();
        int count = 1;
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be instanceof IVacuumNode neighbor && be != self
                    && node.canConnect(dir)
                    && neighbor.canConnect(dir.getOpposite())) {
                sum += neighbor.getVacuumLevel();
                count++;
            }
        }
        node.setVacuumLevel(sum / count);
    }

    static float getVacuumLevel(BlockEntity be) {
        if (be instanceof IVacuumNode node) return node.getVacuumLevel();
        return -1;
    }
}
