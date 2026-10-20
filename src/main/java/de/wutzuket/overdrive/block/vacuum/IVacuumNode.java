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

    /**
     * Volumen der Multiblock-Struktur, an der dieser Port haengt (Anzahl der Bloecke).
     * Die Vakuum-Pumpe zaehlt diese Bloecke in ihr Volumen ein - dadurch wird z.B. der
     * Beschleuniger-Ring genauso behandelt wie der Reaktor. Ports ohne Struktur
     * (Pipes, Pumpe selbst) liefern 0.
     */
    default int getStructureVolume() {
        return 0;
    }

    /** Letzter Tick, zu dem dieser Node aktiv von einer Pumpe gespeist wurde. */
    default long getVacuumFedTick() { return 0; }
    default void setVacuumFedTick(long tick) { }

    /** Gnadenfrist in Ticks, bevor ein ungespeistes Netz zu lecken beginnt. */
    long FED_GRACE_TICKS = 40;
    /** Vakuum-Verlust pro Tick nach Ablauf der Gnadenfrist. */
    float STALE_DRAIN_PER_TICK = 0.01f;

    static void propagate(BlockEntity self) {
        if (!(self instanceof IVacuumNode node)) return;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) return;
        BlockPos pos = self.getBlockPos();

        float sum = node.getVacuumLevel();
        int count = 1;
        long fed = node.getVacuumFedTick();
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be instanceof IVacuumNode neighbor && be != self
                    && node.canConnect(dir)
                    && neighbor.canConnect(dir.getOpposite())) {
                sum += neighbor.getVacuumLevel();
                count++;
                fed = Math.max(fed, neighbor.getVacuumFedTick());
            }
        }
        float avg = sum / count;
        if (level.getGameTime() - fed > FED_GRACE_TICKS) {
            // Kein Pumpensignal mehr (Pipe getrennt, Pumpe aus): Netz leckt leer,
            // statt den letzten Wert ewig zu halten.
            avg = Math.max(0, avg - STALE_DRAIN_PER_TICK);
        }
        node.setVacuumLevel(avg);
        node.setVacuumFedTick(fed);
    }

    static float getVacuumLevel(BlockEntity be) {
        if (be instanceof IVacuumNode node) return node.getVacuumLevel();
        return -1;
    }
}
