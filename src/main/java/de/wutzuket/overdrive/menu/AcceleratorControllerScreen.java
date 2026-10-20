package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.network.AcceleratorEnergyRatePayload;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AcceleratorControllerScreen extends AbstractContainerScreen<AcceleratorControllerMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 146;

    private static final int BAR_W = 16;
    private static final int BAR_H = 54;

    private ButtonHelper hologramButton;
    private EditBox rateField;
    private ButtonHelper rateSet;

    public AcceleratorControllerScreen(AcceleratorControllerMenu menu, Inventory inventory, Component title) {
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
                    boolean current = menu.getBlockEntity().isHologramEnabled();
                    menu.getBlockEntity().setHologramEnabled(!current);
                }, GuiStyle.COL_CYAN);
        addRenderableWidget(hologramButton);

        rateField = new EditBox(font, x + 48, y + 114, 66, 14, Component.literal("rate"));
        rateField.setMaxLength(16);
        rateField.setValue(ODFormat.formatEnergy(menu.getEnergyRateSynced()));
        addRenderableWidget(rateField);
        rateSet = new ButtonHelper(x + 116, y + 114, 44, 14, Component.literal("Set"),
                b -> submitRate(), GuiStyle.COL_CYAN);
        addRenderableWidget(rateSet);
    }

    private void submitRate() {
        if (rateField == null) return;
        try {
            long rate = parseRate(rateField.getValue());
            rate = Math.max(1, Math.min(menu.getMaxRate(), rate));
            PacketDistributor.sendToServer(new AcceleratorEnergyRatePayload(menu.getBlockEntity().getBlockPos(), (int) rate));
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
        if (rateField != null && rateField.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                submitRate();
                return true;
            }
            // Inventar-Taste (E) schlucken, solange das Textfeld aktiv ist - sonst
            // schliesst sich der Screen, wenn man die FE-Rate eintippt.
            if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiStyle.panel(guiGraphics, x, y, WIDTH, HEIGHT);

        GuiStyle.metricGauge(guiGraphics, font, x + 8, y + 16, BAR_W, BAR_H,
                menu.getEnergyScaled(BAR_H - 2) / (float) (BAR_H - 2), GuiStyle.COL_RED, null);
        GuiStyle.metricGauge(guiGraphics, font, x + 28, y + 16, BAR_W, BAR_H,
                menu.getVacuumScaled(BAR_H - 2) / (float) (BAR_H - 2), GuiStyle.COL_CYAN, null);
        GuiStyle.fluidGauge(guiGraphics, font, getIridiumStack(), menu.getIridiumInputCap(),
                x + 48, y + 16, BAR_W, BAR_H, null);
        GuiStyle.antimatterGauge(guiGraphics, font, menu.getOutputAntimatterType(),
                menu.getOutputAmountSynced(), menu.getFluidCapacitySynced(),
                x + 68, y + 16, BAR_W, BAR_H, null);

        GuiStyle.hBar(guiGraphics, x + 90, y + 34, 56, 10,
                Math.max(Math.min(menu.getProgressTicks(), 200), menu.getChargeScaled()) / 200.0, GuiStyle.COL_CYAN);
    }

    private FluidStack getIridiumStack() {
        Fluid fluid = menu.getIridiumFluid();
        int amount = menu.getIridiumInputSynced();
        if (fluid == null || fluid == Fluids.EMPTY || amount <= 0) return FluidStack.EMPTY;
        return new FluidStack(fluid, amount);
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
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);

        if (menu.isValidStructure()) {
            guiGraphics.drawString(this.font,
                    Component.translatable(menu.isActive()
                            ? "container.overdrive.accelerator_controller.running"
                            : "container.overdrive.accelerator_controller.idle"),
                    90, 50, menu.isActive() ? GuiStyle.COL_GREEN : GuiStyle.COL_YELLOW, false);
        } else {
            guiGraphics.drawString(this.font,
                    Component.translatable("container.overdrive.accelerator_controller.invalid_structure"),
                    90, 50, GuiStyle.COL_RED, false);
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
        // Im Leerlauf wird nichts produziert -> 0 B/t anzeigen.
        double production = menu.isActive() ? menu.getProductionPerTickSynced() : 0.0;
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.production",
                        ODFormat.formatFluidPerTick(production)),
                8, 104, production > 0 ? GuiStyle.COL_GREEN : GuiStyle.COL_TEXT_DIM, false);
        guiGraphics.drawString(this.font, Component.literal("Rate:"), 8, 118, GuiStyle.COL_TEXT_DIM, false);

        if (rateField != null && !rateField.isFocused()) {
            String current = ODFormat.formatEnergy(menu.getEnergyRateSynced());
            if (!current.equals(rateField.getValue())) rateField.setValue(current);
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
            lines.add(Component.literal(ODFormat.formatEnergyPair(menu.getEnergyLong(), menu.getMaxEnergyLong())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 28 && mouseX <= x + 44 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal((menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 48 && mouseX <= x + 64 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            FluidStack stack = getIridiumStack();
            lines.add(stack.isEmpty() ? Component.literal("Empty") : stack.getHoverName());
            lines.add(Component.literal(ODFormat.formatFluidPair(menu.getIridiumInputSynced(), menu.getIridiumInputCap())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 68 && mouseX <= x + 84 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            var type = menu.getOutputAntimatterType();
            lines.add(type == null ? Component.literal("Empty") : type.displayName());
            lines.add(Component.literal(ODFormat.formatFluidPair(menu.getFluidAmountSynced(), menu.getFluidCapacitySynced())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 90 && mouseX < x + 146 && mouseY >= y + 34 && mouseY < y + 44
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
}
