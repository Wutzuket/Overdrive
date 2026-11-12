package de.wutzuket.overdrive.integration.guideme;

import guideme.compiler.PageCompiler;
import guideme.document.LytErrorSink;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.scene.GuidebookScene;
import guideme.scene.element.SceneElementTagCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

/**
 * Prueft, ob die Szene zu der Maschine passt, und merkt sich die Lage des
 * Controllers.
 *
 * <p>Die Positionen, an denen ein Port stehen kann, werden erst beim Hovern
 * ueber den Block angezeigt: {@link PortSlotsAnnotationStrategy} zeichnet
 * dann einen gruenen Kasten und nennt die Ports, die dort hinpassen. Ohne den
 * Hovern wuerde die Szene dauerhaft gruen markiert.
 *
 * <p>Aufruf in einer Handbuchseite:
 *
 * <pre>
 * &lt;GameScene&gt;
 *     &lt;ImportStructure src="../structures/ad_ring.snbt" /&gt;
 *     &lt;PortSlots controller="overdrive:ad_controller" /&gt;
 * &lt;/GameScene&gt;
 * </pre>
 *
 * <p>Die Regeln sind direkt aus den Strukturpruefungen der Mod abgeleitet:
 * <ul>
 * <li>AD-Ring, ELENA-Ring und Beschleuniger: nur die Wand der mittleren Lage
 *     kann Ports aufnehmen, der Boden und das Dach bleiben Gehaeuse.</li>
 * <li>Vakuumpumpe: jeder Block des 3x3x3-Wuerfels kann ein Port werden.</li>
 * <li>Annihilationsreaktor: jeder Block der Toruswand kann ein Port werden.</li>
 * </ul>
 */
public class PortSlotsElementCompiler implements SceneElementTagCompiler {

    @Override
    public Set<String> getTagNames() {
        return Set.of("PortSlots");
    }

    @Override
    public void compile(GuidebookScene scene, PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el) {
        var controllerId = el.getAttributeString("controller", "");
        if (controllerId.isEmpty()) {
            errorSink.appendError(compiler, "Missing controller attribute", el);
            return;
        }

        var rule = PortSlotsRules.BY_CONTROLLER.get(controllerId);
        if (rule == null) {
            errorSink.appendError(compiler, "Unknown controller: " + controllerId, el);
            return;
        }

        var level = scene.getLevel();
        var blocks = filledBlocks(scene);

        // Position des Controllers suchen. Er liegt in der mittleren Lage und
        // damit in der Ebene, in der Ports stehen duerfen.
        Integer controllerY = null;
        for (var pos : blocks) {
            if (isBlock(level.getBlockState(pos), controllerId)) {
                controllerY = pos.getY();
                break;
            }
        }
        if (controllerY == null) {
            errorSink.appendError(compiler, "Controller " + controllerId + " not found in the scene", el);
            return;
        }

        // Die gruenen Kaesten der Port-Plaetze wuerden die Szene dauerhaft markieren.
        // Sie werden nur beim Hovern ueber den Block gezeigt, die Anzeige uebernimmt
        // PortSlotsAnnotationStrategy.
    }

    /**
     * Liefert die Positionen aller Bloecke der Szene.
     *
     * <p>GuideME gibt die gefuellten Positionen als {@code Stream<BlockPos>} zurueck, wobei
     * alle Eintraege dieselbe, veraenderliche {@code BlockPos}-Instanz teilen. Am Ende der
     * Iteration zeigt damit jeder Eintrag auf die letzte Position. Die Positionen werden
     * deshalb hier kopiert, sonst wuerde der Controller nur gefunden, wenn er zufaellig der
     * letzte Block der Struktur ist.</p>
     */
    static List<BlockPos> filledBlocks(GuidebookScene scene) {
        return copyPositions(scene.getFilledBlocks());
    }

    /**
     * Kopiert die Positionen eines Streams, damit jeder Eintrag eine eigene Instanz bekommt.
     */
    static List<BlockPos> copyPositions(java.util.stream.Stream<BlockPos> positions) {
        return positions.map(BlockPos::immutable).toList();
    }

    private static boolean isBlock(BlockState state, String id) {
        var key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null && key.toString().equals(id);
    }
}
