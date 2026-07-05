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
    private static final int HEIGHT = 166;

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
        this.hologramButton = Button.builder(
                Component.literal("H"),
                button -> {
                    if (menu.getBlockEntity().isValidStructure()) return;
                    boolean current = menu.getBlockEntity().isHologramEnabled();
                    menu.getBlockEntity().setHologramEnabled(!current);
                }
        ).bounds(x + 152, y + 5, 18, 18).build();
        addRenderableWidget(hologramButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFFC6C6C6);

        guiGraphics.fill(x + 8, y + 16, x + 24, y + 70, 0xFF555555);
        int energyBarHeight = menu.getEnergyScaled(50);
        guiGraphics.fill(x + 10, y + 18 + 50 - energyBarHeight, x + 22, y + 68, 0xFFFF4444);

        guiGraphics.fill(x + 44, y + 32, x + 128, y + 44, 0xFF555555);
        int vacuumBarWidth = menu.getVacuumProgress() * 80 / 100;
        guiGraphics.fill(x + 46, y + 34, x + 46 + vacuumBarWidth, y + 42, 0xFF44AAFF);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.energy"), 8, 76, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.usage", menu.getEnergyUsage()), 8, 86, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.vacuum"), 44, 24, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.overdrive.vacuum_pump.volume", menu.getVolume()), 44, 48, 0x404040, false);

        VacuumStatus status = VacuumStatus.values()[menu.getStatusOrdinal()];
        Component statusText;
        int statusColor;
        switch (status) {
            case OFFLINE -> { statusText = Component.translatable("container.overdrive.vacuum_pump.offline"); statusColor = 0xFF5555; }
            case EVACUATING -> { statusText = Component.translatable("container.overdrive.vacuum_pump.evacuating"); statusColor = 0xFFFF55; }
            case STABLE -> { statusText = Component.translatable("container.overdrive.vacuum_pump.stable"); statusColor = 0x55FF55; }
            case LEAK -> { statusText = Component.translatable("container.overdrive.vacuum_pump.leak"); statusColor = 0xFF0000; }
            default -> { statusText = Component.literal("???"); statusColor = 0xFFFFFF; }
        }
        guiGraphics.drawString(this.font, statusText, 44, 62, statusColor, false);
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
        } else if (mouseX >= x + 44 && mouseX <= x + 128 && mouseY >= y + 32 && mouseY <= y + 44) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(menu.getVacuumProgress() + "%"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
