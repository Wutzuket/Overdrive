package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.network.ElectronGunEnergyRatePayload;
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

public class ElectronGunScreen extends AbstractContainerScreen<ElectronGunMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 146;
    private static final int BAR_W = 16;
    private static final int BAR_H = 54;

    private ButtonHelper hologramButton;
    private EditBox rateField;
    private ButtonHelper rateSet;

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
            PacketDistributor.sendToServer(new ElectronGunEnergyRatePayload(menu.getBlockEntity().getBlockPos(), (int) rate));
            rateField.setValue(ODFormat.formatEnergy(rate));
        } catch (Exception ignored) {
            rateField.setValue(ODFormat.formatEnergy(menu.getEnergyRateSynced()));
        }
    }

    /** Parst "3K", "3000 FE", "1.2M", "3000" -> FE. */
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

        float energy = menu.getMaxEnergy() > 0 ? (float) menu.getEnergy() / menu.getMaxEnergy() : 0.0f;
        GuiStyle.metricGauge(guiGraphics, font, x + 8, y + 16, BAR_W, BAR_H, energy,
                GuiStyle.COL_GREEN, null);
        GuiStyle.metricGauge(guiGraphics, font, x + 28, y + 16, BAR_W, BAR_H,
                menu.getVacuumScaled(BAR_H - 2) / (float) (BAR_H - 2), GuiStyle.COL_CYAN, null);
        GuiStyle.antimatterGauge(guiGraphics, font, menu.getAntimatterType(), menu.getFluidAmount(),
                menu.getFluidCapacity(), x + 48, y + 16, BAR_W, BAR_H, null);

        GuiStyle.hBar(guiGraphics, x + 70, y + 34, 96, 10,
                menu.getProgressScaled(94) / 94.0, GuiStyle.COL_CYAN);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, GuiStyle.COL_TEXT, false);
        // renderLabels zeichnet relativ zum GUI-Ursprung, renderBg dagegen absolut.
        // Deshalb gehoert der Status hierher, sonst landet er irgendwo auf dem Bildschirm.
        if (menu.isActive()) {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.electron_gun.running"),
                    70, 50, GuiStyle.COL_GREEN, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("container.overdrive.electron_gun.idle"),
                    70, 50, GuiStyle.COL_YELLOW, false);
        }
        GuiStyle.kv(guiGraphics, font,
                Component.translatable("container.overdrive.electron_gun.energy"),
                ODFormat.formatEnergyPair(menu.getEnergy(), menu.getMaxEnergy()), 8, 74, GuiStyle.COL_GREEN);
        GuiStyle.kv(guiGraphics, font,
                Component.translatable("container.overdrive.electron_gun.vacuum"),
                (menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %", 8, 84, GuiStyle.COL_CYAN);
        guiGraphics.drawString(this.font,
                Component.translatable("container.overdrive.electron_gun.usage",
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
            lines.add(Component.literal(ODFormat.formatEnergyPair(menu.getEnergy(), menu.getMaxEnergy())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 28 && mouseX <= x + 44 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal((menu.getVacuumLevel() / 10) + "." + (menu.getVacuumLevel() % 10) + " %"));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= x + 48 && mouseX <= x + 64 && mouseY >= y + 16 && mouseY <= y + 70) {
            List<Component> lines = new ArrayList<>();
            var type = menu.getAntimatterType();
            lines.add(type == null
                    ? Component.translatable("container.overdrive.electron_gun.empty")
                    : type.displayName());
            lines.add(Component.literal(ODFormat.formatFluidPair(menu.getFluidAmount(), menu.getFluidCapacity())));
            guiGraphics.renderTooltip(this.font, lines, java.util.Optional.empty(), mouseX, mouseY);
        } else {
            super.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
