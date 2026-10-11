package de.wutzuket.overdrive.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ButtonHelper extends Button {

    // Mekanism Colors
    private static final int COL_BG_NORMAL = 0xFF1E1E26;
    private static final int COL_BG_HOVER = 0xFF2A2A35;
    private static final int COL_BG_DISABLED = 0xFF18181C;
    private static final int COL_BORDER_NORMAL = 0xFF3F3F4C;
    private static final int COL_BORDER_HOVER = 0xFF00F0FF;
    private static final int COL_BORDER_DISABLED = 0xFF2C2C35;
    private static final int COL_TEXT_NORMAL = 0xFFD1D5DB;
    private static final int COL_TEXT_HOVER = 0xFFFFFFFF;
    private static final int COL_TEXT_DISABLED = 0xFF555560;
    private static final int COL_HIGHLIGHT = 0x44FFFFFF;

    private final int accentColor;

    public ButtonHelper(int x, int y, int width, int height, Component message, OnPress onPress, int accentColor) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.accentColor = accentColor;
    }

    public ButtonHelper(int x, int y, int width, int height, Component message, OnPress onPress) {
        this(x, y, width, height, message, onPress, 0xFF00F0FF);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        boolean hover = this.isHoveredOrFocused();
        boolean active = this.active;

        int bgColor;
        int borderColor;
        int textColor;

        if (!this.active) {
            bgColor = 0xFF18181C;
            borderColor = 0xFF2C2C35;
        } else if (this.isHoveredOrFocused()) {
            bgColor = 0xFF2A2A35;
            borderColor = 0xFF00F0FF;
        } else {
            bgColor = 0xFF1E1E26;
            borderColor = 0xFF3F3F4C;
        }

        textColor = this.active ? (this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFD1D5DB) : 0xFF555560;

        RenderSystem.enableBlend();

        // Outer border
        g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

        // Inner background
        g.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, bgColor);

        // Highlight line at top when hovered
        if (this.isHoveredOrFocused() && this.active) {
            g.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + 2, 0x44FFFFFF);
        }

        // Active indicator - bottom line in accent color
        if (this.active) {
            g.fill(this.getX() + 1, this.getY() + this.height - 2, this.getX() + this.width - 1, this.getY() + this.height - 1, this.accentColor & 0xFF000000 | 0x00444444);
        }

        // Render text centered
        textColor = this.active ? (this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFD1D5DB) : 0xFF555560;
        int textX = this.getX() + this.width / 2;
        int textY = this.getY() + (this.height - 8) / 2;
        
        // Use Minecraft's font renderer
        var mc = Minecraft.getInstance();
        g.drawCenteredString(mc.font, this.getMessage(), textX, textY, textColor);

        RenderSystem.disableBlend();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active) {
            super.onClick(mouseX, mouseY);
        }
    }
}