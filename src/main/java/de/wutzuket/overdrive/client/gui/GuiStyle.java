package de.wutzuket.overdrive.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;

public final class GuiStyle {
    public static final int COL_BG = 0xFF1E1E24;
    public static final int COL_BG_ALT = 0xFF23232A;
    public static final int COL_INSET = 0xFF101014;
    public static final int COL_INSET_DARK = 0xFF101012;
    public static final int COL_BORDER = 0xFF050509;
    public static final int COL_BORDER_LIGHT = 0xFF363643;
    public static final int COL_SCREEN = 0xFF0A0A0E;
    public static final int COL_CYAN = 0xFF00F0FF;
    public static final int COL_GREEN = 0xFF22C55E;
    public static final int COL_RED = 0xFFEF4444;
    public static final int COL_YELLOW = 0xFFEAB308;
    public static final int COL_TEXT = 0xFFD1D5DB;
    public static final int COL_TEXT_DIM = 0xFF9CA3AF;

    private GuiStyle() {
    }

    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COL_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, COL_BG_ALT);
        g.fill(x + 4, y + 4, x + w - 4, y + h - 4, COL_BG);
        g.fill(x, y, x + w, y + 1, COL_BORDER_LIGHT);
        g.fill(x, y, x + 1, y + h, COL_BORDER_LIGHT);
        g.fill(x + w - 1, y, x + w, y + h, COL_BORDER);
        g.fill(x, y + h - 1, x + w, y + h, COL_BORDER);
    }

    public static void inset(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COL_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, COL_INSET);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x221AFFFF);
    }

    public static void screenArea(GuiGraphics g, int x, int y, int w, int h) {
        inset(g, x, y, w, h);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, COL_SCREEN);
    }

    public static void fluidGauge(GuiGraphics g, Font font, FluidStack stack, int capacity,
                                  int x, int y, int w, int h, String label) {
        FluidRenderHelper.renderFluidGauge(g, stack, capacity, x, y, w, h);
        if (label != null) {
            g.drawCenteredString(font, label, x + w / 2, y + h + 3, COL_TEXT_DIM);
        }
    }

    /** Fuellstandsanzeige fuer ein Antimatter-Tank, in der Farbe des Antimatters. */
    public static void antimatterGauge(GuiGraphics g, Font font, de.wutzuket.overdrive.antimatter.Antimatter type,
                                       int amount, int capacity, int x, int y, int w, int h, String label) {
        g.fill(x, y, x + w, y + h, COL_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, COL_INSET_DARK);
        int innerH = h - 4;
        int fill = capacity <= 0 ? 0 : Math.max(0, Math.min(innerH, (int) ((long) amount * innerH / capacity)));
        if (fill > 0) {
            int color = type == null ? COL_CYAN : type.color();
            int top = y + 2 + innerH - fill;
            g.fill(x + 2, top, x + w - 2, y + h - 2, color);
            g.fill(x + 2, top, x + w - 2, top + 2, 0x45FFFFFF);
        }
        if (label != null) {
            g.drawCenteredString(font, label, x + w / 2, y + h + 3, COL_TEXT_DIM);
        }
    }

    public static void metricGauge(GuiGraphics g, Font font, int x, int y, int w, int h,
                                   float percent, int color, String label) {
        g.fill(x, y, x + w, y + h, COL_BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, COL_INSET_DARK);
        int innerH = h - 4;
        int fill = Math.max(0, Math.min(innerH, (int) (percent * innerH)));
        if (fill > 0) {
            int top = y + 2 + innerH - fill;
            g.fill(x + 2, top, x + w - 2, y + h - 2, color);
            g.fill(x + 2, top, x + w - 2, top + 2, 0x45FFFFFF);
        }
        if (label != null) {
            g.drawCenteredString(font, label, x + w / 2, y + h + 3, COL_TEXT_DIM);
        }
    }

    public static void hBar(GuiGraphics g, int x, int y, int w, int h, double fraction, int color) {
        inset(g, x, y, w, h);
        int fill = (int) (Math.max(0.0, Math.min(1.0, fraction)) * (w - 2));
        if (fill > 0) {
            g.fill(x + 1, y + 1, x + 1 + fill, y + h - 1, color);
            g.fill(x + 1, y + 1, x + 1 + fill, y + 2, 0x30FFFFFF);
        }
    }

    public static void slotFrame(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, COL_BORDER);
        g.fill(x + 1, y + 1, x + 17, y + 17, COL_INSET);
        g.fill(x + 1, y + 1, x + 17, y + 2, 0x221AFFFF);
    }

    public static void slotFrames(GuiGraphics g, AbstractContainerMenu menu, int originX, int originY) {
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            slotFrame(g, originX + slot.x - 1, originY + slot.y - 1);
        }
    }

    public static void kv(GuiGraphics g, Font font, net.minecraft.network.chat.Component label, String value,
                          int x, int y, int valueColor) {
        String text = label.getString() + ":";
        g.drawString(font, text, x, y, COL_TEXT_DIM, false);
        g.drawString(font, value, x + font.width(text) + 4, y, valueColor, false);
    }
}
