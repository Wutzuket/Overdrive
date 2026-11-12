package de.wutzuket.overdrive.integration.guideme;

import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueft die Regeln des {@code <PortSlots />}-Tags und den Tooltip, der beim Hovern ueber einen
 * Block erscheint, der durch einen Port ersetzt werden kann.
 */
class PortSlotsRulesTest {

    @Test
    void everyCasingBelongsToItsMachine() {
        assertNotNull(PortSlotsRules.byCasing("overdrive:ad_casing"));
        assertNotNull(PortSlotsRules.byCasing("overdrive:elena_casing"));
        assertNotNull(PortSlotsRules.byCasing("overdrive:accelerator_casing"));
        assertNotNull(PortSlotsRules.byCasing("overdrive:pump_casing"));
        assertNotNull(PortSlotsRules.byCasing("overdrive:annihilation_heavy_casing"));

        assertNull(PortSlotsRules.byCasing("overdrive:ad_controller"),
                "ein Controller ist kein Gehaeuse, das ersetzt werden kann");
        assertNull(PortSlotsRules.byCasing("minecraft:stone"));
    }

    @Test
    void theControllersAreKnown() {
        for (var controller : new String[]{"overdrive:vacuum_pump", "overdrive:ad_controller",
                "overdrive:elena_controller", "overdrive:particle_accelerator_controller",
                "overdrive:annihilation_controller"}) {
            assertNotNull(PortSlotsRules.BY_CONTROLLER.get(controller), controller + " fehlt");
        }
    }

    @Test
    void portsOnlyFitOnTheMiddleLayerOfTheRings() {
        var ring = PortSlotsRules.byCasing("overdrive:ad_casing");
        assertTrue(ring.middleLayerOnly(), "der AD-Ring nimmt Ports nur in der mittleren Lage auf");
        assertTrue(PortSlotsRules.acceptsPort(ring, 1, 1), "die mittlere Lage nimmt Ports auf");
        assertFalse(PortSlotsRules.acceptsPort(ring, 1, 0), "der Boden nimmt keine Ports auf");
        assertFalse(PortSlotsRules.acceptsPort(ring, 1, 2), "das Dach nimmt keine Ports auf");

        var reactor = PortSlotsRules.byCasing("overdrive:annihilation_heavy_casing");
        assertFalse(reactor.middleLayerOnly(), "der Reaktor nimmt Ports ueberall auf");
        assertTrue(PortSlotsRules.acceptsPort(reactor, 4, 0));
        assertTrue(PortSlotsRules.acceptsPort(reactor, 4, 8));
    }

    @Test
    void theTooltipNamesTheCasingAndThePorts() {
        var rule = PortSlotsRules.byCasing("overdrive:ad_casing");
        var tooltip = PortSlotsRules.tooltip(rule);

        // Ohne geladene Sprachdatei bleibt der Translation-Key stehen, geprueft werden
        // deshalb die Argumente des Tooltips.
        assertEquals("guide.overdrive.can_be_replaced_with", tooltip.getString());

        assertTrue(tooltip.getContents() instanceof TranslatableContents, "der Tooltip ist translatable");
        var args = ((TranslatableContents) tooltip.getContents()).getArgs();
        assertEquals(2, args.length, "der Tooltip nennt das Gehaeuse und die Ports");
        assertTrue(args[0].toString().contains("ad_casing"),
                "der Tooltip muss das Gehaeuse nennen: " + args[0]);
        assertTrue(args[1].toString().contains("ad_energy_input_port"),
                "der Tooltip muss die Ports nennen: " + args[1]);
        assertTrue(args[1].toString().contains("ad_output_port"),
                "der Tooltip muss alle Ports nennen: " + args[1]);
    }

    @Test
    void everyPortOfEveryRuleHasATranslation() {
        for (var rule : PortSlotsRules.BY_CONTROLLER.values()) {
            assertFalse(rule.ports().isEmpty(), rule.controller() + " hat keine Ports");
            for (var port : rule.ports()) {
                assertNotNull(PortSlotsRules.BY_CONTROLLER.get(rule.controller()));
                assertTrue(port.startsWith("overdrive:"), port + " ist kein Block der Mod");
            }
            assertTrue(rule.casing().startsWith("overdrive:"), rule.casing() + " ist kein Block der Mod");
        }
        assertEquals(PortSlotsRules.BY_CONTROLLER.size(), PortSlotsRules.BY_CASING.size(),
                "jede Maschine hat genau ein Gehaeuse");
    }
}
