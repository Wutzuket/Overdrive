package de.wutzuket.overdrive;

import com.google.gson.JsonParser;
import de.wutzuket.overdrive.client.sound.MachineSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft die Klange der Maschinen.
 *
 * <p>Der Client sucht den Klang einer Sound-Event ueber deren Location in der
 * {@code sounds.json}. Passt der Name dort nicht zum registrierten Sound-Event
 * oder fehlt die ogg-Datei, bleibt die Maschine stumm. Ausserdem lehnt die
 * Sound-Engine einen Klang ab, der mit der Lautstaerke null startet.</p>
 */
class MachineSoundTest {

    private static final String SOUNDS_JSON = "assets/overdrive/sounds.json";

    /** Die Sound-Events, die es fuer die Maschinen geben muss. */
    private static final List<String> MACHINE_SOUNDS = List.of(
            "machine.coal_generator",
            "machine.particle_accelerator",
            "machine.ad_ring",
            "machine.elena_ring",
            "machine.electron_gun",
            "machine.cryo_distiller",
            "machine.vacuum_pump",
            "machine.annihilation_reactor",
            "machine.cooling_tower",
            "machine.circuit_press");

    @Test
    void everyMachineSoundEventIsRegistered() {
        for (var name : MACHINE_SOUNDS) {
            var id = ResourceLocation.parse("overdrive:" + name);
            assertNotNull(BuiltInRegistries.SOUND_EVENT.get(id), "Sound-Event fehlt: " + id);
        }
    }

    @Test
    void theSoundEventsPointToTheirEntryInTheSoundsJson() {
        for (var name : MACHINE_SOUNDS) {
            var id = ResourceLocation.parse("overdrive:" + name);
            var event = BuiltInRegistries.SOUND_EVENT.get(id);
            assertNotNull(event, "Sound-Event fehlt: " + id);
            assertEquals(id, event.getLocation(),
                    "Die Location des Sound-Events muss zum Eintrag in der sounds.json passen: " + id);
        }
    }

    @Test
    void everyMachineHasAnEntryInTheSoundsJson() throws Exception {
        var root = JsonParser.parseString(read(SOUNDS_JSON)).getAsJsonObject();
        for (var name : MACHINE_SOUNDS) {
            assertTrue(root.has(name), "In der sounds.json fehlt: " + name);
        }
    }

    @Test
    void everySoundHasItsFile() throws Exception {
        var root = JsonParser.parseString(read(SOUNDS_JSON)).getAsJsonObject();
        for (var entry : root.entrySet()) {
            var sounds = entry.getValue().getAsJsonObject().getAsJsonArray("sounds");
            assertTrue(sounds.size() > 0, entry.getKey() + ": es ist kein Sound angegeben");
            for (var sound : sounds) {
                var name = sound.getAsJsonObject().get("name").getAsString();
                var id = ResourceLocation.parse(name);
                // "overdrive:machine/coal_generator" -> "assets/overdrive/sounds/machine/coal_generator.ogg"
                var path = "assets/" + id.getNamespace() + "/sounds/" + id.getPath() + ".ogg";
                assertNotNull(getClass().getClassLoader().getResource(path), "Die Sound-Datei fehlt: " + path);
            }
        }
    }

    @Test
    void theRunningSoundStartsWithAVolume() {
        var event = SoundEvent.createVariableRangeEvent(
                ResourceLocation.parse("overdrive:machine.coal_generator"));
        var instance = new MachineSoundInstance(event, new BlockPos(0, 64, 0), 1.0F, 1.0F);

        // Ohne diese Freigabe startet die Sound-Engine keinen Klang, der mit
        // der Lautstaerke null beginnt.
        assertTrue(instance.canStartSilent(),
                "Der Klang muss mit einer Lautstaerke von null starten duerfen");
        assertTrue(instance.getCurrentVolume() > 0.0F,
                "Der Klang darf nicht mit der Lautstaerke null starten");
        assertTrue(instance.isLooping(), "Der Klang der Maschine muss loopen");
    }

    private String read(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "Ressource fehlt: " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
