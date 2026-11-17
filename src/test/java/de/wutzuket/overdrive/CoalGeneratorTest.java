package de.wutzuket.overdrive;

import de.wutzuket.overdrive.block.coalgenerator.CoalGeneratorEntity;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft das Verhalten des Kohlekraftwerks.
 *
 * <p>Ist der Energiespeicher voll und gibt der Generator nichts an Nachbarn
 * ab, soll er aufhoeren zu brennen. Die Kohle bleibt aber erhalten, damit er
 * weiterlaeuft, sobald wieder Platz ist.</p>
 */
class CoalGeneratorTest {

    private static final int MAX = 400000;

    @Test
    void aFullGeneratorStopsBurning() {
        assertFalse(CoalGeneratorEntity.isRunning(100, MAX, MAX, 0),
                "Ein voller Generator darf nicht brennen");
    }

    @Test
    void aFullGeneratorKeepsBurningWhileItDeliversEnergy() {
        assertTrue(CoalGeneratorEntity.isRunning(100, MAX, MAX, 20),
                "Wenn Energie an Nachbarn abgegeben wird, laeuft der Generator weiter");
    }

    @Test
    void aGeneratorWithSpaceBurns() {
        assertTrue(CoalGeneratorEntity.isRunning(100, 100, MAX, 0),
                "Ein Generator mit Platz brennt");
    }

    @Test
    void aGeneratorWithoutFuelDoesNotBurn() {
        assertFalse(CoalGeneratorEntity.isRunning(0, 100, MAX, 0),
                "Ohne Brennstoff brennt der Generator nicht");
    }

    @Test
    void theRunningStateIsSavedForTheClient() {
        var state = ODBlocks.COAL_GENERATOR.get().defaultBlockState();
        var be = new CoalGeneratorEntity(new BlockPos(0, 64, 0), state);
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var tag = be.saveWithoutMetadata(registries);

        assertTrue(tag.contains("running"),
                "Der Brennzustand muss im NBT stehen, damit das GUI und der Klang stimmen");
    }
}
