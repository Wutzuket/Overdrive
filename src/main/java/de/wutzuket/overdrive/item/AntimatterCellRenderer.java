package de.wutzuket.overdrive.item;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.antimatter.Antimatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the icon of an antimatter cell. The icon is generated at runtime: the shape of the cell is
 * fixed, the colour and the fill level come from the antimatter inside of the stack. That way every
 * new type of antimatter automatically gets a fitting cell icon without a single texture file.
 */
public class AntimatterCellRenderer extends BlockEntityWithoutLevelRenderer {

    /** A plain white texture, the colour comes from the vertex colours. */
    private static final ResourceLocation TEXTURE = Overdrive.id("textures/dynamic/antimatter_cell.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucentCull(TEXTURE);

    private static AntimatterCellRenderer instance;
    private static boolean textureRegistered;

    /** Der Rahmen der Zelle. */
    private static final int FRAME = 0xFF95A0B8;
    private static final int FRAME_DARK = 0xFF2C3348;
    /** Das Glas, wenn die Zelle leer ist (nur ein leichter Schleier). */
    private static final int GLASS = 0x66C8F0FF;

    private AntimatterCellRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher dispatcher,
                                  net.minecraft.client.model.geom.EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    /** @return the shared renderer, it is created when the client is available. */
    public static AntimatterCellRenderer getInstance() {
        if (instance == null) {
            Minecraft minecraft = Minecraft.getInstance();
            instance = new AntimatterCellRenderer(minecraft.getBlockEntityRenderDispatcher(),
                    minecraft.getEntityModels());
        }
        return instance;
    }

    /** Registers the white texture in the texture manager. */
    private static void registerTexture() {
        if (textureRegistered) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        DynamicTexture texture = new DynamicTexture(16, 16, false);
        NativeImage image = texture.getPixels();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                image.setPixelRGBA(x, y, 0xFFFFFFFF);
            }
        }
        texture.upload();
        minecraft.getTextureManager().register(TEXTURE, texture);
        textureRegistered = true;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffer,
                             int light, int overlay) {
        registerTexture();

        Antimatter type = null;
        int amount = 0;
        int capacity = 1;
        if (stack.getItem() instanceof AntimatterCellItem cell) {
            type = cell.getType(stack);
            amount = cell.getAmount(stack);
            capacity = cell.getCapacity();
        }

        // Der Deckel der Zelle
        rect(buffer, pose, 6, 13, 10, 15, FRAME, 0.0F, light, overlay);
        rect(buffer, pose, 7, 13, 9, 15, FRAME_DARK, 0.01F, light, overlay);

        // Der Koerper der Zelle
        rect(buffer, pose, 4, 1, 12, 13, FRAME, 0.0F, light, overlay);

        if (type != null && amount > 0) {
            float fill = Math.max(0.0F, Math.min(1.0F, (float) amount / capacity));
            int height = Math.max(1, Math.round(11 * fill));
            int top = 2 + height;
            rect(buffer, pose, 5, 2, 11, top, type.color(), 0.02F, light, overlay);
            // Neon-Kante an der Fuellung
            rect(buffer, pose, 5, top - 1, 11, top, brighten(type.color(), 1.6F), 0.04F, light, overlay);
        }

        // Das Glasfenster liegt vor dem Inhalt
        rect(buffer, pose, 5, 2, 11, 13, GLASS, 0.03F, light, overlay);

        if (type != null && amount > 0) {
            float fill = Math.max(0.0F, Math.min(1.0F, (float) amount / capacity));
            int height = Math.max(1, Math.round(11 * fill));
            int top = 2 + height;
            // Glanzlicht an der Fuellung
            rect(buffer, pose, 6, top - 2, 7, top - 1, 0xA0FFFFFF, 0.05F, light, overlay);
        }

        // Der Schatten an der Unterkante
        rect(buffer, pose, 4, 1, 12, 2, FRAME_DARK, 0.06F, light, overlay);
    }

    /** Hellt eine Farbe auf, damit die Fuellung leuchtet. */
    private static int brighten(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(255, (int) (((argb >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((argb >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((argb & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Zeichnet ein gefuelltes Rechteck in Item-Koordinaten (0..1). */
    private static void rect(MultiBufferSource buffer, PoseStack pose, float x1, float y1, float x2, float y2,
                             int argb, float z, int light, int overlay) {
        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        float a = ((argb >>> 24) & 0xFF) / 255.0F;
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;

        quad(consumer, pose, x1 / 16.0F, y1 / 16.0F, x2 / 16.0F, y2 / 16.0F, z, r, g, b, a, light, overlay);
    }

    /** Ein Viereck in der XY-Ebene, im Uhrzeigersinn (die Normale zeigt nach +Z). */
    private static void quad(VertexConsumer consumer, PoseStack pose, float x1, float y1, float x2, float y2,
                             float z, float r, float g, float b, float a, int light, int overlay) {
        var matrix = pose.last().pose();
        consumer.addVertex(matrix, x1, y1, z).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(overlay).setLight(light).setNormal(pose.last(), 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, x2, y1, z).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(overlay).setLight(light).setNormal(pose.last(), 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, x2, y2, z).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(overlay).setLight(light).setNormal(pose.last(), 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, x1, y2, z).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(overlay).setLight(light).setNormal(pose.last(), 0.0F, 0.0F, 1.0F);
    }
}
