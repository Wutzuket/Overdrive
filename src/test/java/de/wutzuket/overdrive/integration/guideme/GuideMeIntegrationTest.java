package de.wutzuket.overdrive.integration.guideme;

import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft die GuideME-Integration:
 * <ul>
 * <li>Der Rezepttyp-Provider meldet alle eigenen Rezepttypen an.</li>
 * <li>Die Service-Datei ist vorhanden, damit GuideME den Provider findet.</li>
 * <li>Die Guide-Definition und die Seiten liegen an den erwarteten Stellen.</li>
 * </ul>
 */
class GuideMeIntegrationTest {

    @Test
    void recipeTypeProviderRegistersAllCustomRecipeTypes() {
        var registered = new ArrayList<Object>();

        RecipeTypeMappingSupplier.RecipeTypeMappings mappings = new RecipeTypeMappingSupplier.RecipeTypeMappings() {
            @Override
            public <T extends net.minecraft.world.item.crafting.Recipe<C>,
                    C extends net.minecraft.world.item.crafting.RecipeInput> void add(
                            net.minecraft.world.item.crafting.RecipeType<T> recipeType,
                            java.util.function.Function<net.minecraft.world.item.crafting.RecipeHolder<T>,
                                    guideme.document.block.LytBlock> factory) {
                registered.add(recipeType);
            }
        };

        new GuideMeRecipeTypes().collect(mappings);

        assertEquals(10, registered.size(), "Der Provider muss alle Rezepttypen melden");
        assertTrue(registered.contains(ODRecipeTypes.SCANNER.get()), "Scanner-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.CIRCUIT_PRESS.get()), "Circuit-Press-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.ANNIHILATION.get()), "Annihilations-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.COLLISION.get()), "Kollisions-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.AD_RECIPE.get()), "AD-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.ELENA_RECIPE.get()), "ELENA-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.POSITRON_EXTRACTION.get()), "Positronen-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.COOLING_TOWER.get()), "Kuehlturm-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.REACTOR_COOLING.get()), "Reaktorkuehlungs-Rezepttyp fehlt");
        assertTrue(registered.contains(ODRecipeTypes.CRYO_DISTILLER.get()), "Destillator-Rezepttyp fehlt");
    }

    @Test
    void serviceFileIsPresent() {
        var loader = getClass().getClassLoader();
        var service = loader.getResource("META-INF/services/guideme.compiler.tags.RecipeTypeMappingSupplier");
        assertNotNull(service, "Service-Datei fuer den Rezepttyp-Provider fehlt");
    }

    @Test
    void guideDefinitionAndPagesArePresent() {
        var loader = getClass().getClassLoader();

        // Das Handbuch wird im Code registriert (damit eigene Tags wie
        // <PortSlots /> moeglich sind) - die Seiten liegen aber weiterhin als
        // Ressourcen in dem Guide-Ordner.
        assertNotNull(loader.getResource("assets/overdrive/guides/overdrive/guide/index.md"),
                "Startseite fehlt");
        assertNotNull(loader.getResource("assets/overdrive/guides/overdrive/guide/_de_de/index.md"),
                "deutsche Startseite fehlt");
        assertNotNull(loader.getResource("assets/overdrive/models/item/guide.json"),
                "Modell des Handbuchs fehlt");
    }

    @Test
    void guideIsRegisteredInCodeWithThePortSlotsTag() {
        assertEquals(ResourceLocation.fromNamespaceAndPath("overdrive", "guide"), GuideMeGuide.GUIDE_ID);

        // Der eigene Tag muss zu GuideME gehoeren und die Ports der Mod kennen.
        assertEquals(java.util.Set.of("PortSlots"),
                new PortSlotsElementCompiler().getTagNames());
    }

    /**
     * Die Tooltips der Port-Slots zeigen die Namen der Ports. Die passenden
     * Uebersetzungen muessen in beiden Sprachen vorhanden sein.
     */
    @Test
    void allPortNamesUsedByThePortSlotsTooltipAreTranslated() throws java.io.IOException {
        var loader = getClass().getClassLoader();

        for (var language : List.of("en_us", "de_de")) {
            var url = loader.getResource("assets/overdrive/lang/" + language + ".json");
            assertNotNull(url, "Sprachdatei fehlt: " + language);

            String content;
            try (var in = url.openStream()) {
                content = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }

            for (var port : List.of(
                    "overdrive:vacuum_energy_port",
                    "overdrive:vacuum_output_port",
                    "overdrive:ad_energy_input_port",
                    "overdrive:ad_vacuum_port",
                    "overdrive:ad_input_port",
                    "overdrive:ad_output_port",
                    "overdrive:elena_energy_input_port",
                    "overdrive:elena_vacuum_port",
                    "overdrive:elena_input_port",
                    "overdrive:elena_output_port",
                    "overdrive:accelerator_energy_input_port",
                    "overdrive:accelerator_vacuum_input_port",
                    "overdrive:iridium_target",
                    "overdrive:accelerator_output",
                    "overdrive:cell_docking_port",
                    "overdrive:annihilation_vacuum_port",
                    "overdrive:coolant_input_port",
                    "overdrive:steam_output_port",
                    "overdrive:annihilation_energy_output")) {
                var key = "block." + port.replace(':', '.');
                assertTrue(content.contains("\"" + key + "\""),
                        language + ": Uebersetzung fehlt: " + key);
            }

            assertTrue(content.contains("\"guide.overdrive.can_be_replaced_with\""),
                    language + ": Uebersetzung fuer den Port-Tooltip fehlt");
        }
    }

    @Test
    void guideIdIsValid() {
        assertEquals(ResourceLocation.fromNamespaceAndPath("overdrive", "guide"), GuideMeGuide.GUIDE_ID);
        assertNotNull(ODBlocks.SCANNER.get());
        assertNotNull(ODItems.SCANNER.get());
    }
}
