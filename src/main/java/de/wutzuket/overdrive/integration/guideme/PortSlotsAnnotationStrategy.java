package de.wutzuket.overdrive.integration.guideme;

import guideme.color.SymbolicColor;
import guideme.scene.ImplicitAnnotationStrategy;
import guideme.scene.annotation.InWorldBoxAnnotation;
import guideme.scene.annotation.SceneAnnotation;
import guideme.scene.level.GuidebookLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Zeigt beim Hovern ueber einen Block, der durch einen Port ersetzt werden kann, welche Ports
 * dort hin passen.
 *
 * <p>GuideME erzeugt den Tooltip einer Szene selbst aus dem Namen des Blockes, unter dem der
 * Cursor liegt. Die Annotationen des {@code <PortSlots />}-Tags werden dabei nicht ausgewertet,
 * sie markieren nur die Stellen. Damit der Tooltip der Szene die Ports nennt, muss die Mod eine
 * {@link ImplicitAnnotationStrategy} registrieren, die GuideME vor dem Block-Namen fragt.</p>
 */
public class PortSlotsAnnotationStrategy implements ImplicitAnnotationStrategy {

    /**
     * Die Lage des Controllers je Szene. Die Suche einmal pro Szene zu machen ist wichtig,
     * sonst wuerde sie bei jedem Hover wiederholt.
     */
    private static final Map<GuidebookLevel, Map<String, Integer>> CONTROLLER_LEVELS =
            Collections.synchronizedMap(new WeakHashMap<>());

    /** Wert fuer einen Controller, der in der Szene nicht gefunden wurde. */
    private static final int NOT_FOUND = Integer.MIN_VALUE;

    @Override
    public @Nullable SceneAnnotation getAnnotation(GuidebookLevel level, BlockState state,
                                                   BlockHitResult hitResult) {
        var rule = PortSlotsRules.byCasing(PortSlotsRules.blockId(state));
        if (rule == null) {
            return null;
        }

        var pos = hitResult.getBlockPos();
        if (!PortSlotsRules.acceptsPort(rule, controllerLevel(level, rule), pos.getY())) {
            // Boden und Dach der Ringe bleiben Gehaeuse, dort passt kein Port hin.
            return null;
        }

        var annotation = InWorldBoxAnnotation.forBlock(pos, SymbolicColor.GREEN);
        annotation.setTooltip(PortSlotsRules.tooltip(rule));
        return annotation;
    }

    /**
     * @return die Hoehe der Lage, in der der Controller steht, damit die Ports dort
     *         aufgenommen werden koennen
     */
    private int controllerLevel(GuidebookLevel level, PortSlotsRules.Rule rule) {
        var levels = CONTROLLER_LEVELS.computeIfAbsent(level, key -> new HashMap<>());
        synchronized (levels) {
            return levels.computeIfAbsent(rule.controller(), id -> findControllerLevel(level, id));
        }
    }

    /** Sucht den Controller in der Szene und merkt sich seine Hoehe. */
    private int findControllerLevel(GuidebookLevel level, String controllerId) {
        for (var pos : PortSlotsElementCompiler.copyPositions(level.getFilledBlocks())) {
            var id = PortSlotsRules.blockId(level.getBlockState(pos));
            if (controllerId.equals(id)) {
                return pos.getY();
            }
        }
        return NOT_FOUND;
    }
}
