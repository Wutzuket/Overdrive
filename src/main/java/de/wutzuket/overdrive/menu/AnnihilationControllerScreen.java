package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity;
import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.network.AnnihilationBurnRatePayload;
import de.wutzuket.overdrive.network.AnnihilationTogglePayload;
import de.wutzuket.overdrive.network.ToggleHologramPayload;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

public class AnnihilationControllerScreen extends AbstractContainerScreen<AnnihilationControllerMenu> {
    private static final int COL_CYAN = GuiStyle.COL_CYAN;
    private static final int COL_GREEN = GuiStyle.COL_GREEN;
    private static final int COL_RED = GuiStyle.COL_RED;
    private static final int COL_YELLOW = GuiStyle.COL_YELLOW;
    private static final int COL_TEXT = GuiStyle.COL_TEXT;
    private static final int COL_TEXT_DIM = GuiStyle.COL_TEXT_DIM;

    private static final int WIDTH = 248;
    private static final int HEIGHT = 216;
    private static final int GAUGE_W = 14;
    private static final int GAUGE_H = 58;
    private static final int TAB_Y = HEIGHT - 22;

    private ButtonHelper hologramBtn;
    private ButtonHelper tempToggleBtn;
    private ButtonHelper btnActivate;
    private ButtonHelper btnScram;
    private ButtonHelper tabStatus;
    private ButtonHelper tabFuel;
    private ButtonHelper tabCoolant;
    private ButtonHelper tabStats;
    private EditBox burnRateField;
    private ButtonHelper burnMinus;
    private ButtonHelper burnPlus;
    private ButtonHelper burnSet;
    private int currentTab = 0;
    private boolean showKelvin = false;
    private double smoothTemp = 20;

    /** Geglättete Temperaturanzeige (Physik pendelt pro Tick, Anzeige folgt ruhig). */
    private double dispTemp() {
        smoothTemp += (menu.getTemp() - smoothTemp) * 0.15;
        return smoothTemp;
    }

    public AnnihilationControllerScreen(AnnihilationControllerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.titleLabelY = 5;
        this.inventoryLabelY = HEIGHT - 92;
    }

    private String fmtTemp(double celsius) {
        if (showKelvin) return String.format(Locale.ROOT, "%.0f K", celsius + 273.15);
        return String.format(Locale.ROOT, "%.0f C", celsius);
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;

        hologramBtn = new ButtonHelper(x + WIDTH - 22, y + 6, 16, 16, Component.literal("H"), b -> {
            var be = menu.getBlockEntity();
            if (be != null) PacketDistributor.sendToServer(new ToggleHologramPayload(be.getBlockPos()));
        }, COL_CYAN);
        addRenderableWidget(hologramBtn);

        tempToggleBtn = new ButtonHelper(x + WIDTH - 40, y + HEIGHT - 48, 34, 14, Component.literal(showKelvin ? "K" : "C"), b -> {
            showKelvin = !showKelvin;
            b.setMessage(Component.literal(showKelvin ? "K" : "C"));
        }, COL_CYAN);
        addRenderableWidget(tempToggleBtn);

        tabStatus = new ButtonHelper(x + 6, y + TAB_Y, 52, 14, Component.literal("Status"), b -> setTab(0), COL_CYAN);
        tabFuel = new ButtonHelper(x + 60, y + TAB_Y, 46, 14, Component.literal("Fuel"), b -> setTab(1), COL_CYAN);
        tabCoolant = new ButtonHelper(x + 108, y + TAB_Y, 58, 14, Component.literal("Coolant"), b -> setTab(2), COL_CYAN);
        tabStats = new ButtonHelper(x + 168, y + TAB_Y, 46, 14, Component.literal("Stats"), b -> setTab(3), COL_CYAN);
        addRenderableWidget(tabStatus);
        addRenderableWidget(tabFuel);
        addRenderableWidget(tabCoolant);
        addRenderableWidget(tabStats);

        btnActivate = new ButtonHelper(x + 8, y + HEIGHT - 48, 104, 16, Component.literal("ACTIVATE"), b -> {
            var be = menu.getBlockEntity();
            if (be != null && !be.isEnabled()) PacketDistributor.sendToServer(new AnnihilationTogglePayload(be.getBlockPos(), true));
        }, COL_GREEN);
        btnScram = new ButtonHelper(x + 116, y + HEIGHT - 48, 104, 16, Component.literal("SCRAM"), b -> {
            var be = menu.getBlockEntity();
            if (be != null && be.isEnabled()) PacketDistributor.sendToServer(new AnnihilationTogglePayload(be.getBlockPos(), false));
        }, COL_RED);
        addRenderableWidget(btnActivate);
        addRenderableWidget(btnScram);

        burnRateField = new EditBox(font, x + 78, y + HEIGHT - 70, 50, 12, Component.literal("burnRate"));
        burnRateField.setMaxLength(12);
        burnRateField.setValue(String.format(Locale.ROOT, "%.1f", menu.getBurnRateSynced()));
        addRenderableWidget(burnRateField);

        burnMinus = new ButtonHelper(x + 60, y + HEIGHT - 70, 16, 12, Component.literal("-"), b -> changeBurnRate(-0.1), COL_CYAN);
        burnPlus = new ButtonHelper(x + 130, y + HEIGHT - 70, 16, 12, Component.literal("+"), b -> changeBurnRate(0.1), COL_CYAN);
        burnSet = new ButtonHelper(x + 148, y + HEIGHT - 70, 24, 12, Component.literal("Set"), b -> submitBurnRate(), COL_CYAN);
        addRenderableWidget(burnMinus);
        addRenderableWidget(burnPlus);
        addRenderableWidget(burnSet);

        updateTabVis();
    }

