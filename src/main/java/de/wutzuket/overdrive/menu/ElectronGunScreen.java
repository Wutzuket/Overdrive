package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.network.ToggleHologramPayload;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ElectronGunScreen extends AbstractContainerScreen<ElectronGunMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 126;
    private static final int BAR_W = 16;
    private static final int BAR_H = 54;

    private ButtonHelper hologramButton;

    public ElectronGunScreen(ElectronGunMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;

        this.hologramButton = new ButtonHelper(x + WIDTH - 22, y + 6, 16, 16, Component.literal("H"),
                button -> PacketDistributor.sendToServer(new ToggleHologramPayload(menu.getBlockEntity().getBlockPos())),
                GuiStyle.COL_CYAN);
        addRenderableWidget(hologramButton);
    }

    private FluidStack getFluidStack() {
        var be = menu.getBlockEntity();
        if (be == null) return FluidStack.EMPTY;
        de.wutzuket.overdrive.antimatter.Antimatter fluid = be.getAntimatterTank().getAntimatterInTank(0);
        if (fluid == null) return FluidStack.EMPTY;
        return FluidStack.EMPTY;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);

        float energy = menu.getMaxEnergy() > 0 ? (float) menu.getEnergy() / menu.getMaxEnergy() : 0.0f;
        GuiStyle.metricGauge(guiGraphics, font, x + 8, y + 16, BAR_W, BAR_H, energy,
                GuiStyle.COL_GREEN, null);
        GuiStyle.metricGauge(guiGraphics, font, x + 28, y + 16, BAR_W, BAR_H,
                menu.getVacuumScaled(BAR_H - 2) / (float) (BAR_H - 2), GuiStyle.COL_CYAN, null);
        GuiStyle.fluidGauge(guiGraphics, font, getFluidStack(), menu.getFluidCapacity(),
                x + 48, y + 16, BAR_W, BAR_H, null);

        GuiStyle.hBar(guiGraphics, x + 70, y + 34, 96, 10,
                menu.getProgressScaled(94) / 94.0, GuiStyle.COL_CYAN);

        if (menu.isActive()) {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.electron_gun.running"),
                    70, 50, GuiStyle.COL_GREEN, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.electron_gun.idle"),
                    70, 50, GuiStyle.COL_YELLOW, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);
        GuiStyle.kv(guiGraphics, font,
                Component.translatable("container.overdrive.electron_gun.energy"),
                ODFormat.formatEnergyPair(menu.getEnergy(), menu.getMaxEnergy()), 8, 74, GuiStyle.COL_GREEN);
        GuiStyle.kv(guiGraphics, font,
                Component.translatable("container.overdrive.electron_gun.vacuum"),
                (menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %", 8, 84, GuiStyle.COL_CYAN);
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.electron_gun.usage", 3000),
                8, 94, GuiStyle.COL_TEXT, false);
        // Im Leerlauf wird nichts produziert -> 0 B/t anzeigen.
        double production = menu.isActive() ? menu.getProductionPerTickSynced() : 0.0;
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.production",
                        ODFormat.formatFluidPerTick(production)),
                8, 104, production > 0 ? GuiStyle.COL_GREEN : GuiStyle.COL_TEXT_DIM, false);
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
        } else if (mouseX >= x + 28 && mouseX <= x + 44 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal((menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 48 && mouseX <= x + 64 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            FluidStack fluid = getFluidStack();
            lines.add(fluid.isEmpty() ? Component.literal("Empty") : fluid.getHoverName());
            lines.add(Component.literal(ODFormat.formatFluidPair(menu.getFluidAmount(), menu.getFluidCapacity())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
