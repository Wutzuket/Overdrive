package de.wutzuket.overdrive;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft die Modelle der Ports.
 *
 * <p>Ein Port muss an allen Seiten als Port zu erkennen sein. Ein Modell, das die Textur nur
 * auf eine Seite legt, zeigt den Port von den anderen Seiten wie normales Gehaeuse.</p>
 */
class PortModelTest {

    private static final String MODEL_ROOT = "assets/overdrive/models/block/";

    /** Modelle, die zu einem Port gehoeren, aber kein "port" im Namen haben. */
    private static final List<String> EXTRA_PORT_MODELS = List.of(
            "accelerator/accelerator_output.json",
            "annihilation/annihilation_energy_output.json");

    @Test
    void everyPortModelUsesItsTextureOnAllSides() throws Exception {
        for (var model : portModels()) {
            var content = read(model);

            // Die Textur muss auf allen Seiten liegen, nicht nur auf einer.
            assertTrue(content.contains("\"all\""),
                    model + ": die Port-Textur muss auf allen Seiten liegen");

            // Und keine Seite darf eine eigene Textur bekommen.
            for (var side : List.of("north", "south", "east", "west", "up", "down")) {
                assertTrue(!content.contains("\"" + side + "\":"),
                        model + ": die Seite " + side + " hat eine eigene Textur");
            }
        }
    }

    @Test
    void everyPortModelHasItsTexture() throws Exception {
        var pattern = Pattern.compile("\"all\"\\s*:\\s*\"overdrive:block/([a-z0-9_]+)\"");
        for (var model : portModels()) {
            var content = read(model);
            var matcher = pattern.matcher(content);
            assertTrue(matcher.find(), model + ": keine Port-Textur gefunden");

            var texture = matcher.group(1);
            assertTrue(textureExists(texture),
                    model + ": die Textur overdrive:block/" + texture + " fehlt");
        }
    }

    private List<String> portModels() throws Exception {
        var models = new ArrayList<String>();
        for (var extra : EXTRA_PORT_MODELS) {
            models.add(extra);
        }
        // Die Modelle mit "port" im Namen liegen in verschiedenen Ordnern.
        for (var folder : List.of("ad", "elena", "vacuum", "accelerator", "annihilation",
                "cooling_tower", "tungsten")) {
            var dir = getClass().getClassLoader().getResource(MODEL_ROOT + folder);
            if (dir == null) {
                continue;
            }
            try (var files = java.nio.file.Files.list(java.nio.file.Path.of(dir.toURI()))) {                files.filter(path -> path.getFileName().toString().endsWith(".json"))
                        .filter(path -> path.getFileName().toString().contains("port"))
                        .forEach(path -> models.add(folder + "/" + path.getFileName()));
            }
        }
        assertTrue(models.size() >= 19, "es werden mindestens 19 Port-Modelle erwartet, gefunden: " + models.size());
        return models;
    }

    private String read(String model) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(MODEL_ROOT + model)) {
            assertTrue(in != null, "Modell fehlt: " + model);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private boolean textureExists(String name) {
        return getClass().getClassLoader()
                .getResource("assets/overdrive/textures/block/" + name + ".png") != null;
    }
}
