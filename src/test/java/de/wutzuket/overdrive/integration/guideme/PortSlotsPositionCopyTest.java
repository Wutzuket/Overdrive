package de.wutzuket.overdrive.integration.guideme;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GuideME gibt die gefuellten Positionen einer Szene als Stream zurueck, in dem alle Eintraege
 * dieselbe veraenderliche {@code BlockPos}-Instanz teilen. Am Ende der Iteration zeigt damit
 * jeder Eintrag auf die letzte Position der Struktur.
 *
 * <p>Der Compiler der Mod kopiert die Positionen deshalb. Ohne die Kopie wuerde der Controller
 * einer Maschine nur gefunden, wenn er zufaellig der letzte Block der importierten Struktur ist -
 * beim AD-Ring und beim Annihilationsreaktor war das nicht der Fall, die Seiten zeigten den
 * Fehler "Controller not found in the scene".</p>
 */
class PortSlotsPositionCopyTest {

    @Test
    void positionsAreCopiedSoThatEveryEntryKeepsItsOwnValue() {
        // So liefert GuideME die Positionen: eine MutableBlockPos-Instanz fuer alle Eintraege.
        var mutable = new BlockPos.MutableBlockPos();
        List<Long> packed = List.of(
                BlockPos.asLong(4, 1, 8),
                BlockPos.asLong(0, 0, 0),
                BlockPos.asLong(8, 2, 8));

        var guideMeStream = packed.stream().map(value -> (BlockPos) mutable.set(value));

        var copied = PortSlotsElementCompiler.copyPositions(guideMeStream);

        assertEquals(packed.size(), copied.size());
        assertEquals(packed.size(), copied.stream().map(BlockPos::toShortString).distinct().count(),
                "jede Position muss ihren eigenen Wert behalten");

        assertTrue(copied.stream().anyMatch(pos -> pos.equals(new BlockPos(4, 1, 8))),
                "die Position des Controllers muss vorhanden sein");
    }

    @Test
    void theControllerIsFoundEvenWhenItIsNotTheLastBlock() {
        // Der Controller steht nicht an letzter Stelle der Struktur.
        var mutable = new BlockPos.MutableBlockPos();
        var guideMeStream = Stream.of(
                BlockPos.asLong(0, 0, 0),
                BlockPos.asLong(4, 1, 8),
                BlockPos.asLong(8, 2, 8)).map(value -> (BlockPos) mutable.set(value));

        var copied = PortSlotsElementCompiler.copyPositions(guideMeStream);

        var controller = copied.stream()
                .filter(pos -> pos.equals(new BlockPos(4, 1, 8)))
                .findFirst();
        assertTrue(controller.isPresent(),
                "der Controller muss gefunden werden, auch wenn er nicht der letzte Block ist");
    }
}
