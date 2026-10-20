package de.wutzuket.overdrive.block.antimatter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.PipeSideMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * Renders the content of an antimatter pipe. The glass tube itself is drawn by the block model,
 * this renderer only adds the glowing stream of antimatter that flows through the pipe and the
 * markers for the push and pull configuration of the single sides.
 */
public class AntimatterPipeRenderer implements BlockEntityRenderer<AntimatterPipeBlockEntity> {

    /** Edge of the glowing stream inside the tube. */
    private static final float BEAM_MIN = 6.0F / 16.0F;
    /** Edge of the glowing stream inside the tube. */
    private static final float BEAM_MAX = 10.0F / 16.0F;

    public AntimatterPipeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull AntimatterPipeBlockEntity pipe, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (pipe.getLevel() == null) {
            return;
        }
        BlockState state = pipe.getBlockState();

        renderFlow(pipe, state, poseStack, buffer, packedLight);
    }

    /**
     * Renders the glowing stream of antimatter. The stream is drawn inside the glass tube, so the
     * player can see the color of the transported antimatter without the tube hiding it.
     */
    private void renderFlow(AntimatterPipeBlockEntity pipe, BlockState state, PoseStack poseStack,
                            MultiBufferSource buffer, int packedLight) {
        Antimatter type = pipe.getLocalType();
        int amount = pipe.getLocalAmount();
        if (type == null || amount <= 0) {
            return;
        }

        float fill = Math.max(0.0F, Math.min(1.0F, pipe.getNetworkFillLevel()));
        // The stream is drawn thinner the less antimatter is in the pipe, an almost empty pipe
        // only shows a thin thread instead of a completely filled tube.
        float radius = (BEAM_MAX - BEAM_MIN) / 2.0F * fill;
        if (radius <= 0.0005F) {
            return;
        }
        float center = (BEAM_MIN + BEAM_MAX) / 2.0F;
        float min = center - radius;
        float max = center + radius;

        float pulse = 0.75F + 0.25F * (float) Math.sin((pipe.getLevel().getGameTime() + pipe.getBlockPos().asLong())
                * 0.15F);

        float alpha = (0.60F + 0.40F * fill) * pulse;
        float r = type.red();
        float g = type.green();
        float b = type.blue();

        VertexConsumer consumer = buffer.getBuffer(RenderType.debugFilledBox());
        Set<Direction> open = openSides(state);

        // A straight pipe is drawn as one long stream, every other shape gets one stream per arm
        // plus a glowing core in the middle of the pipe.
        for (Direction direction : Direction.values()) {
            if (!open.contains(direction)) {
                continue;
            }
            Direction opposite = direction.getOpposite();
            if (open.contains(opposite)) {
                // both sides are connected, the stream covers the whole block
                if (direction.getAxis() == Direction.Axis.X) {
                    addBox(consumer, poseStack, 0, min, min, 1, max, max, r, g, b, alpha);
                } else if (direction.getAxis() == Direction.Axis.Y) {
                    addBox(consumer, poseStack, min, 0, min, max, 1, max, r, g, b, alpha);
                } else {
                    addBox(consumer, poseStack, min, min, 0, max, max, 1, r, g, b, alpha);
                }
            } else {
                float from = 0.5F;
                float to = 1.0F;
                switch (direction) {
                    case WEST -> addBox(consumer, poseStack, 0, min, min, from, max, max, r, g, b, alpha);
                    case EAST -> addBox(consumer, poseStack, from, min, min, 1, max, max, r, g, b, alpha);
                    case DOWN -> addBox(consumer, poseStack, min, 0, min, max, from, max, r, g, b, alpha);
                    case UP -> addBox(consumer, poseStack, min, from, min, max, 1, max, r, g, b, alpha);
                    case NORTH -> addBox(consumer, poseStack, min, min, 0, max, max, from, r, g, b, alpha);
                    case SOUTH -> addBox(consumer, poseStack, min, min, from, max, max, 1, r, g, b, alpha);
                    default -> {
                    }
                }
            }
        }

        // glowing core of the pipe
        addBox(consumer, poseStack, min, min, min, max, max, max,
                r, g, b, alpha * 0.8F);
    }

    private static void addBox(VertexConsumer consumer, PoseStack poseStack,
                               float x1, float y1, float z1, float x2, float y2, float z2,
                               float r, float g, float b, float a) {
        LevelRenderer.addChainedFilledBoxVertices(poseStack, consumer, x1, y1, z1, x2, y2, z2, r, g, b, a);
    }

    private static Set<Direction> openSides(BlockState state) {
        Set<Direction> open = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            PipeConnection connection = state.getValue(AntimatterPipeBlock.CONNECTIONS.get(direction));
            if (connection == PipeConnection.OPEN || connection == PipeConnection.PULL
                    || connection == PipeConnection.PUSH) {
                open.add(direction);
            }
        }
        return open;
    }
}
