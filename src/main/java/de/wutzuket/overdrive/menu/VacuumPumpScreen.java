package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlockEntity.VacuumStatus;
import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class VacuumPumpScreen extends AbstractContainerScreen<VacuumPumpMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 116;

    private static final int BAR_W = 16;
    private static final int BAR_H = 54;

    private Button hologramButton;

    public VacuumPumpScreen(VacuumPumpMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        this.hologramButton = new ButtonHelper(x + 150, y + 20, 18, 18, Component.literal("H"),
                button -> {
                    if (menu.getBlockEntity().isValidStructure()) return;
                    boolean current = menu.getBlockEntity().isHologramEnabled();
                    menu.getBlockEntity().setHologramEnabled(!current);
                }, GuiStyle.COL_CYAN);
        addRenderableWidget(hologramButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);

        GuiStyle.metricGauge(guiGraphics, font, x + 8, y + 16, BAR_W, BAR_H,
                menu.getEnergyScaled(BAR_H - 2) / (float) (BAR_H - 2), GuiStyle.COL_RED, null);
        GuiStyle.hBar(guiGraphics, x + 44, y + 32, 84, 10,
                menu.getVacuumProgress() / 100.0, GuiStyle.COL_CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);

        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.vacuum"),
                44, 22, GuiStyle.COL_TEXT_DIM, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.volume", menu.getVolume()),
                44, 46, GuiStyle.COL_CYAN, false);

        VacuumStatus status = VacuumStatus.values()[menu.getStatusOrdinal()];
        Component statusText;
        int statusColor;
        switch (status) {
            case OFFLINE -> { statusText = Component.translatable("container.overdrive.vacuum_pump.offline"); statusColor = GuiStyle.COL_RED; }
            case EVACUATING -> { statusText = Component.translatable("container.overdrive.vacuum_pump.evacuating"); statusColor = GuiStyle.COL_YELLOW; }
            case STABLE -> { statusText = Component.translatable("container.overdrive.vacuum_pump.stable"); statusColor = GuiStyle.COL_GREEN; }
            case LEAK -> { statusText = Component.translatable("container.overdrive.vacuum_pump.leak"); statusColor = GuiStyle.COL_RED; }
            default -> { statusText = Component.literal("???"); statusColor = GuiStyle.COL_TEXT; }
        }
        guiGraphics.drawString(this.font, statusText, 44, 58, statusColor, false);

        GuiStyle.kv(guiGraphics, font, Component.translatable("container.overdrive.vacuum_pump.energy"),
                ODFormat.formatEnergyPair(menu.getEnergy(), menu.getMaxEnergy()), 8, 74, GuiStyle.COL_GREEN);
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.vacuum_pump.usage", menu.getEnergyUsage()),
                8, 84, GuiStyle.COL_TEXT, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        if (mouseX >= x + 8 && mouseX <= x + 24 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(ODFormat.formatEnergyPair(menu.getEnergy(), menu.getMaxEnergy())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 44 && mouseX <= x + 128 && mouseY >= y + 32 && mouseY <= y + 42) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(menu.getVacuumProgress() + "%"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
