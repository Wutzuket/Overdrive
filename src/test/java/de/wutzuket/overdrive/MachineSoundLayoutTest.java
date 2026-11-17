package de.wutzuket.overdrive;

import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import de.wutzuket.overdrive.block.ad.ADControllerEntity;
import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity;
import de.wutzuket.overdrive.block.coalgenerator.CoalGeneratorEntity;
import de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity;
import de.wutzuket.overdrive.block.elena.ELENAControllerEntity;
import de.wutzuket.overdrive.block.tungsten.ElectronGunEntity;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlockEntity;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.sound.MachineSoundLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft, wo der Klang einer Maschine abgespielt wird.
 *
 * <p>Bei einem Multiblock darf der Klang nicht nur vom Controller kommen,
 * sondern muss ueber die ganze Struktur verteilt sein.</p>
 */
class MachineSoundLayoutTest {

    private static final BlockPos CONTROLLER = new BlockPos(10, 64, 10);

    @Test
    void aRingIsSpreadOverTheWholeCircle() {
        var positions = MachineSoundLayout.ring(BlockPos.ZERO, 25.0D, 8);
        assertEquals(8, positions.size());
        for (var pos : positions) {
            double distance = Math.sqrt(pos.getX() * (double) pos.getX() + pos.getZ() * (double) pos.getZ());
            assertTrue(Math.abs(distance - 25.0D) < 1.5D,
                    "Die Position liegt nicht auf dem Kreis: " + pos);
        }
    }

    @Test
    void aLineFollowsTheDirection() {
        var positions = MachineSoundLayout.line(BlockPos.ZERO, Direction.EAST, 3);
        assertEquals(3, positions.size());
        assertEquals(new BlockPos(0, 0, 0), positions.get(0));
        assertEquals(new BlockPos(1, 0, 0), positions.get(1));
        assertEquals(new BlockPos(2, 0, 0), positions.get(2));
    }

    @Test
    void theAcceleratorSpreadsTheSoundOverTheRing() {
        var state = ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get().defaultBlockState();
        var entity = new ParticleAcceleratorControllerEntity(CONTROLLER, state);
        var positions = entity.getSoundPositions(CONTROLLER, state);

        assertTrue(positions.size() > 1, "Der Klang muss auf mehrere Stellen verteilt sein");
        // Der Controller steht auf dem Ring, der Klang muss sich aber ueber den
        // ganzen Ring verteilen und nicht nur am Controller stehen.
        double farthest = 0.0D;
        for (var pos : positions) {
            farthest = Math.max(farthest, distance(pos, CONTROLLER));
        }
        assertTrue(farthest > 30.0D,
                "Der Klang muss sich ueber den ganzen Ring verteilen");
    }

    @Test
    void theAdRingSpreadsTheSoundOverTheRing() {
        var state = ODBlocks.AD_CONTROLLER.get().defaultBlockState();
        var entity = new ADControllerEntity(CONTROLLER, state);
        assertTrue(entity.getSoundPositions(CONTROLLER, state).size() > 1);
    }

    @Test
    void theElenaRingSpreadsTheSoundOverTheRing() {
        var state = ODBlocks.ELENA_CONTROLLER.get().defaultBlockState();
        var entity = new ELENAControllerEntity(CONTROLLER, state);
        assertTrue(entity.getSoundPositions(CONTROLLER, state).size() > 1);
    }

    @Test
    void theReactorSpreadsTheSoundOverTheTorus() {
        var state = ODBlocks.ANNIHILATION_CONTROLLER.get().defaultBlockState();
        var entity = new AnnihilationControllerEntity(CONTROLLER, state);
        var positions = entity.getSoundPositions(CONTROLLER, state);

        assertTrue(positions.size() > 1, "Der Klang muss auf mehrere Stellen verteilt sein");
        double farthest = 0.0D;
        for (var pos : positions) {
            farthest = Math.max(farthest, distance(pos, CONTROLLER));
        }
        assertTrue(farthest > 10.0D,
                "Der Klang muss sich ueber den ganzen Torus verteilen");
    }

    @Test
    void theCoolingTowerSpreadsTheSoundOverItsRim() {
        var state = ODBlocks.COOLING_TOWER_CONTROLLER.get().defaultBlockState();
        var entity = new CoolingTowerControllerEntity(CONTROLLER, state);
        assertTrue(entity.getSoundPositions(CONTROLLER, state).size() > 1);
    }

    @Test
    void theVacuumPumpSpreadsTheSoundOverItsHousing() {
        var state = ODBlocks.VACUUM_PUMP.get().defaultBlockState();
        var entity = new VacuumPumpBlockEntity(CONTROLLER, state);
        assertTrue(entity.getSoundPositions(CONTROLLER, state).size() > 1);
    }

    @Test
    void theElectronGunSpreadsTheSoundOverTheBarrel() {
        var state = ODBlocks.ELECTRON_GUN.get().defaultBlockState();
        var entity = new ElectronGunEntity(CONTROLLER, state);
        var positions = entity.getSoundPositions(CONTROLLER, state);

        assertEquals(3, positions.size());
        assertTrue(distance(positions.get(2), CONTROLLER) > 1.0D,
                "Der Klang muss auch am Ende der Rohre kommen");
    }

    @Test
    void aSingleBlockMachineKeepsItsOwnPlace() {
        var state = ODBlocks.COAL_GENERATOR.get().defaultBlockState();
        var entity = new CoalGeneratorEntity(CONTROLLER, state);
        var positions = entity.getSoundPositions(CONTROLLER, state);

        assertEquals(1, positions.size());
        assertEquals(CONTROLLER, positions.get(0));
    }

    @Test
    void theFacingMovesTheSoundWithTheMachine() {
        var block = ODBlocks.PARTICLE_ACCELERATOR_CONTROLLER.get();
        var north = block.defaultBlockState();
        var east = north.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);

        var entity = new ParticleAcceleratorControllerEntity(CONTROLLER, north);
        var northPositions = entity.getSoundPositions(CONTROLLER, north);
        var eastPositions = entity.getSoundPositions(CONTROLLER, east);

        assertTrue(!northPositions.equals(eastPositions),
                "Der Klang muss der Ausrichtung der Maschine folgen");
    }

    /** Der Abstand zwischen zwei Bloecken. */
    private static double distance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
