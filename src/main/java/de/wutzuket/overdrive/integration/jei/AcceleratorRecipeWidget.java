package de.wutzuket.overdrive.integration.jei;

import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

public class AcceleratorRecipeWidget implements IRecipeWidget {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 80;

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(0, 0);
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // Mekanism zeichnet oft einen stilisierten Verarbeitungs-Pfeil "->" in die Mitte zwischen Slot und Tank
        guiGraphics.drawString(font, Component.literal("➔"), 78, 34, 0x8B8B8B, false);
    }
}