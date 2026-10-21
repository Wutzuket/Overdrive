package de.wutzuket.overdrive.block.scanner;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;



import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe_manager;

public class scanner_renderer implements BlockEntityRenderer<scanner_blockentity> {
    public scanner_renderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull scanner_blockentity blockEntity) {
        return false;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull scanner_blockentity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos).inflate(6);
    }

    @Override
    public void render(@NotNull scanner_blockentity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        Direction scannerFacing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
        Direction patternFacing = blockEntity.getMatchedFacing() != null ? blockEntity.getMatchedFacing() : scannerFacing;
        Vec3i facingOffset = scannerFacing.getNormal();
        int x = facingOffset.getX() * 3;
        int y = facingOffset.getY() * 3 + 1;
        int z = facingOffset.getZ() * 3;
        if (blockEntity.isAnimating() && blockEntity.getMatchedRecipeId() != null) {
            scanner_recipe recipe = scanner_recipe_manager.getRecipe(blockEntity.getLevel(), blockEntity.getMatchedRecipeId());
            if (recipe == null) {
                return;
            }
            renderAnimation(poseStack, buffer, blockEntity, recipe, x, y, z, patternFacing, facingOffset, partialTick, packedLight, packedOverlay);
        } else {
            boolean matched = blockEntity.getMatchedRecipeId() != null;
            renderBox(poseStack, buffer, x, y, z, matched);
        }
    }

    private void renderBox(PoseStack poseStack, MultiBufferSource buffer, int centerX, int centerY, int centerZ, boolean matched) {
        float minX = centerX - 1.0F;
        float minY = centerY - 1.0F;
        float minZ = centerZ - 1.0F;
        float maxX = centerX + 2.0F;
        float maxY = centerY + 2.0F;
        float maxZ = centerZ + 2.0F;
        float r = matched ? 0.15F : 1.0F;
        float g = matched ? 1.0F : 0.45F;
        float b = matched ? 0.15F : 0.0F;
        float a = matched ? 0.30F : 0.25F;
        float t = 0.08F;

        edgeX(buffer, poseStack, minX, minY, minZ, maxX, t, r, g, b, a);
        edgeX(buffer, poseStack, minX, minY, maxZ, maxX, t, r, g, b, a);
        edgeZ(buffer, poseStack, minX, minY, minZ, maxZ, t, r, g, b, a);
        edgeZ(buffer, poseStack, maxX, minY, minZ, maxZ, t, r, g, b, a);

        edgeX(buffer, poseStack, minX, maxY, minZ, maxX, t, r, g, b, a);
        edgeX(buffer, poseStack, minX, maxY, maxZ, maxX, t, r, g, b, a);
        edgeZ(buffer, poseStack, minX, maxY, minZ, maxZ, t, r, g, b, a);
        edgeZ(buffer, poseStack, maxX, maxY, minZ, maxZ, t, r, g, b, a);

        edgeY(buffer, poseStack, minX, minY, minZ, maxY, t, r, g, b, a);
        edgeY(buffer, poseStack, maxX, minY, minZ, maxY, t, r, g, b, a);
        edgeY(buffer, poseStack, maxX, minY, maxZ, maxY, t, r, g, b, a);
        edgeY(buffer, poseStack, minX, minY, maxZ, maxY, t, r, g, b, a);
    }

    private void edgeX(MultiBufferSource buffer, PoseStack poseStack, float x1, float y, float z, float x2, float t, float r, float g, float b, float a) {
        LevelRenderer.addChainedFilledBoxVertices(poseStack, buffer.getBuffer(RenderType.debugFilledBox()),
                Math.min(x1, x2), y - t, z - t, Math.max(x1, x2), y + t, z + t, r, g, b, a);
    }

    private void edgeY(MultiBufferSource buffer, PoseStack poseStack, float x, float y1, float z, float y2, float t, float r, float g, float b, float a) {
        LevelRenderer.addChainedFilledBoxVertices(poseStack, buffer.getBuffer(RenderType.debugFilledBox()),
                x - t, Math.min(y1, y2), z - t, x + t, Math.max(y1, y2), z + t, r, g, b, a);
    }

    private void edgeZ(MultiBufferSource buffer, PoseStack poseStack, float x, float y, float z1, float z2, float t, float r, float g, float b, float a) {
        LevelRenderer.addChainedFilledBoxVertices(poseStack, buffer.getBuffer(RenderType.debugFilledBox()),
                x - t, y - t, Math.min(z1, z2), x + t, y + t, Math.max(z1, z2), r, g, b, a);
    }

    private void renderAnimation(PoseStack poseStack, MultiBufferSource buffer, scanner_blockentity blockEntity, scanner_recipe recipe,
                                 int centerX, int centerY, int centerZ, Direction patternFacing, Vec3i scannerFacingOffset,
                                 float partialTick, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
        int animationLight = 15728880;

        float totalProgress = blockEntity.getAnimationProgress(partialTick);
        float movementEndTarget = 0.8F;
        float moveProgress = Math.min(1.0F, totalProgress / movementEndTarget);
        float eased = moveProgress * moveProgress * (3.0F - 2.0F * moveProgress);

        BlockPos scannerPos = blockEntity.getBlockPos();
        boolean mirrored = blockEntity.isMatchedMirrored();

        double targetCenterX = centerX + 0.5D;
        double targetCenterY = centerY + 0.5D;
        double targetCenterZ = centerZ + 0.5D;

        float minScale = 1.0F / 6.0F;
        float currentScale = 1.0F - (eased * (1.0F - minScale));

        for (CompoundTag cellTag : blockEntity.getAnimationSnapshot()) {
            int layer = cellTag.getInt("Layer");
            int row = cellTag.getInt("Row");
            int col = cellTag.getInt("Col");

            BlockPos cellPos = recipe.getCellPos(scannerPos, blockEntity.getBlockState(), layer, row, col, mirrored, patternFacing);
            ResourceLocation blockId = ResourceLocation.parse(cellTag.getString("BlockId"));
            BlockState cellState = BuiltInRegistries.BLOCK.getOptional(blockId).orElseThrow().defaultBlockState();

            double originX = (cellPos.getX() - scannerPos.getX()) + 0.5D;
            double originY = (cellPos.getY() - scannerPos.getY()) + 0.5D;
            double originZ = (cellPos.getZ() - scannerPos.getZ()) + 0.5D;

            double offsetX = (originX - targetCenterX) * currentScale;
            double offsetY = (originY - targetCenterY) * currentScale;
            double offsetZ = (originZ - targetCenterZ) * currentScale;

            double moveX = targetCenterX + offsetX;
            double moveY = targetCenterY + offsetY;
            double moveZ = targetCenterZ + offsetZ;

            poseStack.pushPose();
            poseStack.translate(moveX - 0.5D, moveY - 0.5D, moveZ - 0.5D);
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(currentScale, currentScale, currentScale);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            blockRender.renderSingleBlock(cellState, poseStack, buffer, animationLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
