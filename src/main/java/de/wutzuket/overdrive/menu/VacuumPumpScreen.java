package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlockEntity.VacuumStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class VacuumPumpScreen extends AbstractContainerScreen<VacuumPumpMenu> {
    private static final int WIDTH = 176;
    // Kompakte Höhe ohne Spielerinventar unten
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

        // Button nach unten versetzt (y + 20), damit er nicht im Titel hängt
        this.hologramButton = Button.builder(
                Component.literal("H"),
                button -> {
                    if (menu.getBlockEntity().isValidStructure()) return;
                    boolean current = menu.getBlockEntity().isHologramEnabled();
                    menu.getBlockEntity().setHologramEnabled(!current);
                }
        ).bounds(x + 150, y + 20, 18, 18).build();
        addRenderableWidget(hologramButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. Sauberes Mekanism-Panel als Basis zeichnen
        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFC6C6C6);
        // 3D-Grenzrahmen
        guiGraphics.fill(x, y, x + WIDTH, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + HEIGHT, 0xFFFFFFFF);
        guiGraphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFF555555);
        guiGraphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF555555);

        // 2. Vertikalen Energiebalken (Mekanism-Style) rendern
        renderMekanismBar(guiGraphics, x + 8, y + 16, BAR_W, BAR_H, menu.getEnergyScaled(BAR_H - 2), 0xFFFF3333);

        // 3. Horizontaler Vakuum-Fortschrittsbalken im Mekanism-Style
        int px = x + 44, py = y + 32, pw = 84, ph = 10;

        guiGraphics.fill(px, py, px + pw, py + ph, 0xFF2D2D2D);
        guiGraphics.fill(px + 1, py + 1, px + pw, py + ph, 0xFF707070);
        guiGraphics.fill(px + 1, py + 1, px + pw - 1, py + ph - 1, 0xFF141414);

        int vacuumBarWidth = menu.getVacuumProgress() * (pw - 2) / 100;
        if (vacuumBarWidth > 0) {
            int fillW = Math.min(vacuumBarWidth, pw - 2);
            // Klares Mekanism-Blau für das Vakuum
            guiGraphics.fill(px + 1, py + 1, px + 1 + fillW, py + ph - 1, 0xFF3399FF);
            // Glanz-Highlight an der Oberkante
            guiGraphics.fill(px + 1, py + 1, px + 1 + fillW, py + 2, 0x66FFFFFF);
        }
    }

    private void renderMekanismBar(GuiGraphics guiGraphics, int bx, int by, int w, int h, int fillHeight, int color) {
        guiGraphics.fill(bx, by, bx + w, by + h, 0xFF2D2D2D);
        guiGraphics.fill(bx + 1, by + 1, bx + w, by + h, 0xFF707070);
        guiGraphics.fill(bx + 1, by + 1, bx + w - 1, by + h - 1, 0xFF141414);

        if (fillHeight > 0) {
            int innerX = bx + 1;
            int innerY = by + 1;
            int innerW = w - 2;
            int maxInnerH = h - 2;

            int top = innerY + maxInnerH - fillHeight;
            guiGraphics.fill(innerX, top, innerX + innerW, innerY + maxInnerH, color);
            guiGraphics.fill(innerX, top, innerX + innerW, top + 1, 0x66FFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Titel oben links
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);

        // Texte für das kompakte Layout ohne Spielerinventar nach unten gerückt
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.energy"), 8, 74, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.usage", menu.getEnergyUsage()), 8, 84, 0x404040, false);

        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.vacuum"), 44, 22, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.volume", menu.getVolume()), 44, 46, 0x404040, false);

        // Status-Text-Farbzuordnung
        VacuumStatus status = VacuumStatus.values()[menu.getStatusOrdinal()];
        Component statusText;
        int statusColor;
        switch (status) {
            case OFFLINE -> { statusText = Component.translatable("container.overdrive.vacuum_pump.offline"); statusColor = 0xEF4444; }
            case EVACUATING -> { statusText = Component.translatable("container.overdrive.vacuum_pump.evacuating"); statusColor = 0xEAB308; }
            case STABLE -> { statusText = Component.translatable("container.overdrive.vacuum_pump.stable"); statusColor = 0x22C55E; }
            case LEAK -> { statusText = Component.translatable("container.overdrive.vacuum_pump.leak"); statusColor = 0xDC2626; }
            default -> { statusText = Component.literal("???"); statusColor = 0xFFFFFF; }
        }
        guiGraphics.drawString(this.font, statusText, 44, 58, statusColor, false);
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
            lines.add(Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE"));
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