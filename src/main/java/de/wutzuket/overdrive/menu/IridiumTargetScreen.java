package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class IridiumTargetScreen extends AbstractContainerScreen<IridiumTargetMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int FLUID_BAR_X = 18;
    private static final int FLUID_BAR_Y = 17;
    private static final int FLUID_BAR_W = 18;
    private static final int FLUID_BAR_H = 54;

    public IridiumTargetScreen(IridiumTargetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private void drawFluidGauge(GuiGraphics g, FluidStack stack, int capacity, int x, int y, String label) {
        GuiStyle.fluidGauge(g, font, stack, capacity, x, y, FLUID_BAR_W, FLUID_BAR_H, label);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(g, x, y, WIDTH, HEIGHT);
        GuiStyle.inset(g, x + 6, y + 80, 164, 82);
        drawFluidGauge(g, menu.getFluidStack(), menu.getFluidCapacity(), x + FLUID_BAR_X, y + FLUID_BAR_Y, null);
        GuiStyle.slotFrames(g, menu, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, GuiStyle.COL_TEXT, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, GuiStyle.COL_TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        if (in(mouseX, mouseY, x + FLUID_BAR_X, y + FLUID_BAR_Y, FLUID_BAR_W, FLUID_BAR_H)) {
            renderFluidTooltip(g, menu.getFluidStack(), menu.getFluidAmount(), menu.getFluidCapacity(), "Empty", mouseX, mouseY);
            return;
        }
        super.renderTooltip(g, mouseX, mouseY);
    }

    private void renderFluidTooltip(GuiGraphics g, FluidStack stack, int amount, int capacity, String fallback, int mouseX, int mouseY) {
        Component name = stack.isEmpty() ? Component.literal(fallback) : stack.getHoverName();
        g.renderTooltip(font, List.of(name, Component.literal(ODFormat.formatFluidPair(amount, capacity))), java.util.Optional.empty(), mouseX, mouseY);
    }

    private static boolean in(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
