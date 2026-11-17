package de.wutzuket.overdrive;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.wutzuket.overdrive.index.ODBlocks;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft die Werkzeugstufen der Maschinen und der Erze.
 *
 * <p>Minecraft holt sich die benoetigte Spitzhacken-Stufe aus zwei Tags:
 * {@code needs_*_tool} sagt, welche Stufe ein Block braucht, und
 * {@code incorrect_for_*_tool} sagt, welche Werkzeugstufe ihn nicht abbauen
 * darf. Beide muessen zusammenpassen, sonst faellt das Erz mit jedem
 * Spitzhacken oder gar nicht.</p>
 */
class OreToolTest {

    private static final String TAG_ROOT = "data/minecraft/tags/block/";

    /** Die Stufen der Erze: Tag der Stufe -> die Bloecke, die diese Stufe brauchen. */
    private static final List<String> TIER_TAGS = List.of(
            "needs_stone_tool", "needs_iron_tool", "needs_diamond_tool");

    /** Die Erze und ihre benoetigte Stufe. */
    private static final List<String> ORES = List.of(
            "overdrive:aluminum_ore", "overdrive:deepslate_aluminum_ore",
            "overdrive:iridium_ore", "overdrive:deepslate_iridium_ore",
            "overdrive:tungsten_ore", "overdrive:deepslate_tungsten_ore");

    @Test
    void theOresNeedACorrectTool() {
        for (var name : List.of("ALUMINUM_ORE", "DEEPSLATE_ALUMINUM_ORE", "IRIDIUM_ORE",
                "DEEPSLATE_IRIDIUM_ORE", "TUNGSTEN_ORE", "DEEPSLATE_TUNGSTEN_ORE")) {
            var block = switch (name) {
                case "ALUMINUM_ORE" -> ODBlocks.ALUMINUM_ORE.get();
                case "DEEPSLATE_ALUMINUM_ORE" -> ODBlocks.DEEPSLATE_ALUMINUM_ORE.get();
                case "IRIDIUM_ORE" -> ODBlocks.IRIDIUM_ORE.get();
                case "DEEPSLATE_IRIDIUM_ORE" -> ODBlocks.DEEPSLATE_IRIDIUM_ORE.get();
                case "TUNGSTEN_ORE" -> ODBlocks.TUNGSTEN_ORE.get();
                default -> ODBlocks.DEEPSLATE_TUNGSTEN_ORE.get();
            };
            assertTrue(block.defaultBlockState().requiresCorrectToolForDrops(),
                    name + ": das Erz muss das richtige Werkzeug brauchen");
        }
    }

    @Test
    void theMachinesDropWithEveryTool() {
        // Maschinen werden normalerweise mit dem Schraubenschluessel abgebaut.
        // Deshalb sollen sie auch mit dem falschen Werkzeug fallen.
        assertFalse(ODBlocks.CIRCUIT_PRESS.get().defaultBlockState().requiresCorrectToolForDrops(),
                "Die Presse darf kein bestimmtes Werkzeug brauchen");
        assertFalse(ODBlocks.CRYO_DISTILLER.get().defaultBlockState().requiresCorrectToolForDrops(),
                "Der Cryo Destiller darf kein bestimmtes Werkzeug brauchen");
        assertFalse(ODBlocks.COAL_GENERATOR.get().defaultBlockState().requiresCorrectToolForDrops(),
                "Das Kohlekraftwerk darf kein bestimmtes Werkzeug brauchen");
        assertFalse(ODBlocks.VACUUM_PUMP.get().defaultBlockState().requiresCorrectToolForDrops(),
                "Die Vakuumpumpe darf kein bestimmtes Werkzeug brauchen");
    }

    @Test
    void theMachinesAreNoTierBlocks() throws IOException {
        // Die Maschinen stehen in keinem Werkzeug-Tier, sonst wuerden sie beim
        // Abbauen mit dem falschen Werkzeug nicht fallen.
        var machines = List.of("overdrive:circuit_press", "overdrive:cryo_distiller",
                "overdrive:coal_generator", "overdrive:vacuum_pump", "overdrive:scanner",
                "overdrive:electron_gun", "overdrive:tungsten_casing", "overdrive:tungsten_target",
                "overdrive:positron_vacuum_output_port", "overdrive:particle_accelerator_controller",
                "overdrive:ad_controller", "overdrive:elena_controller",
                "overdrive:annihilation_controller", "overdrive:cooling_tower_controller");
        for (var tier : TIER_TAGS) {
            var values = readTag(tier);
            for (var machine : machines) {
                assertFalse(values.contains(machine), machine + ": darf nicht im Tier-Tag " + tier + " stehen");
            }
        }
    }

    @Test
    void everyOreIsMineableWithAPickaxe() throws IOException {
        var mineable = readTag("mineable/pickaxe");
        for (var ore : ORES) {
            assertTrue(mineable.contains(ore), ore + ": muss mit der Spitzhacke abbaubar sein");
        }
    }

    @Test
    void theCircuitPressIsMineableWithAPickaxe() throws IOException {
        assertTrue(readTag("mineable/pickaxe").contains("overdrive:circuit_press"),
                "Die Presse muss mit der Spitzhacke abbaubar sein");
    }

