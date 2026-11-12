package de.wutzuket.overdrive.integration.guideme;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft, dass die Ports einer Maschine in den Handbook-Szenen zusammen stehen.
 *
 * <p>Die Ports einer Maschine muessen an einer Stelle stehen, damit man auf
 * einen Blick sieht, wo sie hinpassen. Beim Annihilationsreaktor stehen sie
 * zusaetzlich auf der obersten Lage des Torus.</p>
 */
class PortGroupingTest {

    private static final String STRUCTURE_PATH =
            "assets/overdrive/guides/overdrive/guide/structures/";

    /** Struktur -> Controller der Maschine. */
    private static final Map<String, String> MACHINES = new LinkedHashMap<>();

    static {
        MACHINES.put("ad_ring.snbt", "overdrive:ad_controller");
        MACHINES.put("elena_ring.snbt", "overdrive:elena_controller");
        MACHINES.put("particle_accelerator.snbt", "overdrive:particle_accelerator_controller");
        MACHINES.put("annihilation_reactor.snbt", "overdrive:annihilation_controller");
        MACHINES.put("vacuum_pump.snbt", "overdrive:vacuum_pump");
    }

    @Test
    void thePortsOfEveryMachineStandNextToEachOther() throws Exception {
        for (var entry : MACHINES.entrySet()) {
            var rule = PortSlotsRules.BY_CONTROLLER.get(entry.getValue());
            assertTrue(rule != null, entry.getValue() + " fehlt");

            var ports = portPositions(entry.getKey(), rule.ports());
            assertTrue(ports.size() >= rule.ports().size(),
                    entry.getKey() + ": nicht alle Ports sind in der Struktur");

            // Die Ports liegen alle in einer Reihe: gleiche Hoehe und entweder gleiche
            // Tiefe oder gleiche Spalte.
            int y = ports.getFirst().getY();
            for (var port : ports) {
                assertEquals(y, port.getY(),
                        entry.getKey() + ": die Ports liegen nicht in einer Reihe: " + port);
            }
            boolean sameColumn = ports.stream().allMatch(p -> p.getX() == ports.getFirst().getX());
            boolean sameRow = ports.stream().allMatch(p -> p.getZ() == ports.getFirst().getZ());
            assertTrue(sameColumn || sameRow,
                    entry.getKey() + ": die Ports liegen nicht in einer Reihe");

            // Und die Reihe ist eng: zwischen dem ersten und dem letzten Port
            // liegen hoechstens so viele Luecken, wie es Ports gibt.
            int span = sameColumn
                    ? ports.stream().mapToInt(p -> p.getZ()).max().getAsInt()
                            - ports.stream().mapToInt(p -> p.getZ()).min().getAsInt()
                    : ports.stream().mapToInt(p -> p.getX()).max().getAsInt()
                            - ports.stream().mapToInt(p -> p.getX()).min().getAsInt();
            assertTrue(span <= ports.size(),
                    entry.getKey() + ": die Ports stehen zu weit auseinander (" + span + ")");
        }
    }

    @Test
    void thePortsOfTheReactorStandOnTheTopLayer() throws Exception {
        var rule = PortSlotsRules.BY_CONTROLLER.get("overdrive:annihilation_controller");
        var ports = portPositions("annihilation_reactor.snbt", rule.ports());

        int top = ports.getFirst().getY();
        for (var port : ports) {
            assertEquals(top, port.getY(),
                    "die Reaktor-Ports muessen alle auf der obersten Lage liegen: " + port);
        }

        // Und die Ports sind eine Linie auf dieser Lage.
        for (int i = 1; i < ports.size(); i++) {
            assertEquals(ports.get(i - 1).getX() + 1, ports.get(i).getX(),
                    "die Reaktor-Ports muessen nebeneinander in einer Linie stehen");
            assertEquals(ports.get(i - 1).getZ(), ports.get(i).getZ(),
                    "die Reaktor-Ports muessen nebeneinander in einer Linie stehen");
        }
    }

    /**
     * Die Positionen der Ports einer Struktur. Ein Port kann mehrfach vorkommen
     * (der Reaktor braucht zwei Cell Docking Ports), darum werden alle
     * Vorkommen gesammelt.
     */
    private List<BlockPos> portPositions(String structure, List<String> portIds) throws Exception {
        var blocks = readStructure(structure);
        var positions = new ArrayList<BlockPos>();
        for (var portId : portIds) {
            var found = new ArrayList<BlockPos>();
            for (var entry : blocks.entrySet()) {
                if (entry.getValue().equals(portId)) {
                    found.add(entry.getKey());
                }
            }
            assertTrue(!found.isEmpty(), structure + ": der Port " + portId + " fehlt");
            positions.addAll(found);
        }
        // Die Ports liegen nebeneinander, die Reihenfolge der Liste ist aber
        // die der Regeln und nicht die der Struktur.
        positions.sort((a, b) -> {
            if (a.getY() != b.getY()) {
                return Integer.compare(a.getY(), b.getY());
            }
            if (a.getZ() != b.getZ()) {
                return Integer.compare(a.getZ(), b.getZ());
            }
            return Integer.compare(a.getX(), b.getX());
        });
        return positions;
    }

    private Map<BlockPos, String> readStructure(String name) throws Exception {
        String text;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(STRUCTURE_PATH + name)) {
            assertTrue(in != null, "Struktur fehlt: " + name);
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
