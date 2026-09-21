package de.wutzuket.overdrive.block.advanced_hologram;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;

public class AdvancedHologramBlockRenderer implements BlockEntityRenderer<AdvancedHologramBlockEntity> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

    public AdvancedHologramBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull AdvancedHologramBlockEntity blockEntity) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull AdvancedHologramBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(8.0D);
    }

    @Override
    public void render(@NotNull AdvancedHologramBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        List<HoloElement> elements = blockEntity.getRenderElements();
        if (elements.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        int light = LightTexture.FULL_BRIGHT;

        for (HoloElement element : elements) {
            if (element.text() != null && !element.text().isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(element.x(), element.y(), element.z());
            poseStack.mulPose(Axis.XP.rotationDegrees((float) element.rotX()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) element.rotY()));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) element.rotZ()));
            if (element.alpha() > 0.0F) {
                renderBox(consumer, poseStack.last(), element, light, packedOverlay);
            }
            poseStack.popPose();
        }

        for (HoloElement element : elements) {
            if (element.text() == null || element.text().isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(element.x(), element.y(), element.z());
            poseStack.mulPose(Axis.XP.rotationDegrees((float) element.rotX()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) element.rotY()));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) element.rotZ()));
            if (element.alpha() > 0.0F) {
                renderText(poseStack.last().pose(), buffer, element);
            }
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderText(Matrix4f matrix, MultiBufferSource buffer, HoloElement element) {
        float scale = (float) element.scale();
        float r = element.red();
        float g = element.green();
        float b = element.blue();
        float a = element.alpha();
        int argb = ((int) (a * 255.0F) << 24) | ((int) (r * 255.0F) << 16) | ((int) (g * 255.0F) << 8) | (int) (b * 255.0F);
        int light = LightTexture.FULL_BRIGHT;
        Font font = Minecraft.getInstance().font;
        float textWidth = font.width(element.text());
        float xOffset = 0.0F;
        String alignment = element.alignment();
        if ("center".equals(alignment)) {
            xOffset = -textWidth / 2.0F;
        } else if ("right".equals(alignment)) {
            xOffset = -textWidth;
        }
        Matrix4f textMatrix = new Matrix4f(matrix);
        textMatrix.scale(scale, -scale, -scale);
        font.drawInBatch(
                Component.literal(element.text()),
                xOffset, 0.0F, argb, false, textMatrix, buffer,
                Font.DisplayMode.NORMAL, 0, light);
    }

    private static void renderBox(VertexConsumer consumer, PoseStack.Pose pose, HoloElement element, int light, int overlay) {
        float r = element.red();
        float g = element.green();
        float b = element.blue();
        float a = element.alpha();
        float dx = (float) (element.sizeX() / 2.0D);
        float dy = (float) (element.sizeY() / 2.0D);
        float dz = (float) (element.sizeZ() / 2.0D);

        quad(consumer, pose, light, overlay, r, g, b, a,
                -dx, -dy, dz, dx, -dy, dz, dx, dy, dz, -dx, dy, dz,
                0.0F, 0.0F, 1.0F);
        quad(consumer, pose, light, overlay, r, g, b, a,
                dx, -dy, -dz, -dx, -dy, -dz, -dx, dy, -dz, dx, dy, -dz,
                0.0F, 0.0F, -1.0F);
        quad(consumer, pose, light, overlay, r, g, b, a,
                -dx, -dy, -dz, -dx, -dy, dz, -dx, dy, dz, -dx, dy, -dz,
                -1.0F, 0.0F, 0.0F);
        quad(consumer, pose, light, overlay, r, g, b, a,
                dx, -dy, dz, dx, -dy, -dz, dx, dy, -dz, dx, dy, dz,
                1.0F, 0.0F, 0.0F);
        quad(consumer, pose, light, overlay, r, g, b, a,
                -dx, dy, dz, dx, dy, dz, dx, dy, -dz, -dx, dy, -dz,
                0.0F, 1.0F, 0.0F);
        quad(consumer, pose, light, overlay, r, g, b, a,
                -dx, -dy, -dz, dx, -dy, -dz, dx, -dy, dz, -dx, -dy, dz,
                0.0F, -1.0F, 0.0F);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int light, int overlay,
                             float r, float g, float b, float a,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float nx, float ny, float nz) {
        Matrix4f matrix4f = pose.pose();
        consumer.addVertex(matrix4f, x1, y1, z1).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix4f, x2, y2, z2).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix4f, x3, y3, z3).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix4f, x4, y4, z4).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }
}