    @Test
    void everyOreHasTheCorrectToolTier() throws IOException {
        var stone = readTag("needs_stone_tool");
        var iron = readTag("needs_iron_tool");
        var diamond = readTag("needs_diamond_tool");

        assertTrue(stone.contains("overdrive:aluminum_ore"), "Aluminium braucht eine Steinspitzhacke");
        assertTrue(stone.contains("overdrive:deepslate_aluminum_ore"), "Aluminium braucht eine Steinspitzhacke");
        assertTrue(iron.contains("overdrive:iridium_ore"), "Iridium braucht eine Eisenspitzhacke");
        assertTrue(iron.contains("overdrive:deepslate_iridium_ore"), "Iridium braucht eine Eisenspitzhacke");
        assertTrue(diamond.contains("overdrive:tungsten_ore"), "Wolfram braucht eine Diamantspitzhacke");
        assertTrue(diamond.contains("overdrive:deepslate_tungsten_ore"), "Wolfram braucht eine Diamantspitzhacke");
    }

    @Test
    void theToolTiersMatchTheIncorrectToolTags() throws IOException {
        var needsStone = readTag("needs_stone_tool");
        var needsIron = readTag("needs_iron_tool");
        var needsDiamond = readTag("needs_diamond_tool");
        var incorrectWooden = readTag("incorrect_for_wooden_tool");
        var incorrectStone = readTag("incorrect_for_stone_tool");
        var incorrectIron = readTag("incorrect_for_iron_tool");

        // Ein Block, der eine Stein-Spitzhacke braucht, darf nur von der Holzstufe
        // nicht abgebaut werden.
        for (var block : needsStone) {
            if (!needsCorrectTool(block)) {
                continue;
            }
            assertTrue(incorrectWooden.contains(block), block + ": muss als 'falsch fuer Holz' gelten");
            assertFalse(incorrectStone.contains(block), block + ": darf nicht als 'falsch fuer Stein' gelten");
            assertFalse(incorrectIron.contains(block), block + ": darf nicht als 'falsch fuer Eisen' gelten");
        }
        for (var block : needsIron) {
            if (!needsCorrectTool(block)) {
                continue;
            }
            assertTrue(incorrectWooden.contains(block), block + ": muss als 'falsch fuer Holz' gelten");
            assertTrue(incorrectStone.contains(block), block + ": muss als 'falsch fuer Stein' gelten");
            assertFalse(incorrectIron.contains(block), block + ": darf nicht als 'falsch fuer Eisen' gelten");
        }
        for (var block : needsDiamond) {
            if (!needsCorrectTool(block)) {
                continue;
            }
            assertTrue(incorrectWooden.contains(block), block + ": muss als 'falsch fuer Holz' gelten");
            assertTrue(incorrectStone.contains(block), block + ": muss als 'falsch fuer Stein' gelten");
            assertTrue(incorrectIron.contains(block), block + ": muss als 'falsch fuer Eisen' gelten");
        }

        // Und umgekehrt: was als falsch fuer eine Stufe gilt, braucht auch
        // mindestens diese Stufe.
        assertFalse(incorrectStone.contains("overdrive:aluminum_ore"),
                "Aluminium darf nicht zu schwer fuer Stein sein");
        assertFalse(incorrectIron.contains("overdrive:iridium_ore"),
                "Iridium darf nicht zu schwer fuer Eisen sein");
    }

    /** Braucht der Block ueberhaupt ein bestimmtes Werkzeug? */
    private static boolean needsCorrectTool(String blockId) {
        if (!blockId.startsWith("overdrive:")) {
            return false;
        }
        var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .get(net.minecraft.resources.ResourceLocation.parse(blockId));
        return block != null && block.defaultBlockState().requiresCorrectToolForDrops();
    }

    private Set<String> readTag(String name) throws IOException {        var root = JsonParser.parseString(read(TAG_ROOT + name + ".json")).getAsJsonObject();
        Set<String> values = new LinkedHashSet<>();
        JsonArray array = root.getAsJsonArray("values");
        for (var element : array) {
            values.add(element.getAsString());
        }
        return values;
    }

    /**
     * Liest eine Datei aus dem Mod. Die Dateien werden direkt vom Dateisystem
     * gelesen: die Tags von {@code minecraft} liegen auch im Minecraft-Jar auf
     * dem Klassenpfad und der Classloader wuerde sonst die Vanilla-Datei finden.
     */
    private String read(String resource) throws IOException {
        var path = resourcesRoot().resolve(resource);
        assertTrue(java.nio.file.Files.exists(path), "Ressource fehlt: " + path);
        return java.nio.file.Files.readString(path, StandardCharsets.UTF_8);
    }

    /** Sucht den Ordner {@code src/main/resources} des Projekts. */
    private static java.nio.file.Path resourcesRoot() {
        var dir = java.nio.file.Path.of("").toAbsolutePath();
        while (dir != null) {
            var candidate = dir.resolve("src").resolve("main").resolve("resources");
            if (java.nio.file.Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("Der Ordner src/main/resources wurde nicht gefunden");
    }
}
