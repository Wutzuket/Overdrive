package de.wutzuket.overdrive.integration.guideme;

import guideme.color.SymbolicColor;
import guideme.compiler.PageCompiler;
import guideme.document.LytErrorSink;
import guideme.document.interaction.TextTooltip;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.scene.GuidebookScene;
import guideme.scene.annotation.InWorldBoxAnnotation;
import guideme.scene.element.SceneElementTagCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

/**
 * Markiert in einer 3D-Szene alle Positionen, an denen ein Port stehen kann.
 *
 * <p>Der Tag ist das GegenstÃ¼ck zu den Annotationen der einzelnen Ports: Er
 * markiert die Gehaeuse-Bloecke, die durch einen Port ersetzt werden
 * koennen, mit einem gruenen Kasten und dem Hinweis
 * "Can be replaced with: ...".
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

        var tooltip = PortSlotsRules.tooltip(rule);

        for (var pos : filledBlocks(scene)) {
            if (!isBlock(level.getBlockState(pos), rule.casing())) {
                continue;
            }
            if (rule.middleLayerOnly() && pos.getY() != controllerY) {
                // Boden und Dach der Ringe und des Beschleunigers sind reine
                // Gehaeuse - dort passt kein Port.
                continue;
            }

            var annotation = InWorldBoxAnnotation.forBlock(pos, SymbolicColor.GREEN);
            annotation.setTooltip(new TextTooltip(tooltip));
            scene.addAnnotation(annotation);
        }
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
