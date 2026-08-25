package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.network.ToggleHologramPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ADControllerScreen extends AbstractContainerScreen<ADControllerMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 116;
    private static final int BAR_W = 16;
    private static final int BAR_H = 54;

    private Button hologramButton;

    public ADControllerScreen(ADControllerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        this.hologramButton = Button.builder(
                Component.literal("H"),
                button -> {
                    PacketDistributor.sendToServer(new ToggleHologramPayload(menu.getBlockEntity().getBlockPos()));
                }
        ).bounds(x + 150, y + 20, 18, 18).build();
        addRenderableWidget(hologramButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFC6C6C6);
        guiGraphics.fill(x, y, x + WIDTH, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + HEIGHT, 0xFFFFFFFF);
        guiGraphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFF555555);
        guiGraphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF555555);

        renderMekanismBar(guiGraphics, x + 8, y + 16, BAR_W, BAR_H, menu.getEnergyScaled(BAR_H - 2), 0xFFFF3333);
        renderMekanismBar(guiGraphics, x + 28, y + 16, BAR_W, BAR_H, menu.getVacuumScaled(BAR_H - 2), 0xFF3399FF);

        int px = x + 48, py = y + 36, pw = 80, ph = 10;

        guiGraphics.fill(px, py, px + pw, py + ph, 0xFF2D2D2D);
        guiGraphics.fill(px + 1, py + 1, px + pw, py + ph, 0xFF707070);
        guiGraphics.fill(px + 1, py + 1, px + pw - 1, py + ph - 1, 0xFF141414);

        int maxTicks = menu.getMaxProgressTicks();
        int progressWidth = maxTicks > 0 ? menu.getProgressTicks() * (pw - 2) / maxTicks : 0;
        if (progressWidth > 0) {
            int fillW = Math.min(progressWidth, pw - 2);
            guiGraphics.fill(px + 1, py + 1, px + 1 + fillW, py + ph - 1, 0xFF00E5FF);
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
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.accelerator_controller.energy"), 8, 74, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.accelerator_controller.vacuum"), 8, 84, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.accelerator_controller.usage", 2000), 8, 94, 0x404040, false);

        if (menu.isValidStructure()) {
            Component statusText;
            int statusColor;
            if (menu.isActive()) {
                statusText = Component.translatable("container.overdrive.accelerator_controller.running");
                statusColor = 0x22C55E;
            } else {
                statusText = Component.translatable("container.overdrive.accelerator_controller.idle");
                statusColor = 0xEAB308;
            }
            guiGraphics.drawString(this.font, statusText, 48, 24, statusColor, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.accelerator_controller.invalid_structure"), 48, 24, 0xEF4444, false);
        }
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
        } else if (mouseX >= x + 28 && mouseX <= x + 44 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal((menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
