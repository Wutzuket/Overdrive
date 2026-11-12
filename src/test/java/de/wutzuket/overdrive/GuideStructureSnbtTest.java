package de.wutzuket.overdrive;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft die SNBT-Strukturdateien des Handbuchs (GuideME) gegen den Vanilla-Parser.
 *
 * <p>GuideME laedt {@code .snbt}-Dateien ueber {@link NbtUtils#snbtToStructure(String)},
 * das intern das gepackte Vanilla-Strukturformat erwartet: die Palette ist eine
 * Liste von Strings und die Bloecke stehen unter {@code data} mit dem Zustand als
 * String. Ein falsches Format fuehrt im Spiel zu leeren 3D-Szenen.
 */
class GuideStructureSnbtTest {

    private static final String STRUCTURE_PATH =
            "assets/overdrive/guides/overdrive/guide/structures/";

    @Test
    void allStructureFilesAreValid() throws IOException, CommandSyntaxException {
        var loader = GuideStructureSnbtTest.class.getClassLoader();

        // Die Strukturdateien werden ueber den Classloader gelesen. So spielt es
        // keine Rolle, ob sie im Entwicklungs-Setup im Klassen- oder im
        // Ressourcen-Ordner liegen.
        var names = List.of(
                "vacuum_pump.snbt",
                "particle_accelerator.snbt",
                "ad_ring.snbt",
                "elena_ring.snbt",
                "electron_gun.snbt",
                "annihilation_reactor.snbt",
                "cooling_tower.snbt");

        for (var name : names) {
            var url = loader.getResource(STRUCTURE_PATH + name);
            assertNotNull(url, "Strukturdatei fehlt: " + name);

            String text;
            try (var in = url.openStream()) {
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            // 1. Der SNBT-Parser muss die Datei ueberhaupt lesen koennen.
            CompoundTag parsed = TagParser.parseTag(text);
            assertTrue(parsed.contains("size"), name + ": size fehlt");
            assertTrue(parsed.contains("palette"), name + ": palette fehlt");
            assertTrue(parsed.contains("data"), name + ": data fehlt");

            // 2. Das gepackte Format in das interne Format umwandeln.
            CompoundTag unpacked = NbtUtils.snbtToStructure(text);

            ListTag palette = unpacked.getList("palette", 10);
            ListTag blocks = unpacked.getList("blocks", 10);
            ListTag size = unpacked.getList("size", 3);

            assertEquals(3, size.size(), name + ": size muss 3 Werte haben");
            assertFalse(palette.isEmpty(), name + ": leere Palette");
            assertFalse(blocks.isEmpty(), name + ": leere Blockliste");

            // 3. Jeder Paletteneintrag muss ein Blockzustand mit Name sein.
            for (int i = 0; i < palette.size(); i++) {
                CompoundTag entry = palette.getCompound(i);
                assertTrue(entry.contains("Name"), name + ": Paletteneintrag ohne Name");
                assertTrue(entry.getString("Name").contains(":"),
                        name + ": ungueltiger Blockname " + entry.getString("Name"));
            }

            // 4. Die Blockpositionen muessen innerhalb der Struktur liegen.
            int maxX = size.getInt(0);
            int maxY = size.getInt(1);
            int maxZ = size.getInt(2);
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag block = blocks.getCompound(i);
                ListTag pos = block.getList("pos", 3);
                assertEquals(3, pos.size(), name + ": ungueltige Position");
                assertTrue(pos.getInt(0) >= 0 && pos.getInt(0) < maxX, name + ": x ausserhalb");
                assertTrue(pos.getInt(1) >= 0 && pos.getInt(1) < maxY, name + ": y ausserhalb");
                assertTrue(pos.getInt(2) >= 0 && pos.getInt(2) < maxZ, name + ": z ausserhalb");
                int state = block.getInt("state");
                assertTrue(state >= 0 && state < palette.size(),
                        name + ": Zustandsindex ausserhalb der Palette");
            }
        }
    }

    /**
     * Beide Ports der Vakuumpumpe koennen an jeder freien Stelle des
     * 3x3x3-Wuerfels stehen. Der Ausgangsport sitzt im Beispiel in der
     * hinteren Ebene, aber nicht genau in der Mitte.
     */
    @Test
    void vacuumPumpOutputPortIsInsideTheCube() throws IOException, CommandSyntaxException {
        var blocks = readStructure("vacuum_pump.snbt");

        List<BlockPos> ports = new ArrayList<>();
        for (var entry : blocks.entrySet()) {
            if (entry.getValue().startsWith("overdrive:vacuum_output_port")) {
                ports.add(entry.getKey());
            }
        }

        assertEquals(1, ports.size(), "Es muss genau einen Vakuum-Ausgang geben");
        var port = ports.getFirst();

        // Der Wuerfel reicht von z=0 bis z=2 (0-basierte SNBT-Koordinaten).
        // Der Ausgang sitzt am Rand des Wuerfels, dahinter steckt das Rohr.
        assertEquals(2, port.getZ(), "Der Vakuum-Ausgang muss am Rand des Wuerfels liegen");
        var pipe = blocks.get(port.offset(0, 0, 1));
        assertTrue(pipe != null && pipe.startsWith("overdrive:vacuum_pipe"),
                "hinter dem Vakuum-Ausgang muss das Rohr in das Netz fuehren");

        // Genau ein Energie-Port.
        long energy = blocks.entrySet().stream()
                .filter(e -> e.getValue().startsWith("overdrive:vacuum_energy_port"))
                .count();
        assertEquals(1, energy, "Es muss genau einen Energie-Port geben");
    }

    /**
     * Die Elektronenkanone ist eine Reihe aus drei Bloecken: Kanone, Target
     * einen Block davor, Positronen-Port zwei Bloecke davor
     * (ElectronGunEntity.findTungstenTarget / findOutputPort).
     */
    @Test
    void electronGunIsAStraightLineOfThreeBlocks() throws IOException, CommandSyntaxException {
        var blocks = readStructure("electron_gun.snbt");

        var gun = findBlock(blocks, "overdrive:electron_gun");
        var target = findBlock(blocks, "overdrive:tungsten_target");
        var port = findBlock(blocks, "overdrive:positron_vacuum_output_port");

        assertEquals(gun.getY(), target.getY(), "Target muss auf gleicher Hoehe liegen");
        assertEquals(gun.getY(), port.getY(), "Port muss auf gleicher Hoehe liegen");
        assertEquals(gun.getX(), target.getX(), "Target muss in derselben Spur liegen");
        assertEquals(gun.getX(), port.getX(), "Port muss in derselben Spur liegen");

        // Die Kanone blickt nach Norden, das Target liegt also bei z-1 und der
        // Port bei z-2.
        assertEquals(gun.getZ() - 1, target.getZ(), "Target muss einen Block vor der Kanone liegen");
        assertEquals(gun.getZ() - 2, port.getZ(), "Port muss zwei Bloecke vor der Kanone liegen");

        // Das Vakuumrohr steckt hinter der Kanone, nicht am Positronen-Port.
        BlockPos pipe = findBlock(blocks, "overdrive:vacuum_pipe");
        assertTrue(pipe.getZ() > gun.getZ(),
                "Das Vakuumrohr muss hinter der Kanone liegen, nicht am Positronen-Port");
    }

    private static BlockPos findBlock(Map<BlockPos, String> blocks, String id) {
        for (var entry : blocks.entrySet()) {
            if (entry.getValue().startsWith(id)) {
                return entry.getKey();
            }
        }
        throw new AssertionError("Block " + id + " fehlt in der Struktur");
    }

    /**
     * Liest eine SNBT-Datei und liefert die Bloecke mit ihrem Zustandsnamen.
     */
    private static Map<BlockPos, String> readStructure(String name) throws IOException, CommandSyntaxException {
        var loader = GuideStructureSnbtTest.class.getClassLoader();
        var url = loader.getResource(STRUCTURE_PATH + name);
        assertNotNull(url, "Strukturdatei fehlt: " + name);

        String text;
        try (var in = url.openStream()) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        CompoundTag unpacked = NbtUtils.snbtToStructure(text);
        ListTag palette = unpacked.getList("palette", 10);
        ListTag blocks = unpacked.getList("blocks", 10);

        var result = new LinkedHashMap<BlockPos, String>();
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag block = blocks.getCompound(i);
            ListTag pos = block.getList("pos", 3);
            String state = palette.getCompound(block.getInt("state")).getString("Name");
            result.put(new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2)), state);
        }
        return result;
    }
}
