package de.wutzuket.overdrive.block.accelerator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class ParticleAcceleratorRenderer implements BlockEntityRenderer<ParticleAcceleratorControllerEntity> {
    private static final int OUTER_RADIUS = 25;

    public ParticleAcceleratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull ParticleAcceleratorControllerEntity blockEntity) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull ParticleAcceleratorControllerEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos).inflate(OUTER_RADIUS + 3);
    }

    @Override
    public void render(@NotNull ParticleAcceleratorControllerEntity blockEntity, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) return;
        if (!blockEntity.isHologramEnabled()) return;

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos controllerPos = blockEntity.getBlockPos();

        int cx = controllerPos.getX() + facing.getStepX() * OUTER_RADIUS;
        int cy = controllerPos.getY();
        int cz = controllerPos.getZ() + facing.getStepZ() * OUTER_RADIUS;

        int buildLayer = findBuildLayer(blockEntity, cx, cy, cz, controllerPos);
        if (buildLayer == 2) return;

        int minX = cx - OUTER_RADIUS - 2;
        int maxX = cx + OUTER_RADIUS + 2;
        int minZ = cz - OUTER_RADIUS - 2;
        int maxZ = cz + OUTER_RADIUS + 2;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x - cx + 0.5;
                double dz = z - cz + 0.5;
                double dist = Math.sqrt(dx * dx + dz * dz);

                boolean inRing = dist >= OUTER_RADIUS - 3.5 && dist <= OUTER_RADIUS + 0.5;
                if (!inRing) continue;

                int dy = buildLayer;
                boolean isFloorOrCeiling = dy == -1 || dy == 1;

                BlockPos checkPos = new BlockPos(x, cy + dy, z);
                BlockState existingState = blockEntity.getLevel().getBlockState(checkPos);

                boolean onOuterWall = dist >= OUTER_RADIUS - 0.5;
                boolean onInnerWall = dist <= OUTER_RADIUS - 2.5;

                if (isFloorOrCeiling) {
                    if (existingState.getBlock() instanceof AcceleratorCasing ||
                            existingState.getBlock() instanceof AcceleratorOutputBlock) continue;
                } else if (onOuterWall || onInnerWall) {
                    boolean alreadyPlaced = existingState.getBlock() instanceof AcceleratorCasing
                            || existingState.getBlock() instanceof AcceleratorOutputBlock
                            || existingState.getBlock() instanceof AcceleratorEnergyInputPortBlock
                            || existingState.getBlock() instanceof AcceleratorVacuumInputPortBlock
                            || existingState.getBlock() instanceof IridiumTargetBlock
                            || existingState.getBlock() instanceof ParticleAcceleratorControllerBlock;
                    if (alreadyPlaced) continue;
                } else {
                    // Tunnel interior — must be air
                    if (existingState.isAir()) continue;
                    // Wrong block in tunnel -> red
                }

                float r, g, b, a = 0.35f;
                if (isFloorOrCeiling) {
                    r = 0.0f; g = 0.8f; b = 0.3f;
                } else if (onOuterWall || onInnerWall) {
                    r = 0.3f; g = 0.6f; b = 1.0f;
                } else {
                    r = 1.0f; g = 0.0f; b = 0.0f;
                }

                double rx = checkPos.getX() - controllerPos.getX();
                double ry = checkPos.getY() - controllerPos.getY();
                double rz = checkPos.getZ() - controllerPos.getZ();

                renderGhostBox(poseStack, buffer, (float) rx, (float) ry, (float) rz, r, g, b, a);
            }
        }
    }

    private int findBuildLayer(ParticleAcceleratorControllerEntity blockEntity,
                                int cx, int cy, int cz, BlockPos controllerPos) {
        int minX = cx - OUTER_RADIUS - 2;
        int maxX = cx + OUTER_RADIUS + 2;
        int minZ = cz - OUTER_RADIUS - 2;
        int maxZ = cz + OUTER_RADIUS + 2;

        int[] dyOrder = {-1, 0, 1};

        for (int dy : dyOrder) {
            boolean layerComplete = true;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = x - cx + 0.5;
                    double dz = z - cz + 0.5;
                    double dist = Math.sqrt(dx * dx + dz * dz);

                    boolean inRing = dist >= OUTER_RADIUS - 3.5 && dist <= OUTER_RADIUS + 0.5;
                    if (!inRing) continue;

                    BlockPos checkPos = new BlockPos(x, cy + dy, z);
                    BlockState existingState = blockEntity.getLevel().getBlockState(checkPos);

                    if (dy == -1 || dy == 1) {
                        if (!(existingState.getBlock() instanceof AcceleratorCasing) &&
                                !(existingState.getBlock() instanceof AcceleratorOutputBlock)) {
                            layerComplete = false;
                        }
                    } else {
                        boolean onOuterWall = dist >= OUTER_RADIUS - 0.5;
                        boolean onInnerWall = dist <= OUTER_RADIUS - 2.5;
                        if (onOuterWall || onInnerWall) {
                            boolean alreadyPlaced = existingState.getBlock() instanceof AcceleratorCasing
                                    || existingState.getBlock() instanceof AcceleratorOutputBlock
                                    || existingState.getBlock() instanceof AcceleratorEnergyInputPortBlock
                                    || existingState.getBlock() instanceof AcceleratorVacuumInputPortBlock
                                    || existingState.getBlock() instanceof IridiumTargetBlock
                                    || existingState.getBlock() instanceof ParticleAcceleratorControllerBlock;
                            if (!alreadyPlaced) {
                                layerComplete = false;
                            }
                        } else if (!existingState.isAir()) {
                            layerComplete = false;
                        }
                    }

                    if (!layerComplete) {
                        break;
                    }
                }
                if (!layerComplete) break;
            }
            if (!layerComplete) return dy;
        }
        return 2;
    }

    private void renderGhostBox(PoseStack poseStack, MultiBufferSource buffer,
                                 float x, float y, float z, float r, float g, float b, float a) {
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
