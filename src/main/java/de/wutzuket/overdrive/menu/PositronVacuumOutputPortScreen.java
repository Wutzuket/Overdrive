package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PositronVacuumOutputPortScreen extends AbstractContainerScreen<PositronVacuumOutputPortMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    private static final int FLUID_BAR_X = 18;
    private static final int FLUID_BAR_Y = 17;
    private static final int FLUID_BAR_W = 18;
    private static final int FLUID_BAR_H = 54;

    private double lastMouseX;
    private double lastMouseY;

    public PositronVacuumOutputPortScreen(PositronVacuumOutputPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);
        GuiStyle.inset(guiGraphics, x + 6, y + 80, 164, 82);
        GuiStyle.antimatterGauge(guiGraphics, font, menu.getAntimatterType(), menu.getFluidAmount(),
                menu.getFluidCapacity(), x + FLUID_BAR_X, y + FLUID_BAR_Y, FLUID_BAR_W, FLUID_BAR_H, null);
        GuiStyle.slotFrames(guiGraphics, menu, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);
        var type = menu.getAntimatterType();
        if (type == null) {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.positron_vacuum_output_port.empty"),
                    40, 20, GuiStyle.COL_TEXT_DIM, false);
        } else {
            guiGraphics.drawString(this.font, type.displayName(), 40, 20, GuiStyle.COL_TEXT, false);
        }
        guiGraphics.drawString(this.font, ODFormat.formatFluidPair(menu.getFluidAmount(), menu.getFluidCapacity()),
                40, 30, GuiStyle.COL_CYAN, false);
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.positron_vacuum_output_port.cell_hint"),
                40, 44, GuiStyle.COL_TEXT_DIM, false);
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.positron_vacuum_output_port.cell_hint_action"),
                40, 54, GuiStyle.COL_TEXT_DIM, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                GuiStyle.COL_TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        if (mouseX >= x + FLUID_BAR_X && mouseX <= x + FLUID_BAR_X + FLUID_BAR_W &&
                mouseY >= y + FLUID_BAR_Y && mouseY <= y + FLUID_BAR_Y + FLUID_BAR_H) {
            List<Component> lines = new ArrayList<>();
            var type = menu.getAntimatterType();
            if (type != null) {
                lines.add(type.displayName());
            } else {
                lines.add(Component.translatable("container.overdrive.positron_vacuum_output_port.empty"));
            }
            lines.add(Component.literal(ODFormat.formatFluidPair(menu.getFluidAmount(), menu.getFluidCapacity())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}