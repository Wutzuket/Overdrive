package de.wutzuket.overdrive.block.vacuum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class VacuumPumpRenderer implements BlockEntityRenderer<VacuumPumpBlockEntity> {
    public VacuumPumpRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull VacuumPumpBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) return;
        if (!blockEntity.isHologramEnabled() || blockEntity.isValidStructure()) return;

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction back = facing.getOpposite();

        VertexConsumer consumer = buffer.getBuffer(RenderType.debugFilledBox());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    double x = dx;
                    double y = dy;
                    double z = dz;
                    if (back == Direction.NORTH) z = -dz;
                    else if (back == Direction.SOUTH) z = dz;
                    else if (back == Direction.WEST) { double tmp = x; x = -dz; z = tmp; }
                    else if (back == Direction.EAST) { double tmp = x; x = dz; z = tmp; }

                    float minX = (float) x;
                    float minY = (float) y;
                    float minZ = (float) z;
                    float maxX = minX + 1.0F;
                    float maxY = minY + 1.0F;
                    float maxZ = minZ + 1.0F;

                    float r = 0.0F;
                    float g = 0.8F;
                    float b = 0.3F;
                    float a = 0.2F;

                    LevelRenderer.addChainedFilledBoxVertices(poseStack, consumer,
                            minX, minY, minZ, maxX, maxY, maxZ, r, g, b, a);
                }
            }
        }
    }
}
