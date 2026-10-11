package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.network.ELENAEnergyRatePayload;
import de.wutzuket.overdrive.network.ToggleHologramPayload;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ELENAControllerScreen extends AbstractContainerScreen<ELENAControllerMenu> {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 136;
    private static final int BAR_W = 14;
    private static final int BAR_H = 44;
    private static final int BAR_Y = 16;
    private static final int BAR_ENERGY_X = 8;
    private static final int BAR_VACUUM_X = 26;
    private static final int BAR_INPUT_X = 44;
    private static final int BAR_OUTPUT_X = 62;
    private static final int HBAR_X = 84;
    private static final int HBAR_Y = 34;
    private static final int HBAR_W = 104;
    private static final int HBAR_H = 10;

    private ButtonHelper hologramButton;
    private EditBox rateField;
    private ButtonHelper rateSet;

    public ELENAControllerScreen(ELENAControllerMenu menu, Inventory inventory, Component title) {
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

        rateField = new EditBox(font, x + 48, y + 104, 80, 14, Component.literal("rate"));
        rateField.setMaxLength(16);
        rateField.setValue(ODFormat.formatEnergy(menu.getEnergyRateSynced()));
        addRenderableWidget(rateField);
        rateSet = new ButtonHelper(x + 130, y + 104, 44, 14, Component.literal("Set"),
                b -> submitRate(), GuiStyle.COL_CYAN);
        addRenderableWidget(rateSet);
    }

    private void submitRate() {
        if (rateField == null) return;
        try {
            long rate = parseRate(rateField.getValue());
            rate = Math.max(1, Math.min(menu.getMaxRate(), rate));
            PacketDistributor.sendToServer(new ELENAEnergyRatePayload(menu.getBlockEntity().getBlockPos(), (int) rate));
            rateField.setValue(ODFormat.formatEnergy(rate));
        } catch (Exception ignored) {
            rateField.setValue(ODFormat.formatEnergy(menu.getEnergyRateSynced()));
        }
    }

    /** Parst "400M", "400 MFE", "1.2G", "800000000" -> FE. */
    static long parseRate(String raw) {
        String s = raw.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        if (s.endsWith("FE")) s = s.substring(0, s.length() - 2);
        long mult = 1;
        if (s.endsWith("K")) { mult = 1_000L; s = s.substring(0, s.length() - 1); }
        else if (s.endsWith("M")) { mult = 1_000_000L; s = s.substring(0, s.length() - 1); }
        else if (s.endsWith("G")) { mult = 1_000_000_000L; s = s.substring(0, s.length() - 1); }
        else if (s.endsWith("T")) { mult = 1_000_000_000_000L; s = s.substring(0, s.length() - 1); }
        double v = Double.parseDouble(s.replace(",", "."));
        if (!Double.isFinite(v) || v <= 0) throw new IllegalArgumentException("rate");
        return (long) (v * mult);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (rateField != null && rateField.isFocused() && (keyCode == 257 || keyCode == 335)) {
            submitRate();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);

        float energy = menu.getMaxEnergyLong() > 0 ? (float) (menu.getEnergyLong() / (double) menu.getMaxEnergyLong()) : 0.0f;
        GuiStyle.metricGauge(guiGraphics, font, x + BAR_ENERGY_X, y + BAR_Y, BAR_W, BAR_H, energy,
                GuiStyle.COL_GREEN, "E");
        GuiStyle.metricGauge(guiGraphics, font, x + BAR_VACUUM_X, y + BAR_Y, BAR_W, BAR_H,
                menu.getVacuumScaled(BAR_H - 2) / (float) (BAR_H - 2), GuiStyle.COL_CYAN, "V");

        var be = menu.getBlockEntity();
        if (be != null) {
            var inTank = be.getInputTank();
            GuiStyle.fluidGauge(guiGraphics, font, inTank.getFluid(), inTank.getCapacity(),
                    x + BAR_INPUT_X, y + BAR_Y, BAR_W, BAR_H, "I");
            var outTank = be.getFluidTank();
            GuiStyle.fluidGauge(guiGraphics, font, outTank.getFluid(), outTank.getCapacity(),
                    x + BAR_OUTPUT_X, y + BAR_Y, BAR_W, BAR_H, "O");
        }

        int maxTicks = menu.getMaxProgressTicks();
        double progress = maxTicks > 0 ? menu.getProgressTicks() / (double) maxTicks : 0.0;
        progress = Math.max(progress, menu.getChargeScaled() / 200.0);
        GuiStyle.hBar(guiGraphics, x + HBAR_X, y + HBAR_Y, HBAR_W, HBAR_H, progress, GuiStyle.COL_CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);

        if (menu.isValidStructure()) {
            guiGraphics.drawString(this.font,
                    Component.translatable(menu.isActive()
                            ? "container.overdrive.accelerator_controller.running"
                            : "container.overdrive.accelerator_controller.idle"),
                    HBAR_X, 50, menu.isActive() ? GuiStyle.COL_GREEN : GuiStyle.COL_YELLOW, false);
        } else {
            guiGraphics.drawString(this.font,
                    Component.translatable("container.overdrive.accelerator_controller.invalid_structure"),
                    HBAR_X, 50, GuiStyle.COL_RED, false);
        }

        GuiStyle.kv(guiGraphics, font,
                Component.translatable("container.overdrive.accelerator_controller.energy"),
                ODFormat.formatEnergyPair(menu.getEnergyLong(), menu.getMaxEnergyLong()), 8, 74, GuiStyle.COL_GREEN);
        GuiStyle.kv(guiGraphics, font,
                Component.translatable("container.overdrive.accelerator_controller.vacuum"),
                (menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %", 8, 84, GuiStyle.COL_CYAN);
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.accelerator_controller.usage",
                        ODFormat.formatEnergyPerTick(menu.getEnergyRateSynced())),
                8, 94, GuiStyle.COL_TEXT, false);
        guiGraphics.drawString(this.font, Component.literal("Rate:"), 8, 108, GuiStyle.COL_TEXT_DIM, false);

        if (rateField != null && !rateField.isFocused()) {
            String current = ODFormat.formatEnergy(menu.getEnergyRateSynced());
            if (!current.equals(rateField.getValue())) rateField.setValue(current);
        }
    }

    /** Ticks -> "45 s", "12 min 5 s", "5 h 33 min". */
    private static String fmtTime(long ticks) {
        long totalSeconds = ticks / 20;
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return h + " h " + m + " min";
        if (m > 0) return m + " min " + s + " s";
        return s + " s";
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

        if (in(mouseX, mouseY, x + BAR_ENERGY_X, y + BAR_Y, BAR_W, BAR_H)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(ODFormat.formatEnergyPair(menu.getEnergyLong(), menu.getMaxEnergyLong())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (in(mouseX, mouseY, x + BAR_VACUUM_X, y + BAR_Y, BAR_W, BAR_H)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal((menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (in(mouseX, mouseY, x + BAR_INPUT_X, y + BAR_Y, BAR_W, BAR_H)) {
            var be = menu.getBlockEntity();
            if (be != null) {
                var tank = be.getInputTank();
                renderFluidTooltip(guiGraphics, tank.getFluid(), tank.getFluidAmount(), tank.getCapacity(), "Empty", mouseX, mouseY);
            } else {
                super.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        } else if (in(mouseX, mouseY, x + BAR_OUTPUT_X, y + BAR_Y, BAR_W, BAR_H)) {
            var be = menu.getBlockEntity();
            if (be != null) {
                var tank = be.getFluidTank();
                renderFluidTooltip(guiGraphics, tank.getFluid(), tank.getFluidAmount(), tank.getCapacity(), "Empty", mouseX, mouseY);
            } else {
                super.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        } else if (in(mouseX, mouseY, x + HBAR_X, y + HBAR_Y, HBAR_W, HBAR_H)
                && menu.getChargeTicksTotal() > 1) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(String.format(Locale.ROOT, "Charge: %.1f %%", menu.getChargeScaled() / 2.0)));
            long elapsed = Math.min(menu.getChargeTicksElapsed(), menu.getChargeTicksTotal());
            lines.add(Component.literal("Time: " + fmtTime(elapsed) + " / " + fmtTime(menu.getChargeTicksTotal())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderFluidTooltip(GuiGraphics g, net.neoforged.neoforge.fluids.FluidStack stack, int amount, int capacity, String fallback, int mouseX, int mouseY) {
        Component name = stack.isEmpty() ? Component.literal(fallback) : stack.getHoverName();
        g.renderTooltip(font, List.of(name, Component.literal(ODFormat.formatFluidPair(amount, capacity))), java.util.Optional.empty(), mouseX, mouseY);
    }

    private static boolean in(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
