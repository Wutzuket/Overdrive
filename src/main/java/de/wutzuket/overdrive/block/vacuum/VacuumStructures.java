package de.wutzuket.overdrive.block.vacuum;

import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerBlock;
import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import de.wutzuket.overdrive.block.ad.ADControllerBlock;
import de.wutzuket.overdrive.block.ad.ADControllerEntity;
import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerBlock;
import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity;
import de.wutzuket.overdrive.block.elena.ELENAControllerBlock;
import de.wutzuket.overdrive.block.elena.ELENAControllerEntity;
import de.wutzuket.overdrive.block.tungsten.ElectronGunBlock;
import de.wutzuket.overdrive.block.tungsten.PositronVacuumOutputPortBlock;
import de.wutzuket.overdrive.block.tungsten.TungstenTargetBlock;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Volumen der Multiblock-Strukturen, die an einem Vakuum-Netz haengen.
 *
 * <p>Das Volumen ist die Anzahl der Bloecke, die evakuiert werden muessen. Die Vakuum-Pumpe
 * braucht es fuer die Evakuationszeit und fuer den Energieverbrauch: je groesser das Volumen,
 * desto laenger dauert die Evakuierung und desto mehr FE werden verbraucht.
 *
 * <p>Die Werte werden rein geometrisch aus der Bauart der Maschine berechnet (Ringe, Waende,
 * Hohlraum), damit das Ergebnis unabhaengig von einzelnen fehlenden Bloecken stabil bleibt.
 * Der Annihilationsreaktor zaehlt - wie bisher - seinen Torus-Hohlraum, die anderen Maschinen
 * zaehlen ihre Ring-Bloecke.
 */
public final class VacuumStructures {
    private VacuumStructures() {}

    /**
     * Volumen des Beschleuniger-Rings: zwei Waende in der Mittelebene plus voll gefuellte
     * Deck- und Bodenlage.
     */
    public static int acceleratorVolume(Level level, BlockPos portPos) {
        // Der Port kann auf der inneren oder aeusseren Wand liegen, der Controller auf der
        // aeusseren: deshalb der doppelte Radius als Suchradius.
        BlockPos ctrl = findController(level, portPos, 50, 1, ParticleAcceleratorControllerBlock.class);
        if (ctrl == null) return 0;
        int r = ParticleAcceleratorControllerEntity.OUTER_RADIUS;
        BlockPos center = centerOf(level, ctrl, r);
        int count = 0;
        for (int dx = -r - 2; dx <= r + 2; dx++) {
            for (int dz = -r - 2; dz <= r + 2; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < r - 3.5 || dist > r + 0.5) continue;
                // Deck- und Bodenlage sind im Ring-Band voll gefuellt
                count += 2;
                // Mittelebene: nur die beiden Waende
                if (dist >= r - 0.5 || dist <= r - 2.5) count++;
            }
        }
        return count;
    }

    /** Volumen des AD-Rings (quadratischer Ring, drei Lagen). */
    public static int adVolume(Level level, BlockPos portPos) {
        BlockPos ctrl = findController(level, portPos, 10, 1, ADControllerBlock.class);
        if (ctrl == null) return 0;
        int r = ADControllerEntity.OUTER_RADIUS;
        return squareRingBlocks(centerOf(level, ctrl, r), r, 3);
    }

    /** Volumen des ELENA-Rings (quadratischer Ring, drei Lagen). */
    public static int elenaVolume(Level level, BlockPos portPos) {
        BlockPos ctrl = findController(level, portPos, 6, 1, ELENAControllerBlock.class);
        if (ctrl == null) return 0;
        int r = ELENAControllerEntity.OUTER_RADIUS;
        return squareRingBlocks(centerOf(level, ctrl, r), r, 3);
    }

    /**
     * Volumen des Annihilationsreaktors: der Hohlraum des Torus (Luft, die evakuiert wird).
     * Faellt der Controller nicht gefunden werden, wird der Erfahrungswert 1200 genutzt.
     */
    public static int annihilationVolume(Level level, BlockPos portPos) {
        BlockPos ctrl = findController(level, portPos, 25, 15, AnnihilationControllerBlock.class);
        if (ctrl == null) return 1200;
        BlockPos center = AnnihilationControllerEntity.getCenter(ctrl, level.getBlockState(ctrl));
        int outer = AnnihilationControllerEntity.OUTER;
        int count = 0;
        for (int x = center.getX() - outer; x <= center.getX() + outer; x++) {
            for (int y = center.getY() - outer; y <= center.getY() + outer; y++) {
                for (int z = center.getZ() - outer; z <= center.getZ() + outer; z++) {
                    int dx = Math.abs(x - center.getX());
                    int dy = Math.abs(y - center.getY());
                    int dz = Math.abs(z - center.getZ());
                    if (AnnihilationControllerEntity.isTorusHollow(dx, dy, dz)) count++;
                }
            }
        }
        return count;
    }

    /** Volumen der kleinen Wolfram-Maschine (Elektronenkanone, Target, Target, Gehaeuse). */
    public static int tungstenVolume(Level level, BlockPos portPos) {
        int count = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    Block block = level.getBlockState(portPos.offset(dx, dy, dz)).getBlock();
                    if (block == ODBlocks.TUNGSTEN_CASING.get()
                            || block instanceof ElectronGunBlock
                            || block instanceof TungstenTargetBlock
                            || block instanceof PositronVacuumOutputPortBlock) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /** Mittelpunkt eines Rings: Controller + facing * Radius. */
    private static BlockPos centerOf(Level level, BlockPos controllerPos, int radius) {
        BlockState state = level.getBlockState(controllerPos);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return controllerPos.offset(facing.getStepX() * radius, 0, facing.getStepZ() * radius);
    }

    /** Bloecke eines quadratischen Rings (Chebyshev-Radius) ueber die angegebene Zahl Lagen. */
    private static int squareRingBlocks(BlockPos center, int radius, int layers) {
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int maxDist = Math.max(Math.abs(dx), Math.abs(dz));
                if (maxDist >= radius - 1 && maxDist <= radius) count++;
            }
        }
        return count * layers;
    }

    /** Sucht den Controller einer Maschine in der Naehe des Vakuum-Ports. */
    private static @Nullable BlockPos findController(Level level, BlockPos portPos, int radius, int yRadius,
                                                     Class<? extends Block> controllerBlock) {
        for (int dy = -yRadius; dy <= yRadius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = portPos.offset(dx, dy, dz);
                    if (controllerBlock.isInstance(level.getBlockState(pos).getBlock())) {
                        return pos.immutable();
                    }
                }
            }
        }
        return null;
    }
}
