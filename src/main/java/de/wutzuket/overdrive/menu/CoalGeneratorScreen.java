package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 192;

    // Energie-Gauge links
    private static final int GAUGE_X = 8;
    private static final int GAUGE_Y = 18;
    private static final int GAUGE_W = 14;
    private static final int GAUGE_H = 44;

    // Textfeld rechts
    private static final int PANEL_X = 48;
    private static final int PANEL_Y = 14;
    private static final int PANEL_W = 122;
    private static final int PANEL_H = 76;
    private static final int TEXT_X = 52;
    private static final int TEXT_Y = 17;

    // Brennzeit-Balken innerhalb des Textfelds
    private static final int BURN_X = 52;
    private static final int BURN_Y = 78;
    private static final int BURN_W = 114;
    private static final int BURN_H = 8;

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.titleLabelY = 4;
        this.inventoryLabelY = HEIGHT - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        GuiStyle.panel(g, x, y, WIDTH, HEIGHT);

        CoalGeneratorMenu menu = this.menu;

        // Energie-Gauge
        double energyPct = menu.getMaxEnergy() > 0 ? menu.getEnergy() / (double) menu.getMaxEnergy() : 0.0;
        GuiStyle.metricGauge(g, font, x + GAUGE_X, y + GAUGE_Y, GAUGE_W, GAUGE_H, (float) energyPct, GuiStyle.COL_GREEN, null);

        // Textfeld
        GuiStyle.screenArea(g, x + PANEL_X, y + PANEL_Y, PANEL_W, PANEL_H);

        // Brennzeit-Balken
        double burnPct = menu.getCurrentBurnTime() > 0 ? menu.getBurnTime() / (double) menu.getCurrentBurnTime() : 0.0;
        int burnColor = menu.isActive() ? GuiStyle.COL_YELLOW : GuiStyle.COL_TEXT_DIM;
        GuiStyle.hBar(g, x + BURN_X, y + BURN_Y, BURN_W, BURN_H, burnPct, burnColor);

        // Slot-Rahmen
        GuiStyle.slotFrames(g, menu, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, 8, 4, GuiStyle.COL_TEXT, false);

        CoalGeneratorMenu menu = this.menu;
        int sx = TEXT_X;
        int sy = TEXT_Y;

        // Status
        String status = menu.isActive() ? "§aRunning" : "§eIdle";
        g.drawString(font, "Status: " + status, sx, sy, GuiStyle.COL_TEXT, false);

        // Fuel
        ItemStack fuel = menu.getBlockEntity().getFuelStack();
        String fuelLine = fuel.isEmpty() ? "Fuel: Empty" : "Fuel: " + fuel.getHoverName().getString();
        g.drawString(font, fuelLine, sx, sy + 10, fuel.isEmpty() ? GuiStyle.COL_TEXT_DIM : GuiStyle.COL_CYAN, false);

        // Output
        g.drawString(font, "Output: " + ODFormat.formatEnergyPerTick(menu.getEnergyPerTick()),
                sx, sy + 20, GuiStyle.COL_GREEN, false);

        // Auto-Push Status
        boolean push = ODConfig.COAL_GENERATOR_AUTO_PUSH.get();
        boolean receiver = menu.getBlockEntity().hasEnergyReceiver(menu.getBlockEntity().getLevel(), menu.getBlockEntity().getBlockPos());
        String pushLine;
        if (!push) {
            pushLine = "§7Push: off";
        } else if (receiver) {
            pushLine = "§aPush: active";
        } else {
            pushLine = "§ePush: no receiver";
        }
        g.drawString(font, pushLine, sx, sy + 30, GuiStyle.COL_TEXT, false);

        // Energy (gespeichert) - max. Wert steht im Tooltip der Gauge
        g.drawString(font, "Energy: " + ODFormat.formatEnergy(menu.getEnergy()),
                sx, sy + 40, GuiStyle.COL_TEXT_DIM, false);

        // Brennzeit
        g.drawString(font, "Burn: " + menu.getBurnTime() + "/" + menu.getCurrentBurnTime() + " t",
                sx, sy + 50, GuiStyle.COL_TEXT_DIM, false);

        // Label am Fuel-Slot
        g.drawString(font, "Fuel", CoalGeneratorMenu.FUEL_SLOT_X, CoalGeneratorMenu.FUEL_SLOT_Y - 10, GuiStyle.COL_TEXT_DIM, false);

        // Label am Energie-Gauge
        g.drawString(font, "FE", GAUGE_X, GAUGE_Y + GAUGE_H + 2, GuiStyle.COL_TEXT_DIM, false);

        // Spieler-Inventar
        g.drawString(font, this.playerInventoryTitle, 8, this.inventoryLabelY, GuiStyle.COL_TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        CoalGeneratorMenu menu = this.menu;

        if (mx >= x + GAUGE_X && mx <= x + GAUGE_X + GAUGE_W
                && my >= y + GAUGE_Y && my <= y + GAUGE_Y + GAUGE_H) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("Energy"));
            lines.add(Component.literal(ODFormat.formatEnergyPair(menu.getEnergy(), menu.getMaxEnergy())));
            lines.add(Component.literal("Output: " + ODFormat.formatEnergyPerTick(menu.getEnergyPerTick())));
            g.renderTooltip(font, lines, java.util.Optional.empty(), mx, my);
            return;
        }

        if (mx >= x + BURN_X && mx <= x + BURN_X + BURN_W
                && my >= y + BURN_Y && my <= y + BURN_Y + BURN_H) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal("Burn time"));
            lines.add(Component.literal(menu.getBurnTime() + " / " + menu.getCurrentBurnTime() + " ticks"));
            ItemStack fuel = menu.getBlockEntity().getFuelStack();
            if (!fuel.isEmpty()) {
                lines.add(fuel.getHoverName());
            }
            g.renderTooltip(font, lines, java.util.Optional.empty(), mx, my);
            return;
        }

        super.renderTooltip(g, mx, my);
    }
}