    private void setTab(int tab) {
        currentTab = tab;
        updateTabVis();
    }

    private void changeBurnRate(double delta) {
        var be = menu.getBlockEntity();
        if (be == null) return;
        double value = Math.round(Math.max(be.getMinBurnRate(), be.getBurnRate() + delta) * 10) / 10.0;
        burnRateField.setValue(String.format(Locale.ROOT, "%.1f", value));
        PacketDistributor.sendToServer(new AnnihilationBurnRatePayload(be.getBlockPos(), value));
    }

    private void submitBurnRate() {
        var be = menu.getBlockEntity();
        if (be == null) return;
        try {
            double value = Double.parseDouble(burnRateField.getValue().replace(",", "."));
            value = Math.round(value * 10) / 10.0;
            value = Math.max(be.getMinBurnRate(), value);
            burnRateField.setValue(String.format(Locale.ROOT, "%.1f", value));
            PacketDistributor.sendToServer(new AnnihilationBurnRatePayload(be.getBlockPos(), value));
        } catch (NumberFormatException ignored) {
            burnRateField.setValue(String.format(Locale.ROOT, "%.1f", menu.getBurnRateSynced()));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (burnRateField != null && burnRateField.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                submitBurnRate();
                return true;
            }
            // Inventar-Taste (E) schlucken, solange das Textfeld aktiv ist - sonst
            // schliesst sich der Screen, wenn man die Burn-Rate eintippt.
            if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateTabVis() {
        if (tabStatus != null) tabStatus.setMessage(Component.literal(currentTab == 0 ? "[Status]" : "Status"));
        if (tabFuel != null) tabFuel.setMessage(Component.literal(currentTab == 1 ? "[Fuel]" : "Fuel"));
        if (tabCoolant != null) tabCoolant.setMessage(Component.literal(currentTab == 2 ? "[Coolant]" : "Coolant"));
        if (tabStats != null) tabStats.setMessage(Component.literal(currentTab == 3 ? "[Stats]" : "Stats"));
        boolean fuelTab = currentTab == 1;
        if (burnMinus != null) burnMinus.visible = fuelTab;
        if (burnPlus != null) burnPlus.visible = fuelTab;
        if (burnSet != null) burnSet.visible = fuelTab;
        if (burnRateField != null) burnRateField.setVisible(fuelTab);
    }

    private void drawMekaBg(GuiGraphics g, int x, int y) {
        GuiStyle.panel(g, x, y, WIDTH, HEIGHT);
    }

    private void drawFluidGauge(GuiGraphics g, FluidStack stack, int capacity, int x, int y, String label) {
        GuiStyle.fluidGauge(g, font, stack, capacity, x, y, GAUGE_W, GAUGE_H, label);
    }

    private void drawAntimatterGauge(GuiGraphics g, de.wutzuket.overdrive.antimatter.Antimatter type,
                                     int amount, int capacity, int x, int y, String label) {
        GuiStyle.antimatterGauge(g, font, type, amount, capacity, x, y, GAUGE_W, GAUGE_H, label);
    }

    private void drawMetricGauge(GuiGraphics g, int x, int y, float percent, int color, String label) {
        GuiStyle.metricGauge(g, font, x, y, GAUGE_W, GAUGE_H, percent, color, label);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        drawMekaBg(g, x, y);

        int gaugeY = y + 22;
        drawAntimatterGauge(g, menu.getFuelPositronType(), menu.getFuelPositronSynced(),
                menu.getFuelPositronCap(), x + 10, gaugeY, "P");
        drawAntimatterGauge(g, menu.getFuelAntiprotonType(), menu.getFuelAntiprotonSynced(),
                menu.getFuelAntiprotonCap(), x + 30, gaugeY, "A");
        drawFluidGauge(g, menu.getCoolantFluidStack(), menu.getCoolantCap(), x + 50, gaugeY, "C");
        drawMetricGauge(g, x + 70, gaugeY, menu.getVacuum() / 100.0f, COL_CYAN, "V");

        drawMetricGauge(g, x + WIDTH - 82, gaugeY, (float) (menu.getEnergyLong() / (double) Math.max(1L, menu.getMaxEnergyLong())), COL_GREEN, "E");
        drawMetricGauge(g, x + WIDTH - 62, gaugeY, Math.min(1.0f, (float) (dispTemp() / AnnihilationControllerEntity.CORE_TEMP_MAX)), dispTemp() > AnnihilationControllerEntity.CORE_TEMP_MAX - 200 ? COL_RED : COL_YELLOW, "T");
        drawFluidGauge(g, menu.getSteamFluidStack(), menu.getSteamCap(), x + WIDTH - 42, gaugeY, "S");

        int screenX = x + 91;
        int screenY = y + 20;
        int screenW = 66;
        int screenH = 70;
        GuiStyle.inset(g, screenX, screenY, screenW, screenH);
        g.fill(screenX + 2, screenY + 2, screenX + screenW - 2, screenY + screenH - 2, 0xFF0A0A0E);

        int heatX = x + 8;
        int heatY = y + 104;
        int heatW = WIDTH - 16;
        int heatH = 9;
        GuiStyle.inset(g, heatX, heatY, heatW, heatH);
        double heatLevel = Math.min(1.0, dispTemp() / (double) AnnihilationControllerEntity.CORE_TEMP_MAX);
        int heatFill = (int) (heatLevel * (heatW - 2));
        if (heatFill > 0) {
            int col = heatLevel > 0.85 ? COL_RED : heatLevel > 0.6 ? COL_YELLOW : COL_CYAN;
            g.fill(heatX + 1, heatY + 1, heatX + 1 + heatFill, heatY + heatH - 1, col);
            g.fill(heatX + 1, heatY + 1, heatX + 1 + heatFill, heatY + 2, 0x30FFFFFF);
        }

        GuiStyle.inset(g, x + 8, y + 121, WIDTH - 16, 42);

        if (btnActivate != null) btnActivate.active = !menu.isActiveSynced() && menu.isValid();
        if (btnScram != null) btnScram.active = menu.isActiveSynced();
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 7, COL_TEXT, false);

        int sx = 95;
        int sy = 24;
        String status = !menu.isValid() ? "Invalid" : menu.isActiveSynced() ? "Active" : menu.isEnabledSynced() ? "Idle" : "Disabled";
        int statusColor = !menu.isValid() ? COL_RED : menu.isActiveSynced() ? COL_GREEN : menu.isEnabledSynced() ? COL_YELLOW : COL_TEXT_DIM;
        String detail = !menu.isValid() ? "Structure" : menu.isActiveSynced() ? "Annihilating" : menu.isEnabledSynced() ? idleDetail() : "Stopped";
        g.drawString(font, "Status", sx, sy, COL_TEXT_DIM, false);
        g.drawString(font, status, sx, sy + 10, statusColor, false);
        g.drawString(font, detail, sx, sy + 20, COL_TEXT_DIM, false);
        g.drawString(font, "Burn " + ODFormat.formatFluidPerTick(menu.getBurnRateSynced()), sx, sy + 34, COL_CYAN, false);
        g.drawString(font, "Temp " + fmtTemp(dispTemp()), sx, sy + 44, dispTemp() > AnnihilationControllerEntity.CORE_TEMP_MAX - 200 ? COL_RED : COL_TEXT, false);
        g.drawString(font, "Gen " + ODFormat.formatEnergyPerTick(menu.getGenerationLong()), sx, sy + 54, COL_GREEN, false);

        if (burnRateField != null && !burnRateField.isFocused()) {
            String current = String.format(Locale.ROOT, "%.1f", menu.getBurnRateSynced());
            if (!current.equals(burnRateField.getValue())) burnRateField.setValue(current);
        }

        int panelX = 13;
        int panelY = 127;
        if (currentTab == 0) {
            drawRows(g, panelX, panelY,
                    new Row("Vacuum", String.format(Locale.ROOT, "%.1f %%", menu.getVacuum()), COL_CYAN),
                    new Row("Energy", ODFormat.formatEnergyPair(menu.getEnergyLong(), menu.getMaxEnergyLong()), COL_GREEN),
                    new Row("Heat", fmtTemp(dispTemp()) + " / " + fmtTemp(AnnihilationControllerEntity.CORE_TEMP_MAX),
                            dispTemp() > AnnihilationControllerEntity.CORE_TEMP_MAX - 200 ? COL_RED : COL_YELLOW));
        } else if (currentTab == 1) {
            drawRows(g, panelX, panelY,
                    new Row(menu.getFuelAntiprotonName().isEmpty() ? "Antiproton" : menu.getFuelAntiprotonName(),
                            ODFormat.formatFluidPair(menu.getFuelAntiprotonSynced(), menu.getFuelAntiprotonCap()), COL_TEXT),
                    new Row(menu.getFuelPositronName().isEmpty() ? "Positron" : menu.getFuelPositronName(),
                            ODFormat.formatFluidPair(menu.getFuelPositronSynced(), menu.getFuelPositronCap()), COL_TEXT));
            g.drawString(font, "Burn rate", 13, HEIGHT - 69, COL_TEXT_DIM, false);
        } else if (currentTab == 2) {
            drawRows(g, panelX, panelY,
                    new Row(AnnihilationControllerMenu.fluidName(menu.getCoolantFluidStack(), "Empty"),
                            ODFormat.formatFluidPair(menu.getCoolantAmount(), menu.getCoolantCap()), COL_CYAN),
                    new Row(AnnihilationControllerMenu.fluidName(menu.getSteamFluidStack(), "Empty"),
                            ODFormat.formatFluidPair(menu.getSteamAmount(), menu.getSteamCap()), COL_TEXT));
        } else {
            drawPortLine(g, "Cells", menu.getLastCellSynced(), 2, panelX, panelY);
            drawPortLine(g, "Vacuum", menu.getLastVacSynced(), 1, panelX + 110, panelY);
            drawPortLine(g, "Coolant", menu.getLastCoolSynced(), 1, panelX, panelY + 10);
            drawPortLine(g, "Steam", menu.getLastSteamSynced(), 1, panelX + 110, panelY + 10);
            drawPortLine(g, "Energy", menu.getLastEnergySynced(), 1, panelX, panelY + 20);
        }
    }

    private String idleDetail() {
        return switch (menu.getIdleCode()) {
            case 1 -> "Need vacuum";
            case 2 -> "Need fuel";
            case 3 -> "Need coolant";
            case 4 -> "Steam full";
            case 5 -> "No recipe";
            default -> "Ready";
        };
    }

    private void drawRows(GuiGraphics g, int x, int y, Row... rows) {
        int labelW = 0;
        for (Row r : rows) {
            labelW = Math.max(labelW, font.width(r.label + ":"));
        }
        int vx = x + labelW + 6;
        for (int i = 0; i < rows.length; i++) {
            int ry = y + i * 10;
            g.drawString(font, rows[i].label + ":", x, ry, COL_TEXT_DIM, false);
            g.drawString(font, rows[i].value, vx, ry, rows[i].color, false);
        }
    }

    private static final class Row {
        final String label;
        final String value;
        final int color;

        Row(String label, String value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private void drawPortLine(GuiGraphics g, String label, int found, int need, int x, int y) {
        int col = found >= need ? COL_GREEN : COL_RED;
        g.drawString(font, label + " " + found + "/" + need, x, y, col, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        int gaugeY = y + 22;

        if (in(mouseX, mouseY, x + 10, gaugeY, GAUGE_W, GAUGE_H)) {
            renderAntimatterTooltip(g, menu.getFuelPositronName(), menu.getFuelPositronSynced(), menu.getFuelPositronCap(), mouseX, mouseY);
            return;
        }
        if (in(mouseX, mouseY, x + 30, gaugeY, GAUGE_W, GAUGE_H)) {
            renderAntimatterTooltip(g, menu.getFuelAntiprotonName(), menu.getFuelAntiprotonSynced(), menu.getFuelAntiprotonCap(), mouseX, mouseY);
            return;
        }
        if (in(mouseX, mouseY, x + 50, gaugeY, GAUGE_W, GAUGE_H)) {
            renderFluidTooltip(g, menu.getCoolantFluidStack(), menu.getCoolantAmount(), menu.getCoolantCap(), "Empty", mouseX, mouseY);
            return;
        }
        if (in(mouseX, mouseY, x + 70, gaugeY, GAUGE_W, GAUGE_H)) {
            g.renderTooltip(font, List.of(Component.literal(String.format(Locale.ROOT, "Vacuum: %.1f %%", menu.getVacuum()))), java.util.Optional.empty(), mouseX, mouseY);
            return;
        }
        if (in(mouseX, mouseY, x + WIDTH - 82, gaugeY, GAUGE_W, GAUGE_H)) {
            g.renderTooltip(font, List.of(Component.literal("Energy: " + ODFormat.formatEnergyPair(menu.getEnergyLong(), menu.getMaxEnergyLong())), Component.literal("Generation: " + ODFormat.formatEnergyPerTick(menu.getGenerationLong()))), java.util.Optional.empty(), mouseX, mouseY);
            return;
        }
        if (in(mouseX, mouseY, x + WIDTH - 62, gaugeY, GAUGE_W, GAUGE_H)) {
            g.renderTooltip(font, List.of(Component.literal("Temperature: " + fmtTemp(dispTemp())), Component.literal("Limit: " + fmtTemp(AnnihilationControllerEntity.CORE_TEMP_MAX))), java.util.Optional.empty(), mouseX, mouseY);
            return;
        }
        if (in(mouseX, mouseY, x + WIDTH - 42, gaugeY, GAUGE_W, GAUGE_H)) {
            renderFluidTooltip(g, menu.getSteamFluidStack(), menu.getSteamAmount(), menu.getSteamCap(), "Empty", mouseX, mouseY);
            return;
        }
        super.renderTooltip(g, mouseX, mouseY);
    }

    private void renderAntimatterTooltip(GuiGraphics g, String name, int amount, int capacity, int mouseX, int mouseY) {
        Component title = name == null || name.isEmpty() ? Component.literal("Empty") : Component.literal(name);
        g.renderTooltip(font, List.of(title, Component.literal(ODFormat.formatFluidPair(amount, capacity))), java.util.Optional.empty(), mouseX, mouseY);
    }

    private void renderFluidTooltip(GuiGraphics g, FluidStack stack, int amount, int capacity, String fallback, int mouseX, int mouseY) {
        Component name = stack.isEmpty() ? Component.literal(fallback) : stack.getHoverName();
        g.renderTooltip(font, List.of(name, Component.literal(ODFormat.formatFluidPair(amount, capacity))), java.util.Optional.empty(), mouseX, mouseY);
    }

    private static boolean in(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
