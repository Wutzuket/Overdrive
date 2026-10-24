package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.press.CircuitPressBlockEntity;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public final class CircuitPressScreen extends AbstractContainerScreen<CircuitPressMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 192;
    private static final int ENERGY_X = 8;
    private static final int ENERGY_Y = 18;
    private static final int ENERGY_W = 14;
    private static final int ENERGY_H = 44;
    private static final int STATUS_X = 78;
    private static final int STATUS_Y = 18;
    private static final int ARROW_X = 82;
    private static final int ARROW_Y = 43;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;

    public CircuitPressScreen(CircuitPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        titleLabelY = 4;
        inventoryLabelY = HEIGHT - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        CircuitPressBlockEntity press = menu.getBlockEntity();
        GuiStyle.panel(g, x, y, WIDTH, HEIGHT);
        GuiStyle.metricGauge(g, font, x + ENERGY_X, y + ENERGY_Y, ENERGY_W, ENERGY_H,
                press.getEnergyStored() / (float) press.getMaxEnergyStored(), GuiStyle.COL_GREEN, null);
        drawArrow(g, x + ARROW_X, y + ARROW_Y, press);
        GuiStyle.slotFrames(g, menu, x, y);
    }

    private void drawArrow(GuiGraphics g, int x, int y, CircuitPressBlockEntity press) {
        GuiStyle.inset(g, x, y, ARROW_W, ARROW_H);
        double fraction = Math.min(1.0, press.getProgress()
                / (double) Math.max(1, press.getMaxProgress()));
        int fill = (int) (fraction * (ARROW_W - 4));
        int color = press.isActive() ? GuiStyle.COL_CYAN : GuiStyle.COL_TEXT_DIM;
        if (fill > 0) {
            g.fill(x + 2, y + 5, x + 2 + Math.min(fill, 14), y + 12, color);
            if (fill > 9) g.fill(x + 16, y + 4, x + 18, y + 13, color);
            if (fill > 12) g.fill(x + 18, y + 3, x + 20, y + 14, color);
        }
        g.fill(x + 16, y + 4, x + 18, y + 13, color);
        g.fill(x + 18, y + 5, x + 20, y + 12, color);
        g.fill(x + 20, y + 7, x + 22, y + 10, color);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        CircuitPressBlockEntity press = menu.getBlockEntity();
        g.drawString(font, title, 8, 4, GuiStyle.COL_TEXT, false);
        g.drawString(font, "Status: " + (press.isActive() ? "§aRunning" : "§eIdle"),
                STATUS_X, STATUS_Y, GuiStyle.COL_TEXT, false);
        g.drawString(font, "Inputs", 28, 25, GuiStyle.COL_TEXT_DIM, false);
        g.drawString(font, "Output", 136, 35, GuiStyle.COL_TEXT_DIM, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, GuiStyle.COL_TEXT_DIM, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        int energyX = leftPos + ENERGY_X;
        int energyY = topPos + ENERGY_Y;
        if (mouseX >= energyX && mouseX <= energyX + ENERGY_W
                && mouseY >= energyY && mouseY <= energyY + ENERGY_H) {
            CircuitPressBlockEntity press = menu.getBlockEntity();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("Energy"));
            lines.add(Component.literal(ODFormat.formatEnergyPair(
                    press.getEnergyStored(), press.getMaxEnergyStored())));
            lines.add(Component.literal("Usage: " + ODFormat.formatEnergyPerTick(press.getEnergyPerTick())));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
            return;
        }

        int x = leftPos + ARROW_X;
        int y = topPos + ARROW_Y;
        if (mouseX >= x && mouseX <= x + ARROW_W && mouseY >= y && mouseY <= y + ARROW_H) {
            CircuitPressBlockEntity press = menu.getBlockEntity();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("Progress: " + press.getProgress() + " / "
                    + press.getMaxProgress() + " t"));
            lines.add(Component.literal("Usage: " + ODFormat.formatEnergyPerTick(press.getEnergyPerTick())));
            lines.add(Component.literal("Total: " + ODFormat.formatEnergy(
                    (long) press.getEnergyPerTick() * press.getMaxProgress())));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
            return;
        }
        super.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
