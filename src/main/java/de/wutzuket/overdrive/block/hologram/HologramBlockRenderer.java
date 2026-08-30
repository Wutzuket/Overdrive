package de.wutzuket.overdrive.block.hologram;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class HologramBlockRenderer implements BlockEntityRenderer<HologramBlockEntity> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
    private static final float COLOR_R = 0.35F;
    private static final float COLOR_G = 0.95F;
    private static final float COLOR_B = 1.00F;
    private static final float COLOR_A = 0.55F;

    public HologramBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull HologramBlockEntity blockEntity) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull HologramBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(4.0D);
    }

    @Override
    public void render(@NotNull HologramBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        HologramBlockEntity.HologramModel model = blockEntity.getCachedModel();
        if (model == null || model.elements().isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D + blockEntity.getOffsetX(), 0.5D + blockEntity.getOffsetY(), 0.5D + blockEntity.getOffsetZ());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) blockEntity.getRotationX()));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) blockEntity.getRotationY()));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) blockEntity.getRotationZ()));
        if (blockEntity.getSpin()) {
            float spin = (float) ((blockEntity.getLevel().getGameTime() + partialTick) * 2.0D % 360.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        }
        poseStack.scale((float) blockEntity.getScale(), (float) blockEntity.getScale(), (float) blockEntity.getScale());
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        int light = LightTexture.FULL_BRIGHT;

        for (HologramBlockEntity.HologramElement element : model.elements()) {
            poseStack.pushPose();
            if (element.rotation() != null) {
                applyElementRotation(poseStack, element.rotation());
            }
            renderElement(consumer, poseStack.last(), element, light, packedOverlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void applyElementRotation(PoseStack poseStack, HologramBlockEntity.ElementRotation rotation) {
        poseStack.translate(rotation.originX(), rotation.originY(), rotation.originZ());
        String axis = rotation.axis().toLowerCase();
        switch (axis) {
            case "x" -> poseStack.mulPose(Axis.XP.rotationDegrees(rotation.angle()));
            case "z" -> poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.angle()));
            default -> poseStack.mulPose(Axis.YP.rotationDegrees(rotation.angle()));
        }
        poseStack.translate(-rotation.originX(), -rotation.originY(), -rotation.originZ());
    }

    private static void renderElement(VertexConsumer consumer, PoseStack.Pose pose, HologramBlockEntity.HologramElement element,
                                      int light, int overlay) {
        float minX = (float) Math.min(element.fromX(), element.toX());
        float minY = (float) Math.min(element.fromY(), element.toY());
        float minZ = (float) Math.min(element.fromZ(), element.toZ());
        float maxX = (float) Math.max(element.fromX(), element.toX());
        float maxY = (float) Math.max(element.fromY(), element.toY());
        float maxZ = (float) Math.max(element.fromZ(), element.toZ());

        quad(consumer, pose, light, overlay,
                minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ,
                0.0F, 0.0F, 1.0F);
        quad(consumer, pose, light, overlay,
                maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ,
                0.0F, 0.0F, -1.0F);
        quad(consumer, pose, light, overlay,
                minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ,
                -1.0F, 0.0F, 0.0F);
        quad(consumer, pose, light, overlay,
                maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ,
                1.0F, 0.0F, 0.0F);
        quad(consumer, pose, light, overlay,
                minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ,
                0.0F, 1.0F, 0.0F);
        quad(consumer, pose, light, overlay,
                minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ,
                0.0F, -1.0F, 0.0F);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int light, int overlay,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float nx, float ny, float nz) {
        Matrix4f matrix4f = pose.pose();
        consumer.addVertex(matrix4f, x1, y1, z1).setColor(COLOR_R, COLOR_G, COLOR_B, COLOR_A).setUv(0.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix4f, x2, y2, z2).setColor(COLOR_R, COLOR_G, COLOR_B, COLOR_A).setUv(1.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix4f, x3, y3, z3).setColor(COLOR_R, COLOR_G, COLOR_B, COLOR_A).setUv(1.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix4f, x4, y4, z4).setColor(COLOR_R, COLOR_G, COLOR_B, COLOR_A).setUv(0.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
