package de.wutzuket.overdrive.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidRenderHelper {
    public static void renderFluidGauge(GuiGraphics g, FluidStack stack, int capacity, int x, int y, int width, int height) {
        g.fill(x, y, x + width, y + height, 0xFF050509);
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF101012);
        renderFluidBar(g, stack, capacity, x + 2, y + 2, width - 4, height - 4);
        g.fill(x + 2, y + 2, x + 4, y + height - 2, 0x18FFFFFF);
        g.fill(x + width - 2, y + 2, x + width - 1, y + height - 2, 0x66000000);
    }

    public static void renderFluidBar(GuiGraphics g, FluidStack stack, int capacity, int x, int y, int width, int height) {
        if (stack.isEmpty() || capacity <= 0) return;

        int filledHeight = (int) Math.min(height, (long) stack.getAmount() * height / capacity);
        if (filledHeight <= 0) return;

        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(stack.getFluid());
        ResourceLocation stillTexture = props.getStillTexture(stack);
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int tint = props.getTintColor(stack);
        float alpha = ((tint >> 24) & 0xFF) / 255.0f;
        if (alpha == 0.0f) alpha = 1.0f;
        float red = ((tint >> 16) & 0xFF) / 255.0f;
        float green = ((tint >> 8) & 0xFF) / 255.0f;
        float blue = (tint & 0xFF) / 255.0f;

        int renderY = y + height - filledHeight;
        g.enableScissor(x, renderY, x + width, y + height);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(red, green, blue, alpha);
        for (int tileY = y + height - 16; tileY > renderY - 16; tileY -= 16) {
            for (int tileX = x; tileX < x + width; tileX += 16) {
                g.blit(tileX, tileY, 0, 16, 16, sprite);
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        g.disableScissor();

        g.fill(x, renderY, x + width, Math.min(renderY + 2, y + height), 0x45FFFFFF);
        if (width >= 6 && filledHeight >= 6) {
            g.fill(x + 1, renderY + 2, x + 3, Math.min(renderY + 6, y + height), 0x30FFFFFF);
        }
        RenderSystem.disableBlend();
    }
}
