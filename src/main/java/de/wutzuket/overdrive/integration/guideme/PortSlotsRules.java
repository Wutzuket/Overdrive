package de.wutzuket.overdrive.integration.guideme;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Regeln des {@code <PortSlots />}-Tags: welcher Controller zu welcher Maschine gehoert,
 * welches Gehaeuse die Ports ersetzen und welche Ports dort hin passen.
 */
final class PortSlotsRules {

    /** Regel je Controller: Gehaeuse, moegliche Ports und ob nur die mittlere Lage gilt. */
    static final Map<String, Rule> BY_CONTROLLER = new LinkedHashMap<>();

    /** Regel je Gehaeuse, damit der Block beim Hovern der Szene gefunden wird. */
    static final Map<String, Rule> BY_CASING = new LinkedHashMap<>();

    static {
        register("overdrive:vacuum_pump", new Rule(
                "overdrive:vacuum_pump",
                "overdrive:pump_casing",
                List.of("overdrive:vacuum_energy_port", "overdrive:vacuum_output_port"),
                false));
        register("overdrive:ad_controller", new Rule(
                "overdrive:ad_controller",
                "overdrive:ad_casing",
                List.of("overdrive:ad_energy_input_port", "overdrive:ad_vacuum_port",
                        "overdrive:ad_input_port", "overdrive:ad_output_port"),
                true));
        register("overdrive:elena_controller", new Rule(
                "overdrive:elena_controller",
                "overdrive:elena_casing",
                List.of("overdrive:elena_energy_input_port", "overdrive:elena_vacuum_port",
                        "overdrive:elena_input_port", "overdrive:elena_output_port"),
                true));
        register("overdrive:particle_accelerator_controller", new Rule(
                "overdrive:particle_accelerator_controller",
                "overdrive:accelerator_casing",
                List.of("overdrive:accelerator_energy_input_port", "overdrive:accelerator_vacuum_input_port",
                        "overdrive:iridium_target", "overdrive:accelerator_output"),
                true));
        register("overdrive:annihilation_controller", new Rule(
                "overdrive:annihilation_controller",
                "overdrive:annihilation_heavy_casing",
                List.of("overdrive:cell_docking_port", "overdrive:annihilation_vacuum_port",
                        "overdrive:coolant_input_port", "overdrive:steam_output_port",
                        "overdrive:annihilation_energy_output"),
                false));
    }

    private PortSlotsRules() {
    }

    private static void register(String controller, Rule rule) {
        BY_CONTROLLER.put(controller, rule);
        BY_CASING.put(rule.casing(), rule);
    }

    /**
     * @param blockId die Id des Blocks, zum Beispiel {@code overdrive:ad_casing}
     * @return die Regel des Gehaeuse oder null, wenn an dem Block kein Port hin passt
     */
    static Rule byCasing(String blockId) {
        return BY_CASING.get(blockId);
    }

    /** Baut den Tooltip "AD Casing - Can be replaced with: ...". */
    static Component tooltip(Rule rule) {
        var names = new ArrayList<String>(rule.ports().size());
        for (var port : rule.ports()) {
            names.add(blockName(port));
        }
        return Component.translatable("guide.overdrive.can_be_replaced_with",
                blockName(rule.casing()), String.join(", ", names));
    }

    /** Uebersetzter Name eines Blocks, zum Beispiel "AD Casing". */
    static String blockName(String id) {
        return Component.translatable("block." + id.replace(':', '.')).getString();
    }

    /**
     * Ob an einer Position ein Port hin passt.
     *
     * @param rule        die Regel der Maschine
     * @param controllerY die Hoehe, in der der Controller steht
     * @param blockY      die Hoehe des Blockes
     * @return true, wenn dort ein Port hin passt
     */
    static boolean acceptsPort(Rule rule, int controllerY, int blockY) {
        return !rule.middleLayerOnly() || blockY == controllerY;
    }

    /** Die Id des Blocks, damit die Regeln ohne die Registry geprueft werden koennen. */
    static String blockId(net.minecraft.world.level.block.state.BlockState state) {
        var key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key == null ? null : key.toString();
    }

    /**
     * Die Regel einer Maschine.
     *
     * @param controller       der Controller der Maschine
     * @param casing           das Gehaeuse, das durch einen Port ersetzt werden kann
     * @param ports            die Ports, die dort hin passen
     * @param middleLayerOnly  ob nur die mittlere Lage Ports aufnehmen kann
     */
    record Rule(String controller, String casing, List<String> ports, boolean middleLayerOnly) {
    }
}
