package de.wutzuket.overdrive.block.tungsten;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class TungstenTargetRenderer implements BlockEntityRenderer<BlockEntity> {
    public TungstenTargetRenderer(BlockEntityRendererProvider.Context context) {
    }

    private boolean isHologramEnabled(BlockEntity blockEntity) {
        if (blockEntity instanceof ElectronGunEntity gun) return gun.isHologramEnabled();
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull BlockEntity blockEntity) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos).inflate(4);
    }

    @Override
    public void render(@NotNull BlockEntity blockEntity, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) return;
        if (!isHologramEnabled(blockEntity)) return;

        BlockState state = blockEntity.getBlockState();
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return;
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos origin = blockEntity.getBlockPos();

        // Position 1: Tungsten Target (green)
        BlockPos targetPos = origin.relative(facing, 1);
        BlockState targetState = blockEntity.getLevel().getBlockState(targetPos);
        if (targetState.isAir()) {
            renderGhostBox(poseStack, buffer, targetPos, origin, 0.0f, 0.8f, 0.3f, 0.35f);
        }

        // Position 2: Output Port (blue)
        BlockPos outputPos = origin.relative(facing, 2);
        BlockState outputState = blockEntity.getLevel().getBlockState(outputPos);
        if (outputState.isAir()) {
            renderGhostBox(poseStack, buffer, outputPos, origin, 0.3f, 0.6f, 1.0f, 0.35f);
        }
    }

    private void renderGhostBox(PoseStack poseStack, MultiBufferSource buffer,
                                 BlockPos target, BlockPos origin,
                                 float r, float g, float b, float a) {
        float x = (float) (target.getX() - origin.getX());
        float y = (float) (target.getY() - origin.getY());
        float z = (float) (target.getZ() - origin.getZ());

        float t = 0.04f;
        float minX = x, minY = y, minZ = z;
        float maxX = x + 1.0f, maxY = y + 1.0f, maxZ = z + 1.0f;

        VertexConsumer consumer = buffer.getBuffer(RenderType.debugFilledBox());

        edgeX(poseStack, consumer, minX, minY, minZ, maxX, t, r, g, b, a);
        edgeX(poseStack, consumer, minX, minY, maxZ, maxX, t, r, g, b, a);
        edgeZ(poseStack, consumer, minX, minY, minZ, maxZ, t, r, g, b, a);
        edgeZ(poseStack, consumer, maxX, minY, minZ, maxZ, t, r, g, b, a);

        edgeX(poseStack, consumer, minX, maxY, minZ, maxX, t, r, g, b, a);
        edgeX(poseStack, consumer, minX, maxY, maxZ, maxX, t, r, g, b, a);
        edgeZ(poseStack, consumer, minX, maxY, minZ, maxZ, t, r, g, b, a);
        edgeZ(poseStack, consumer, maxX, maxY, minZ, maxZ, t, r, g, b, a);

        edgeY(poseStack, consumer, minX, minY, minZ, maxY, t, r, g, b, a);
        edgeY(poseStack, consumer, maxX, minY, minZ, maxY, t, r, g, b, a);
        edgeY(poseStack, consumer, maxX, minY, maxZ, maxY, t, r, g, b, a);
        edgeY(poseStack, consumer, minX, minY, maxZ, maxY, t, r, g, b, a);
    }

    private static void edgeX(PoseStack poseStack, VertexConsumer consumer,
                               float x1, float y, float z, float x2, float t,
                               float r, float g, float b, float a) {
        LevelRenderer.addChainedFilledBoxVertices(poseStack, consumer,
                Math.min(x1, x2), y - t, z - t,
                Math.max(x1, x2), y + t, z + t,
                r, g, b, a);
    }

    private static void edgeY(PoseStack poseStack, VertexConsumer consumer,
                               float x, float y1, float z, float y2, float t,
                               float r, float g, float b, float a) {
        LevelRenderer.addChainedFilledBoxVertices(poseStack, consumer,
                x - t, Math.min(y1, y2), z - t,
                x + t, Math.max(y1, y2), z + t,
                r, g, b, a);
    }

    private static void edgeZ(PoseStack poseStack, VertexConsumer consumer,
                               float x, float y, float z1, float z2, float t,
                               float r, float g, float b, float a) {
        LevelRenderer.addChainedFilledBoxVertices(poseStack, consumer,
                x - t, y - t, Math.min(z1, z2),
                x + t, y + t, Math.max(z1, z2),
                r, g, b, a);
    }
}
