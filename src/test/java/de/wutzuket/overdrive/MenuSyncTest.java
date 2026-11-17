package de.wutzuket.overdrive;

import de.wutzuket.overdrive.block.coalgenerator.CoalGeneratorEntity;
import de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity;
import de.wutzuket.overdrive.block.distiller.CryoDistillerEntity;
import de.wutzuket.overdrive.block.press.CircuitPressBlockEntity;
import de.wutzuket.overdrive.block.scanner.scanner_blockentity;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.menu.AntimatterTankMenu;
import de.wutzuket.overdrive.menu.CoalGeneratorMenu;
import de.wutzuket.overdrive.menu.CoolingTowerMenu;
import de.wutzuket.overdrive.menu.CircuitPressMenu;
import de.wutzuket.overdrive.menu.CryoDistillerMenu;
import de.wutzuket.overdrive.menu.scanner_menu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft, dass die Werte der Maschinen im GUI jeden Tick ankommen.
 *
 * <p>Die Werte kommen ueber {@code setData}, so schickt der Server sie, wenn
 * sich etwas aendert. Das Menue darf sie deshalb nicht aus dem Block Entity
 * lesen, denn der Client holt das Block Entity nur beim Laden des Chunks.</p>
 */
class MenuSyncTest {

    private static final BlockPos POS = new BlockPos(0, 64, 0);

    /** Das Inventar wird fuer die Spieler-Slots gebraucht, der Spieler selbst nicht. */
    private static Inventory inventory() {
        return new Inventory((net.minecraft.world.entity.player.Player) null);
    }

    @Test
    void theCoalGeneratorGetsTheEnergyFromTheServer() {
        var state = ODBlocks.COAL_GENERATOR.get().defaultBlockState();
        var menu = new CoalGeneratorMenu(0, inventory(), new CoalGeneratorEntity(POS, state));

        menu.setData(0, 12345);
        menu.setData(1, 400000);
        menu.setData(2, 1);
        menu.setData(3, 120);
        menu.setData(4, 200);

        assertEquals(12345, menu.getEnergy());
        assertEquals(400000, menu.getMaxEnergy());
        assertTrue(menu.isActive());
        assertEquals(120, menu.getBurnTime());
        assertEquals(200, menu.getCurrentBurnTime());
    }

    @Test
    void theCircuitPressGetsTheEnergyFromTheServer() {
        var state = ODBlocks.CIRCUIT_PRESS.get().defaultBlockState();
        var menu = new CircuitPressMenu(0, inventory(), new CircuitPressBlockEntity(POS, state));

        menu.setData(0, 500);
        menu.setData(1, 1000000);
        menu.setData(2, 42);
        menu.setData(3, 100);
        menu.setData(4, 1);

        assertEquals(500, menu.getEnergy());
        assertEquals(1000000, menu.getMaxEnergy());
        assertEquals(42, menu.getProgress());
        assertEquals(100, menu.getMaxProgress());
        assertTrue(menu.isActive());
    }

    @Test
    void theCryoDistillerGetsTheEnergyFromTheServer() {
        var state = ODBlocks.CRYO_DISTILLER.get().defaultBlockState();
        var menu = new CryoDistillerMenu(0, inventory(), new CryoDistillerEntity(POS, state));

        menu.setData(0, 750);
        menu.setData(1, 100000000);
        menu.setData(2, 17);
        menu.setData(3, 100);
        menu.setData(4, 1);

        assertEquals(750, menu.getEnergy());
        assertEquals(100000000, menu.getMaxEnergy());
        assertEquals(17, menu.getProgress());
        assertEquals(100, menu.getMaxProgress());
        assertTrue(menu.isActive());
    }

    @Test
    void theScannerGetsTheEnergyFromTheServer() {
        var state = ODBlocks.SCANNER.get().defaultBlockState();
        var menu = new scanner_menu(0, inventory(), new scanner_blockentity(POS, state));

        menu.setData(0, 900);
        menu.setData(1, 100000);

        assertEquals(900, menu.getEnergy());
        assertEquals(100000, menu.getMaxEnergy());
    }

    @Test
    void theCoolingTowerGetsTheProgressFromTheServer() {
        var state = ODBlocks.COOLING_TOWER_CONTROLLER.get().defaultBlockState();
        var menu = new CoolingTowerMenu(0, inventory(), new CoolingTowerControllerEntity(POS, state));

        menu.setData(0, 33);
        menu.setData(1, 100);
        menu.setData(2, 1);
        menu.setData(3, 1);

        assertEquals(33, menu.getProgress());
        assertEquals(100, menu.getMaxProgress());
        assertTrue(menu.isActive());
        assertTrue(menu.isValid());
    }

    @Test
    void theAntimatterTankGetsTheAmountFromTheServer() {
        var state = ODBlocks.ANTIMATTER_TANK.get().defaultBlockState();
        var menu = new AntimatterTankMenu(0, inventory(),
                new de.wutzuket.overdrive.block.antimatter.AntimatterTankEntity(POS, state));

        menu.setData(0, 2500);
        menu.setData(1, 32000);

        assertEquals(2500, menu.getAmount());
        assertEquals(32000, menu.getCapacity());
    }

    @Test
    void aStoppedMachineIsNotActive() {
        var state = ODBlocks.CIRCUIT_PRESS.get().defaultBlockState();
        var menu = new CircuitPressMenu(0, inventory(), new CircuitPressBlockEntity(POS, state));

        menu.setData(4, 0);
        assertFalse(menu.isActive());
    }
}
