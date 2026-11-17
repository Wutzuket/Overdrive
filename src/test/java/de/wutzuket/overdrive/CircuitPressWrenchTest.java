package de.wutzuket.overdrive;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft, dass sich die Circuit Press abbauen laesst.
 *
 * <p>Zum einen braucht sie eine Loot-Table, damit sie beim Zerstoeren und beim
 * Abbauen mit dem Schraubenschluessel fallen kann. Zum anderen muss sie im Tag
 * der Bloecke sein, die der Schraubenschluessel einsammelt.</p>
 */
class CircuitPressWrenchTest {

    private static final String LOOT_TABLE = "data/overdrive/loot_table/blocks/circuit_press.json";
    private static final String WRENCHABLE_TAG = "data/overdrive/tags/block/wrenchable.json";

    @Test
    void theCircuitPressHasALootTable() throws Exception {
        var content = read(LOOT_TABLE);
        assertTrue(content.contains("\"overdrive:circuit_press\""),
                "Die Loot-Table muss die Circuit Press fallen lassen");
    }

    @Test
    void theCircuitPressCanBeTakenWithTheWrench() throws Exception {
        var content = read(WRENCHABLE_TAG);
        assertTrue(content.contains("\"overdrive:circuit_press\""),
                "Die Circuit Press muss im Tag der Schraubenschluessel-Bloecke sein");
    }

    private String read(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "Ressource fehlt: " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
