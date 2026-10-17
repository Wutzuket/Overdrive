package de.wutzuket.overdrive.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class jei_block_render_helper {
    private static final int FULL_BRIGHT = 15728880;
    private static final float ROTATION_X = 30.0F;
    private static final float ROTATION_Y = 225.0F;
    private static final float COMP = 0.43F;

    private jei_block_render_helper() {
    }

    // Füge diese Methode zu deiner jei_block_render_helper Klasse hinzu
    public static ItemStack getHoveredBlock(scanner_recipe recipe, int viewIndex, float centerX, float centerY, float scale, double mouseX, double mouseY) {
        int layerCount = recipe.layerCount();
        boolean allLayers = viewIndex == 0;
        int singleLayer = allLayers ? -1 : viewIndex - 1;

        float structureCenterY = allLayers ? layerCount / 2.0F : 0.5F;

        int startLayer = allLayers ? 0 : singleLayer;
        int endLayer = allLayers ? layerCount - 1 : singleLayer;

        ItemStack closestStack = null;
        double closestDistance = Double.MAX_VALUE;

        // Isometrische Projektionsfaktoren basierend auf den Winkeln (X=30, Y=225)
        // cos(30) ≈ 0.866, sin(30) ≈ 0.5
        // Da Y=225, sind die X- und Z-Achsen symmetrisch gekippt.
        final float cosX = 0.866F;
        final float sinX = 0.500F;
        final float cos45 = 0.707F;

        for (int layer = startLayer; layer <= endLayer; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!recipe.isActiveCell(layer, row, col)) {
                        continue;
                    }

                    BlockState state = recipe.getMatcher(layer, row, col).displayState();
                    if (state.isAir()) {
                        continue;
                    }

                    float blockY = allLayers ? layer : 0.0F;

                    // Lokale Position relativ zum Grid-Mittelpunkt (1.5)
                    float localX = col - 1.5F + 0.5F; // +0.5F um die Blockmitte zu treffen
                    float localY = blockY - structureCenterY + 0.5F;
                    float localZ = row - 1.5F + 0.5F;

                    // Rotationsmatrix manuell für 225° Y und danach 30° X berechnen
                    // Ry(225) = [[cos, 0, sin], [0, 1, 0], [-sin, 0, cos]] mit cos=sin=-cos45
                    // Ry(225): x' = -(x+z)*cos45, z' = (x-z)*cos45
                    float rotX = -(localX + localZ) * cos45;
                    float rotZ = (localX - localZ) * cos45;

                    // Nach X-Rotation im Minecraft-GUI Raum (Y-Achse ist invertiert wegen des -scale):
                    float finalGuiX = rotX;
                    float finalGuiY = (localY * cosX) - (rotZ * sinX);

                    // Skalieren und auf das GUI-Zentrum verschieben
                    float screenBlockX = centerX + (finalGuiX * scale);
                    // Da im Render-Code "-scale" genutzt wird, subtrahieren wir die verschobene Y-Höhe
                    float screenBlockY = centerY - (finalGuiY * scale);

                    // Abstand der Maus zum kalkulierten Blockmittelpunkt auf dem Bildschirm
                    double distX = mouseX - screenBlockX;
                    double distY = mouseY - screenBlockY;
                    double distance = (distX * distX) + (distY * distY);

                    // Wenn die Maus nah genug am Block ist (Radius von ca. einem halben Block im GUI)
                    float hitRadius = scale * 0.65F;
                    if (distance < hitRadius * hitRadius) {
                        // Da bei allLayers Blöcke voreinander stehen können, gewinnt der,
                        // der näher an der Kamera (höheres Layer/niedrigeres Z auf Screen) oder einfach am dichtesten zur Maus ist
                        if (distance < closestDistance) {
                            var stacks = recipe.getMatcher(layer, row, col).displayStacks();
                            if (!stacks.isEmpty()) {
                                closestDistance = distance;
                                closestStack = stacks.get(0);
                            }
                        }
                    }
                }
            }
        }

        return closestStack;
    }

    public static void renderStructure(GuiGraphics guiGraphics, scanner_recipe recipe, int viewIndex, float centerX, float centerY, float scale) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher blockRender = minecraft.getBlockRenderer();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = guiGraphics.pose();

        int layerCount = recipe.layerCount();
        boolean allLayers = viewIndex == 0;
        int singleLayer = allLayers ? -1 : viewIndex - 1;

        float structureCenterY = allLayers ? layerCount / 2.0F : 0.5F;

        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 200.0F);

        // WICHTIG: Y-Achse mit -scale invertieren! Im GUI zeigt +Y nach unten,
        // der Block-Renderer geht aber davon aus, dass +Y nach oben zeigt.
        poseStack.scale(scale, -scale, scale);

        // Isometrische Rotationen (Standard Minecraft-Winkel)
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(225.0F)); // 225° für die klassische Schrägansicht (oder 45° nutzen)

        // Das 3x3-Grid exakt zentrieren (Zentrum liegt bei 1.5)
        poseStack.translate(-1.5F, -structureCenterY, -1.5F);

        int startLayer = allLayers ? 0 : singleLayer;
        int endLayer = allLayers ? layerCount - 1 : singleLayer;

        for (int layer = startLayer; layer <= endLayer; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!recipe.isActiveCell(layer, row, col)) {
                        continue;
                    }

                    BlockState state = recipe.getMatcher(layer, row, col).displayState();
                    if (state.isAir()) {
                        continue;
                    }

                    // Keine Verschiebungs-Tricks mehr: Jeder Block sitzt exakt auf seiner Layer-Höhe
                    float blockY = allLayers ? layer : 0.0F;

                    poseStack.pushPose();
                    poseStack.translate(col, blockY, row);
                    blockRender.renderSingleBlock(state, poseStack, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                }
            }
        }

        buffer.endBatch();
        poseStack.popPose();
    }

    /** Ein einzelner Block fuer {@link #renderBlocks}. */
    public record RenderBlock(BlockState state, float x, float y, float z) {
    }

    /**
     * Rendert eine beliebige kleine Block-Struktur isometrisch (gleicher Stil wie der Scanner).
     * Die Koordinaten in {@link RenderBlock} sind relativ zum gemeinsamen Ursprung.
     */
    public static void renderBlocks(GuiGraphics guiGraphics, java.util.List<RenderBlock> blocks,
                                    float centerX, float centerY, float scale) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher blockRender = minecraft.getBlockRenderer();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 200.0F);
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(ROTATION_X));
        poseStack.mulPose(Axis.YP.rotationDegrees(ROTATION_Y));

        for (RenderBlock block : blocks) {
            if (block.state() == null || block.state().isAir()) continue;
            poseStack.pushPose();
            poseStack.translate(block.x(), block.y(), block.z());
            blockRender.renderSingleBlock(block.state(), poseStack, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        buffer.endBatch();
        poseStack.popPose();
    }

    /**
     * Bildschirmposition eines Blocks im isometrischen 2-Block-Stapel (x/z = 0),
     * damit der Widget-Hover-Test zum Renderer passt.
     */
    public static float[] projectVertical(float localY, float centerX, float centerY, float scale) {
        final float cosX = 0.866F;
        return new float[]{centerX, centerY - (localY * cosX) * scale};
    }
}